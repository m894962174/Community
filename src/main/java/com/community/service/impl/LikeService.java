package com.community.service.impl;

import com.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
     * @param entityUserId
     */
    public void like(int userId, int entityUserId, int entityType, int entityId) {
        String ekey = RedisUtil.generateRedisKey(entityType, entityId);
        String ukey = RedisUtil.generateLikeUserKey(entityUserId);
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                boolean ismember = redisTemplate.opsForSet().isMember(ekey, userId);
                operations.multi();
                if (ismember) {   //已被点赞:则取消赞
                    redisTemplate.opsForSet().remove(ekey, userId);
                    //用户所获得的赞-1
                    redisTemplate.opsForValue().decrement(ukey);
                } else {
                    redisTemplate.opsForSet().add(ekey, userId);
                    redisTemplate.opsForValue().increment(ukey);
                }
                return operations.exec();
            }
        });
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
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int getLikeStatus(int userId, int entityType, int entityId) {
        String key = RedisUtil.generateRedisKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? 1 : 0;
    }

    /**
     * 查询当前用户获赞数
     * @param userId
     * @return
     */
    public int getLikeUserCount(int userId){
        String ukey = RedisUtil.generateLikeUserKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(ukey);
        return count==null? 0 : count.intValue();
    }
}
