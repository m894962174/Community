package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.controller.filter.SensitiveWordFilter;
import com.community.mapper.DiscussPostMapper;
import com.community.service.IDiscussPostService;
import com.community.vo.DiscussPost;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class DiscussPostService extends ServiceImpl<DiscussPostMapper, DiscussPost> implements IDiscussPostService {

    private static final Logger log = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer, Integer> postCountCache;


    @PostConstruct
    private void initCache() {
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String s) throws Exception {
                        if (s == null || StringUtils.isBlank(s)) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] params = s.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        log.debug("初始化缓存， 从DB中获取数据...");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        //初始化帖子总数
        postCountCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer integer) throws Exception {
                        log.debug("初始化缓存， 从DB中获取数据...");
                        return discussPostMapper.selectCount(new EntityWrapper<DiscussPost>().ne("status", 2));
                    }
                });
    }

    /**
     * 社区首页帖子展示
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<DiscussPost> listDiscussPosts(int userId, int offset, int limit, int orderMode) {
        //先尝试从本地缓存caffeine取值
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        //二级缓存 ： 未加
        log.debug("从DB中获取数据...");
        return this.baseMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }


    /**
     * 获取未被拉黑的帖子数
     *
     * @param userId
     * @return
     */
    public int getCount(int userId) {
        if (userId == 0) { //仅对统计全部帖子数加本地缓存
            return this.postCountCache.get(0);
        }
        log.debug("从DB中获取数据...");
        if (userId == 0) {
            return this.baseMapper.selectCount(new EntityWrapper<DiscussPost>().ne("status", 2));
        } else {
            return this.baseMapper.selectCount(new EntityWrapper<DiscussPost>().eq("user_id", userId).ne("status", 2));
        }
    }

    /**
     * 发布新帖
     *
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
     *
     * @param discussPostId
     * @return
     */
    @Override
    public DiscussPost selectDisCussPostById(int discussPostId) {
        return this.selectById(discussPostId);
    }

    /**
     * 修改CommentCount
     *
     * @param
     */
    @Override
    public void update(int commentCount, int id) {
        this.baseMapper.updateCommentCount(commentCount, id);
    }

    public int updateType(int id, int type) {
        return this.baseMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return this.baseMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return this.baseMapper.updateScore(id, score);
    }
}
