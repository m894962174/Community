package com.community.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.community.vo.Message;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/20/13:45
 * @Description:
 */
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 分页查询当前用户下的所有会话
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * 查询当前用户的会话总数
     * @param userId
     * @return
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话所包含的私信列表.
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * 修改消息的状态
     * @param ids
     * @param status
     * @return
     */
    int updateStatus(List<Integer> ids, int status);

}
