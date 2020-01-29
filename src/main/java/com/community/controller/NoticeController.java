package com.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.community.service.impl.MessageService;
import com.community.service.impl.UserService;
import com.community.util.CommonStatus;
import com.community.util.UserThreadLocal;
import com.community.vo.Message;
import com.community.vo.Page;
import com.community.vo.User;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/28/16:59
 * @Description:
 */
@Controller
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;


    /**
     * 系统通知页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String toNoticeList(Model model) {
        User user = UserThreadLocal.getUser();
        //评论事件
        Message message = messageService.selectNewNotice(user.getId(), CommonStatus.TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);
            //处理content，便于获取事件信息
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.selectUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            int count = messageService.selectNoticeCount(user.getId(), CommonStatus.TOPIC_COMMENT);
            messageVo.put("count", count);
            count = messageService.selectNoticUnReadCount(user.getId(), CommonStatus.TOPIC_COMMENT);
            messageVo.put("unread", count);
        }
        model.addAttribute("commentNotice", messageVo);
        //点赞事件
        message = messageService.selectNewNotice(user.getId(), CommonStatus.TOPIC_LIKE);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);
            //处理content，便于获取事件信息
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.selectUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            int count = messageService.selectNoticeCount(user.getId(), CommonStatus.TOPIC_LIKE);
            messageVo.put("count", count);
            count = messageService.selectNoticUnReadCount(user.getId(), CommonStatus.TOPIC_LIKE);
            messageVo.put("unread", count);
        }
        model.addAttribute("likeNotice", messageVo);
        //关注事件
        message = messageService.selectNewNotice(user.getId(), CommonStatus.TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);
            //处理content，便于获取事件信息
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.selectUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            int count = messageService.selectNoticeCount(user.getId(), CommonStatus.TOPIC_FOLLOW);
            messageVo.put("count", count);
            count = messageService.selectNoticUnReadCount(user.getId(), CommonStatus.TOPIC_FOLLOW);
            messageVo.put("unread", count);
        }
        model.addAttribute("followNotice", messageVo);

        // 查询未读消息数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticUnReadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/notice";
    }

    /**
     * 系统通知详情页面
     * @param topic
     * @param page
     * @param model
     * @return
     */
    @RequestMapping("/detail/{topic}")
    public String toNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = UserThreadLocal.getUser();
        page.setPath("/notice/detail/" + topic);
        page.setLimit(10);
        page.setRows(messageService.selectNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.getNoticeMessages(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> messageVo = new ArrayList<>();
        if (noticeList != null) {
            for(Message notice : noticeList){
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.selectUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 事件产生源头
                map.put("fromUser", userService.selectUserById(notice.getFromId()));
                messageVo.add(map);
            }
        }
        model.addAttribute("notices", messageVo);

        //变换阅读状态
        List<Integer> ids = messageService.getUnReadLetterIds(noticeList);
        if(!ids.isEmpty()) {
            messageService.updateStatus(ids, 1);
        }
        return "/site/notice-detail";
    }

}
