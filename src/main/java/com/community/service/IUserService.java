package com.community.service;

import com.baomidou.mybatisplus.service.IService;
import com.community.vo.User;

import java.util.Map;

public interface IUserService extends IService<User> {

    /**
     * 根据id查询User
     *
     * @param id
     * @return
     */
    User selectUserById(int id);

    /**
     * 根据userName查询User
     *
     * @param userName
     * @return
     */
    User selectUserByUserName(String userName);

    /**
     * 注册新用户
     *
     * @param user
     * @return
     */
    Map<String, Object> register(User user);

    /**
     * 激活账号
     *
     * @param userId
     * @param activtionCode
     * @return
     */
    int activtion(int userId, String activtionCode);

    /**
     * 根据某参数获取User
     *
     * @param param
     * @return
     */
    User selectUserByParam(String param);

    /**
     * 修改User
     *
     * @param user
     */
    void updateUserHeaderUrl(User user);

    /**
     * 修改密码
     *
     * @param OldPassWord
     * @param passWord
     */
    Map<String, Object> updatePassWord(String OldPassWord, String passWord, String checkPassWord);
}
