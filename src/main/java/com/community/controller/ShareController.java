package com.community.controller;

import com.community.event.EventProducer;
import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import com.community.vo.Event;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/09/11:37
 * @Description:
 */
@Controller
public class ShareController {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;


    /**
     * 分享当前页面，返回信息包含生成图片的访问路径
     *
     * @param htmlUrl
     * @return
     */
    @RequestMapping(value = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String shareImage(String htmlUrl) {
        String fileName = CommonUtil.generateUUID();

        Event event = new Event()
                .setTopic(CommonStatus.TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");

        eventProducer.dealEvent(event);

        Map map = new HashMap();
        //map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        //七牛云路径
        map.put("shareUrl", shareBucketUrl + "/" + fileName);

        return CommonUtil.getJSONString(0, null, map);
    }

    /**
     * 分享生成图片的查看 （由于生成图片转存到七牛云， 已废弃）
     * @param fileName
     * @param response
     */
    //@RequestMapping(value = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("图片名不能为空！");
        }
        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try (OutputStream ops = response.getOutputStream();
             FileInputStream fis = new FileInputStream(file)
        ) {
            byte[] bytes = new byte[1024];
            int b=0;
            while ((b=fis.read(bytes))!= -1){
                ops.write(bytes, 0, b);
            }
        } catch (IOException e) {
            logger.error("图片输出失败：" + e.getMessage());
        }
    }
}
