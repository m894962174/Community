package com.community.service.impl;

import com.community.util.CommonStatus;
import com.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Autowired
    UserService userService;

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

    /**
     * 查询用户关注的用户列表
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String, Object>> getFollowees(int userId, int offset, int limit) {
        String eeKey = RedisUtil.generateFollowee(userId, CommonStatus.ENTITY_TYPE_USER);
        Set<Object> set = template.opsForZSet().reverseRange(eeKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        List<Map<String, Object>> followeesList = new ArrayList<>();
        for (Object o : set) {
            Map<String, Object> map = new HashMap<>();
            int targetId = Integer.valueOf(o.toString());
            map.put("user", userService.selectUserById(targetId));
            Double score = template.opsForZSet().score(eeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            followeesList.add(map);
        }
        return followeesList;
    }

    /**
     * 查询用户的粉丝列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String, Object>> getFollowers(int userId, int offset, int limit) {
        String erKey = RedisUtil.generateFollower(CommonStatus.ENTITY_TYPE_USER, userId);
        Set<Object> set = template.opsForZSet().reverseRange(erKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        List<Map<String, Object>> followersList = new ArrayList<>();
        for (Object o : set) {
            Map<String, Object> map = new HashMap<>();
            int targetId = Integer.valueOf(o.toString());
            map.put("user", userService.selectUserById(targetId));
            Double score = template.opsForZSet().score(erKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            followersList.add(map);
        }
        return followersList;
    }
}
