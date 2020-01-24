package com.community.controller;

import com.community.annotation.CheckLogin;
import com.community.service.impl.FollowService;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/24/16:33
 * @Description:
 */
@Controller
public class FollowController {

    @Autowired
    FollowService followService;

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
//    @CheckLogin
    public String follow(int entityType, int entityId){
        followService.follow(UserThreadLocal.getUser().getId(), entityType, entityId);
        return CommonUtil.getJSONString(0, "已关注!");
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
//    @CheckLogin
    public String unFollow(int entityType, int entityId){
        followService.unFollow(UserThreadLocal.getUser().getId(), entityType, entityId);
        return CommonUtil.getJSONString(0, "已取消关注!");
    }
}
