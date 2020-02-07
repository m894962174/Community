package com.community.service;

import com.baomidou.mybatisplus.service.IService;
import com.community.vo.DiscussPost;

import java.util.List;


public interface IDiscussPostService extends IService<DiscussPost> {

    /**
     * 社区首页帖子展示
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> listDiscussPosts(int userId, int offset, int limit, int orderMode);

    /**
     * 统计帖子总数
     *
     * @param userId
     * @return
     */
    int getCount(int userId);

    /**
     * 添加帖子
     *
     * @param discussPost
     */
    void add(DiscussPost discussPost);

    /**
     * 根据Id查询DiscussPost
     *
     * @param discussPostId
     * @return
     */
    DiscussPost selectDisCussPostById(int discussPostId);

    /**
     * 修改commentCount
     *
     * @param commentCount
     * @param id
     */
    void update(int commentCount, int id);
}
