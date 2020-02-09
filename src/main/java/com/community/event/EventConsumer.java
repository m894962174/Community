package com.community.event;

import com.alibaba.fastjson.JSONObject;
import com.community.service.impl.DiscussPostService;
import com.community.service.impl.ElasticSearchService;
import com.community.service.impl.MessageService;
import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import com.community.vo.DiscussPost;
import com.community.vo.Event;
import com.community.vo.Message;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/27/19:53
 * @Description:
 */
@Component
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    MessageService messageService;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    ThreadPoolTaskScheduler taskScheduler;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    /**
     * 处理kafka发布的事件消息：
     * 放置在message对象中，存于数据库
     *
     * @param record
     */
    @KafkaListener(topics = {CommonStatus.TOPIC_COMMENT, CommonStatus.TOPIC_FOLLOW, CommonStatus.TOPIC_LIKE})
    public void dealEventMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("生成消息为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("生成消息格式错误");
            return;
        }
        Message message = new Message();
        message.setFromId(CommonStatus.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //拼接消息内容的成员转换成json格式，存于正文字段
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.insertMessage(message);
    }

    /**
     * 处理发帖事件
     *
     * @param record
     */
    @KafkaListener(topics = CommonStatus.TOPIC_PUBLISH)
    public void dealPublishPost(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("生成消息为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("生成消息格式错误");
            return;
        }
        DiscussPost discussPost = discussPostService.selectDisCussPostById(event.getEntityId());
        elasticSearchService.insert(discussPost);
    }

    /**
     * 处理删帖事件
     *
     * @param record
     */
    @KafkaListener(topics = CommonStatus.TOPIC_DELETE)
    public void dealDeletePost(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("生成消息为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("生成消息格式错误");
            return;
        }
        elasticSearchService.delete(event.getEntityId());
    }

    /**
     * 处理分享事件
     *
     * @param record
     */
    @KafkaListener(topics = CommonStatus.TOPIC_SHARE)
    public void dealShare(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("生成消息为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("生成消息格式错误");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("分享生成图片成功！" + cmd);
        } catch (IOException e) {
            logger.error("分享生成图片失败" + e.getMessage());
        }

        //启用定时任务，监视分享图片的生成
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 2000);
        //当有返回结果时，说明一次定时任务完成，将本次结果装配进UploadTask
        task.setFuture(future);
    }

    //封装定时任务逻辑
    class UploadTask implements Runnable {

        private String fileName;

        private String suffix;

        //本次任务执行结果
        private Future future;

        //任务开始时间
        private long startTime;

        //任务最大重试次数为三次
        int count;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() - startTime > 100000) {
                logger.info("任务执行过长， 终止任务:" + fileName);
                future.cancel(true);
                return;
            }

            if (count >= 3) {
                logger.info("任务重试已超过三次， 终止任务：" + fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传[%s].", ++count, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                //此处设0 用于后面是否取到， 验证本地上传到七牛云服务器是否成功
                policy.put("returnBody", CommonUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房 (zone1 : 华北地区)
                UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s].", count, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s].", count, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s].", count, fileName));
                }
            }else {
                logger.info("本地分享图片尚未生成：" + fileName);
            }
        }
    }
}
