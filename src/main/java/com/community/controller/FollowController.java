package com.community.controller;

import com.community.service.impl.FollowService;
import com.community.service.impl.UserService;
import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.Page;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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

    @Autowired
    UserService userService;

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
//    @CheckLogin
    public String follow(int entityType, int entityId) {
        followService.follow(UserThreadLocal.getUser().getId(), entityType, entityId);
        return CommonUtil.getJSONString(0, "已关注!");
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
//    @CheckLogin
    public String unFollow(int entityType, int entityId) {
        followService.unFollow(UserThreadLocal.getUser().getId(), entityType, entityId);
        return CommonUtil.getJSONString(0, "已取消关注!");
    }

    /**
     * 用户的关注列表
     *
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.selectUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        page.setPath("/followees" + userId);
        page.setLimit(5);
        page.setRows((int) followService.getFolloweeCount(userId, CommonStatus.ENTITY_TYPE_USER));
        List<Map<String, Object>> list = followService.getFollowees(userId, page.getOffset(), page.getLimit());
        if (list != null){
            for(Map map : list) {
                User u = (User) map.get("user");
                map.put("hasFollowed", this.hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", list);
        return "/site/followee";
    }

    /**
     * 用户的粉丝列表
     *
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.selectUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        page.setPath("/followers" + userId);
        page.setLimit(5);
        page.setRows((int) followService.getFollowerCount(CommonStatus.ENTITY_TYPE_USER, userId));
        List<Map<String, Object>> list = followService.getFollowers(userId, page.getOffset(), page.getLimit());
        if (list != null){
            for(Map map : list) {
                User u = (User) map.get("user");
                map.put("hasFollowed", this.hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", list);
        return "/site/follower";
    }

    /**
     * 当前用户是否关注此用户
     * @param userId
     * @return
     */
    private boolean hasFollowed(int userId) {
        if (UserThreadLocal.getUser() == null) {
            return false;
        }
        return followService.hasFollow(UserThreadLocal.getUser().getId(), CommonStatus.ENTITY_TYPE_USER, userId);
    }
}
