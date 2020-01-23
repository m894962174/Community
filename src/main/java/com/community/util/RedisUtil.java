package com.community.util;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/23/16:40
 * @Description:
 */
public class RedisUtil {

    private static  final String  connectorWord = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    public static  String generateRedisKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE+connectorWord+entityType+connectorWord+entityId;
    }
}
