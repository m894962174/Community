package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.controller.filter.SensitiveWordFilter;
import com.community.mapper.DiscussPostMapper;
import com.community.service.IDiscussPostService;
import com.community.vo.DiscussPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;


@Service
public class DiscussPostService extends ServiceImpl<DiscussPostMapper, DiscussPost> implements IDiscussPostService {

    private static final Logger log= LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    SensitiveWordFilter sensitiveWordFilter;

    /**
     * 社区首页帖子展示
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<DiscussPost> listDiscussPosts(int userId, int offset, int limit) {
        return this.baseMapper.selectDiscussPosts(userId,offset,limit);
    }


    /**
     * 获取未被拉黑的帖子数
     * @param userId
     * @return
     */
    public int getCount(int userId){
        if(userId==0){
            return this.baseMapper.selectCount(new EntityWrapper<DiscussPost>().ne("status",2));
        }else{
            return this.baseMapper.selectCount(new EntityWrapper<DiscussPost>().eq("user_id",userId).ne("status",2));
        }
    }

    /**
     * 发布新帖
     * @param discussPost
     */
    @Override
    public void add(DiscussPost discussPost) {
        //转义可能存在的html标签
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //敏感词过滤
        discussPost.setTitle(sensitiveWordFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveWordFilter.filter(discussPost.getContent()));
        this.insert(discussPost);
    }

    /**
     * 帖子详情
     * @param discussPostId
     * @return
     */
    @Override
    public DiscussPost selectDisCussPostById(int discussPostId) {
        return this.selectById(discussPostId);
    }

    /**
     * 修改CommentCount
     * @param
     */
    @Override
    public void update(int commentCount, int id) {
        this.baseMapper.updateCommentCount(commentCount, id);
    }

    public int updateType(int id, int type){
        return this.baseMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status){
        return this.baseMapper.updateStatus(id, status);
    }
}
