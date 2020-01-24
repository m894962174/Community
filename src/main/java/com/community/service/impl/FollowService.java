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
 * @Date: 2020/01/24/16:05
 * @Description:
 */
@Service
public class FollowService {

    @Autowired
    RedisTemplate<String, Object> template;

    /**
     * 关注
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId) {
        template.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String eeKey = RedisUtil.generateFollowee(userId, entityType);
                String erKey = RedisUtil.generateFollower(entityType, entityId);
                template.multi();
                template.opsForZSet().add(eeKey, entityId, System.currentTimeMillis());
                template.opsForZSet().add(erKey, userId, System.currentTimeMillis());
                return template.exec();
            }
        });
    }

    /**
     * 取消关注
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unFollow(int userId, int entityType, int entityId) {
        template.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String eeKey = RedisUtil.generateFollowee(userId, entityType);
                String erKey = RedisUtil.generateFollower(entityType, entityId);
                template.multi();
                template.opsForZSet().remove(eeKey, entityId);
                template.opsForZSet().remove(erKey, userId);
                return template.exec();
            }
        });
    }

    /**
     * 当前用户关注总数
     *
     * @return
     */
    public long getFolloweeCount(int userId, int entityType) {
        String eeKey = RedisUtil.generateFollowee(userId, entityType);
        return template.opsForZSet().zCard(eeKey);
    }

    /**
     * 当前实体被关注总数
     *
     * @return
     */
    public long getFollowerCount(int entityType, int entityId) {
        String erKey = RedisUtil.generateFollower(entityType, entityId);
        return template.opsForZSet().zCard(erKey);
    }

    /**
     * 当前用户是否关注此实体
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollow(int userId, int entityType, int entityId) {
        String eeKey = RedisUtil.generateFollowee(userId, entityType);
        return template.opsForZSet().score(eeKey, entityId) != null;
    }
}
