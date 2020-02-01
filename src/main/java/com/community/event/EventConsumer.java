package com.community.event;

import com.alibaba.fastjson.JSONObject;
import com.community.service.impl.DiscussPostService;
import com.community.service.impl.ElasticSearchService;
import com.community.service.impl.MessageService;
import com.community.util.CommonStatus;
import com.community.vo.DiscussPost;
import com.community.vo.Event;
import com.community.vo.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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



    /**
     * 处理kafka发布的事件消息：
     * 放置在message对象中，存于数据库
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

        if(!event.getData().isEmpty()) {
            for(Map.Entry<String, Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.insertMessage(message);
    }

    /**
     * 处理发帖事件
     * @param record
     */
    @KafkaListener(topics = CommonStatus.TOPIC_PUBLISH)
    public void  dealPublishPost(ConsumerRecord record){
        if(record == null || record.value() == null) {
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
}
