package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.mapper.LoginTicketMapper;
import com.community.service.ILoginTicketService;
import com.community.util.CommonUtil;
import com.community.vo.LoginTicket;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/14/17:04
 * @Description:
 */

@Service
public class LoginTicketService extends ServiceImpl<LoginTicketMapper, LoginTicket> implements ILoginTicketService {

    @Autowired
    UserService userService;


    /**
     * 生成登录凭证
     *
     * @param username
     * @param password
     * @param expiredSeconds
     */
    @Override
    public Map<String,Object> generateLoginTicket(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        User user = userService.selectUserByUserName(username);
        if(user==null){
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }
        String passWord = CommonUtil.md5(password + user.getSalt());
        if (passWord.equals(user.getPassword())) {    //验证通过
            LoginTicket loginTicket = new LoginTicket();
            loginTicket.setUserId(user.getId());
            loginTicket.setTicket(CommonUtil.generateUUID());
            loginTicket.setStatus(0);
            loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
            this.baseMapper.insert(loginTicket);
            map.put("ticket", loginTicket.getTicket());
            return map;
        }else {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
    }

    /**
     * 让登录凭证失效; status:1
     * @param ticket
     * @return
     */
    @Override
    public void updateLoginTicketStatus(String ticket) {
        LoginTicket loginTicket=this.selectOne(new EntityWrapper<LoginTicket>().eq("ticket",ticket));
        loginTicket.setStatus(1);
        this.update(loginTicket,new EntityWrapper<LoginTicket>().eq("ticket",ticket));
    }

    /**
     * 根据ticket获取LT
     * @param ticket
     * @return
     */
    @Override
    public LoginTicket selectLoginTicketByTicket(String ticket) {
        return this.selectOne(new EntityWrapper<LoginTicket>().eq("ticket",ticket));
    }

}
