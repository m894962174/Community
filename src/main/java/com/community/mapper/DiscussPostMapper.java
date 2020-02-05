package com.community.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.community.vo.DiscussPost;

import java.util.List;

public interface DiscussPostMapper extends BaseMapper<DiscussPost> {

    /**
     * 获取帖子列表
     * @param userId（可根据userID筛选）
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * 修改commentCount
     * @param commentCount
     * @param id
     */
    void updateCommentCount(int commentCount, int id);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

}
