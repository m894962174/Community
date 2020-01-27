package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.controller.filter.SensitiveWordFilter;
import com.community.mapper.CommentMapper;
import com.community.mapper.DiscussPostMapper;
import com.community.service.ICommentService;
import com.community.util.CommonStatus;
import com.community.vo.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/19/13:59
 * @Description:
 */

@Service
public class CommentService extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveWordFilter sensitiveWordFilter;

    /**
     * 分页查询
     *
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return this.baseMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 评论总数
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public int getCommentCount(int entityType, int entityId) {
        return this.baseMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 添加评论
     *
     * @param comment
     */
    @Override
    @Transactional
    public void insertComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveWordFilter.filter(comment.getContent()));
        this.insert(comment);
        //只有给帖子评论时才跟新
        if (comment.getEntityType() == CommonStatus.ENTITY_TYPE_POST) {
            //统计评论数量
            int count = this.selectCount(new EntityWrapper<Comment>().eq("entity_id", comment.getEntityId()).eq("entity_type", comment.getEntityType()));
            //更新DiscussPost中的commentCount
            discussPostMapper.updateCommentCount(count, comment.getEntityId());
        }
    }

    /**
     * 根据Id获取comment实体
     * @param id
     * @return
     */
    @Override
    public Comment selectCommentById(int id) {
        return this.selectById(id);
    }
}
