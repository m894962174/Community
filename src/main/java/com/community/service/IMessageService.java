package com.community.service;

import com.baomidou.mybatisplus.service.IService;
import com.community.vo.Message;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/20/13:53
 * @Description:
 */
public interface IMessageService extends IService<Message> {

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId
     * @return
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询未读私信的数量
     * @param userId
     * @param conversationId
     * @return
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * 插入一条新私信
     * @param message
     */
    void insertMessage(Message message);

}
