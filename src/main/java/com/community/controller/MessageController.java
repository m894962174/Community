package com.community.controller;

import com.community.service.IUserService;
import com.community.service.impl.MessageService;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.Message;
import com.community.vo.Page;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/20/14:25
 * @Description:
 */
@Controller
@RequestMapping("/letter")
public class MessageController {

    //此处不用IMessageService接口接收，通常用接口接收是考虑到可能会有对于接口的多种实现，体现了程序的可扩展性，
    // 此工程可能不太会有这种情形，因此以后都采用Impl接收的方式，便于mybatis-plus2.0的使用
    @Autowired
    MessageService messageService;

    @Autowired
    IUserService userService;


    /**
     * 会话列表
     *
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = UserThreadLocal.getUser();
        page.setPath("/letter/list");
        page.setLimit(10);
        page.setRows(messageService.selectConversationsCount(user.getId()));
        List<Message> conversationList = messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.selectLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.selectLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getToId() ? message.getFromId() : message.getToId();
                map.put("target", userService.selectUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        return "/site/letter";
    }

    /**
     * 某会话的私信列表
     *
     * @param model
     * @param conversationId
     * @param page
     * @return
     */
    @RequestMapping("/detail/{conversationId}")
    public String toMessageDetail(Model model, @PathVariable("conversationId") String conversationId, Page page) {
        User user = UserThreadLocal.getUser();
        page.setPath("/letter/detail" + conversationId);
        page.setLimit(10);
        page.setRows(messageService.selectLetterCount(conversationId));
        List<Message> messageList = messageService.selectConversationLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> lettersList = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap();
                map.put("letter", message);
                map.put("fromUser", userService.selectUserById(message.getFromId()));
                lettersList.add(map);
            }
            //当前会话中私信列表的未读私信ID
            List<Integer> unReadIds = messageService.getUnReadLetterIds(messageList);
            //将其变未已读状态
            if (!unReadIds.isEmpty()) {
                messageService.updateStatus(unReadIds, 1);
            }
            // 私信目标
            model.addAttribute("target", this.getLetterTarget(conversationId));
        }
        model.addAttribute("letters", lettersList);
        return "/site/letter-detail";
    }

    /**
     * 发送私信
     *
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.selectUserByUserName(toName);
        if (target == null) {
            return CommonUtil.getJSONString(1, "目标用户不存在!");
        }
        Message message = new Message();
        message.setFromId(UserThreadLocal.getUser().getId());
        message.setToId(target.getId());
        //ConversationId数据组成规则
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.insertMessage(message);
        //成功发送:     这里status未设置
        return CommonUtil.getJSONString(0);
    }

    /**
     * 获取当前私信的对象
     *
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (UserThreadLocal.getUser().getId() == id0) {
            return userService.selectUserById(id1);
        } else {
            return userService.selectUserById(id0);
        }
    }
}
