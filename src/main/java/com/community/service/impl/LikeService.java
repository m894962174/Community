package com.community.service.impl;

import com.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/23/16:45
 * @Description:
 */
@Service
public class LikeService {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     *
     * @param entityType
     * @param entityId
     * @param userId
     */
    public void like(int userId, int entityType, int entityId) {
        String key = RedisUtil.generateRedisKey(entityType, entityId);
        //点过赞的再点会被移除
        if (redisTemplate.opsForSet().isMember(key, userId)) {
            redisTemplate.opsForSet().remove(key, userId);
        } else {
            redisTemplate.opsForSet().add(key, userId);
        }
    }

    /**
     * 查询实体获赞总数
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public long getLikeCount(int entityType, int entityId) {
        String key = RedisUtil.generateRedisKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 查询当前用户点赞状态： 1：已赞； 2：未赞
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int getLikeStatus(int userId, int entityType, int entityId) {
        String key = RedisUtil.generateRedisKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? 1 : 0;
    }
}
