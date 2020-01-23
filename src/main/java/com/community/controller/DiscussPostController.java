package com.community.controller;

import com.community.annotation.CheckLogin;
import com.community.service.ICommentService;
import com.community.service.IDiscussPostService;
import com.community.service.IUserService;
import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.Comment;
import com.community.vo.DiscussPost;
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


@Controller
public class DiscussPostController {

    @Autowired
    IDiscussPostService discussPostService;

    @Autowired
    IUserService userService;

    @Autowired
    ICommentService commentService;


    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        // 方法调用栈,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.getCount(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.listDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.selectUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    /**
     * 发布帖子
     *
     * @param title
     * @param content
     * @return
     */
    @CheckLogin
    @RequestMapping(value = "/addDiscussPost", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = UserThreadLocal.getUser();
        if (user == null) {
            return CommonUtil.getJSONString(403, "你还没有登录哦!");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setStatus(0);
        post.setType(0);
        post.setCommentCount(0);
        discussPostService.add(post);
        return CommonUtil.getJSONString(0, "发布成功!");
    }

    /**
     * 帖子详情
     *
     * @param discussPostId
     * @param model
     * @return
     */
    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String toDiscussPostDetil(@PathVariable int discussPostId, Model model, Page page) {
        DiscussPost discussPost = discussPostService.selectDisCussPostById(discussPostId);
        User user = userService.selectUserById(discussPost.getUserId());
        //当前帖子的信息：标题，正文，时间等
        model.addAttribute("post", discussPost);
        //帖子发布者信息
        model.addAttribute("user", user);
        //设置评论的分页信息
        page.setLimit(5);
        page.setPath("/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());
        //帖子的评论
        List<Comment> comments = commentService.findCommentsByEntity(CommonStatus.ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        //用来放置每条评论及下面所属的回复
        List<Map<String, Object>> commentLists = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> commentVo = new HashMap<>();
                //当前评论信息
                commentVo.put("comment", comment);
                //当前评论的作者信息
                commentVo.put("user", userService.selectUserById(comment.getUserId()));
                //当前评论的回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(CommonStatus.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 用来放置当前评论的相关回复信息
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList!=null){
                    for(Comment reply:replyList){
                        Map<String,Object> replyVo = new HashMap();
                        //当前回复
                        replyVo.put("reply",reply);
                        //当前回复的作者
                        replyVo.put("user",userService.selectUserById(reply.getUserId()));
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                //此条评论的回复总数
                int replyCount = commentService.getCommentCount(CommonStatus.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVo.put("replys",replyVoList);
                commentLists.add(commentVo);
            }
            model.addAttribute("comments", commentLists);
        }
        return "/site/discuss-detail";
    }

    /**
     * 配合GloableExceptionHandler内方法的路径映射
     * @return
     */
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

}
