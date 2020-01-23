package com.community.service;

import com.baomidou.mybatisplus.service.IService;
import com.community.vo.LoginTicket;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/14/16:58
 * @Description:
 */
public interface ILoginTicketService extends IService<LoginTicket> {

    /**
     * 生成登录凭证
     * @param username
     * @param password
     * @param expiredSeconds
     */
    Map<String,Object> generateLoginTicket(String username, String password, int expiredSeconds);

    /**
     * 更改登录凭证状态
     * @param ticket
     * @return
     */
    void updateLoginTicketStatus(String ticket);

    /**
     * 根据ticket获取LT
     * @param ticket
     * @return
     */
    LoginTicket selectLoginTicketByTicket(String ticket);
}
