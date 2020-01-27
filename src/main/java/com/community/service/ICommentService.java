package com.community.service;

import com.baomidou.mybatisplus.service.IService;
import com.community.vo.Comment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/19/13:59
 * @Description:
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 分页查询
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 评论总数
     * @param entityType
     * @param entityId
     * @return
     */
    int getCommentCount(int entityType, int entityId);

    /**
     * 添加评论
     * @param comment
     */
    void  insertComment(Comment comment);

    /**
     * id 查询
     * @param id
     * @return
     */
    Comment selectCommentById(int id);
}
