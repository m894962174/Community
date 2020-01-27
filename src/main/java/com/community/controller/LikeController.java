package com.community.controller;

import com.community.event.EventProducer;
import com.community.service.impl.LikeService;
import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.Event;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/23/17:00
 * @Description:
 */
@Controller
public class LikeController {

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;


    /**
     * 异步点赞
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = UserThreadLocal.getUser();
        likeService.like(user.getId(), entityUserId, entityType, entityId);
        Map<String,Object> map=new HashMap();
        int likeStatus = likeService.getLikeStatus(user.getId(), entityType, entityId);
        map.put("likeStatus", likeStatus);
        map.put("likeCount", likeService.getLikeCount(entityType, entityId));

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(CommonStatus.TOPIC_LIKE)
                    .setUserId(UserThreadLocal.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.dealEvent(event);
        }
        return CommonUtil.getJSONString(0,null,map);
    }
}
