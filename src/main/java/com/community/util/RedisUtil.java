package com.community.util;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/23/16:40
 * @Description:
 */
public class RedisUtil {

    private static final String connectorWord = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_LOGINTICKET = "loginticket";
    private static final String PREFIX_USER = "user";

    /**
     * 某个实体的赞
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String generateRedisKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + connectorWord + entityType + connectorWord + entityId;
    }

    /**
     * 某个用户收到的所有的赞
     *
     * @param userId
     * @return
     */
    public static String generateLikeUserKey(int userId) {
        return PREFIX_USER_LIKE + connectorWord + userId;
    }

    /**
     * 当前用户关注的实体
     *
     * @param userId
     * @param entityType
     * @return
     */
    public static String generateFollowee(int userId, int entityType) {
        return PREFIX_FOLLOWEE + connectorWord + userId + connectorWord + entityType;
    }

    /**
     * 当前实体被哪些用户关注
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String generateFollower(int entityType, int entityId) {
        return PREFIX_FOLLOWER + connectorWord + entityType + connectorWord + entityId;
    }

    public static String generateKaptchaKey(String cookieOwner) {
        return PREFIX_KAPTCHA + connectorWord + cookieOwner;
    }

    public static String generateLoginTicket(String ticket) {
        return PREFIX_LOGINTICKET + connectorWord + ticket;
    }

    public static String generateUserKey(int userId){
        return  PREFIX_USER + connectorWord + userId;
    }
}
