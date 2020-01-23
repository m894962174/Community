package com.community.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.community.vo.Comment;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询评论
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 根据评论类型，ID查询总数
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(int entityType, int entityId);

}
