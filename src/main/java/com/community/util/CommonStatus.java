package com.community.util;

public interface CommonStatus {

    /**
     * 激活成功
     */
    static int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    static int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    static int ACTIVATION_FAILURE = 2;

    /**
     * 用户不存在
     */
    static int USER_NOTEXIST = -1;

    /**
     * 勾选'记住我'时的凭证存活时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 10;

    /**
     * 未勾选'记住我'时的凭证存活时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 24;

    /**
     * 实体类型: 帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型: 用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题: 评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题: 关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;
}
