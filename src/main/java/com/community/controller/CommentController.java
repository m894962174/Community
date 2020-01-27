package com.community.controller;

import com.community.event.EventProducer;
import com.community.service.ICommentService;
import com.community.service.impl.DiscussPostService;
import com.community.util.CommonStatus;
import com.community.util.UserThreadLocal;
import com.community.vo.Comment;
import com.community.vo.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/19/19:25
 * @Description:
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    ICommentService commentService;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    EventProducer producer;


    /**
     * 新增评论
     *
     * @param discussPostId
     * @param comment
     * @return
     */
    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setCreateTime(new Date());
        comment.setUserId(UserThreadLocal.getUser().getId());
        comment.setStatus(0);
        commentService.insertComment(comment);

        Event event = new Event()
                .setUserId(UserThreadLocal.getUser().getId())
                .setTopic(CommonStatus.TOPIC_COMMENT)
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        if(comment.getEntityType() == CommonStatus.ENTITY_TYPE_POST) {
            event.setEntityUserId(discussPostService.selectDisCussPostById(comment.getEntityId()).getUserId());
        }else if(comment.getEntityType() == CommonStatus.ENTITY_TYPE_COMMENT) {
            event.setEntityUserId(commentService.selectCommentById(comment.getEntityId()).getUserId());
        }
        producer.dealEvent(event);
        return "redirect:/detail/" + discussPostId;
    }
}
