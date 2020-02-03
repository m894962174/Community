package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.mapper.UserMapper;
import com.community.service.IUserService;
import com.community.util.*;
import com.community.vo.LoginTicket;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private LoginTicketService loginTicketService;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    RedisTemplate<String, Object> template;


    /**
     * 查询User
     *
     * @param id
     * @return
     */
    @Override
    public User selectUserById(int id) {
        User user = this.getRedisUser(id);
        if (user == null) {
            user = this.initRedisUser(id);
        }
        return user;
    }


    @Override
    public User selectUserByUserName(String userName) {
        return this.selectOne(new EntityWrapper<User>().eq("username", userName));
    }

    /**
     * 注册新用户
     * BUG:邮件地址不存在怎么办？ && jsr303校验
     *
     * @param user
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //唯一性校验
        User u = this.selectOne(new EntityWrapper<User>().eq("username", user.getUsername()));
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }
        u = this.selectOne(new EntityWrapper<User>().eq("email", user.getEmail()));
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }
        //添加新用户
        user.setSalt(CommonUtil.generateUUID().substring(0, 5));
        user.setPassword(CommonUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommonUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        this.baseMapper.insert(user);
        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //模板解析后的页面当作邮件正文
        mailClient.sendMail(user.getEmail(), "请激活您的账号", templateEngine.process("/mail/activation", context));
        return map;
    }

    /**
     * 激活账号
     *
     * @param userId
     * @param activtionCode
     * @return
     */
    @Override
    public int activtion(int userId, String activtionCode) {
        User user = this.selectUserById(userId);
        if (user == null) {
            return CommonStatus.USER_NOTEXIST;
        } else {
            if (user.getStatus() == 1) {
                return CommonStatus.ACTIVATION_REPEAT;
            } else if (user.getActivationCode().equals(activtionCode)) {
                user.setStatus(1);
                this.updateById(user);
                this.clearRedisUser(userId);
                return CommonStatus.ACTIVATION_SUCCESS;
            } else {
                return CommonStatus.ACTIVATION_FAILURE;
            }
        }
    }

    /**
     * 根据ticket获取User
     *
     * @param param
     * @return
     */
    @Override
    public User selectUserByParam(String param) {
        LoginTicket loginTicket = loginTicketService.selectLoginTicketByTicket(param);
        return this.selectUserById(loginTicket.getUserId());
    }

    /**
     * 修改HeaderUrl
     *
     * @param user
     */
    @Override
    public void updateUserHeaderUrl(User user) {
        this.updateById(user);
        this.clearRedisUser(user.getId());
    }

    /**
     * 修改密码
     *
     * @param OldPassWord
     * @param passWord
     */
    @Override
    public Map<String, Object> updatePassWord(String OldPassWord, String passWord, String checkPassWord) {
        Map<String, Object> map = new HashMap<>();
        if (OldPassWord == null || OldPassWord.equals("") || passWord == null || passWord.equals("")) {
            map.put("errorMsg", "密码不能为空！");
            return map;
        }
        User user = UserThreadLocal.getUser();
        String opwd = CommonUtil.md5(OldPassWord + user.getSalt());
        if (!opwd.equals(user.getPassword())) {
            map.put("errorMsg", "旧密码错误！");
            return map;
        }
        if (!checkPassWord.equals(passWord)) {
            map.put("checkMsg", "前后俩次输入不一致");
            return map;
        }
        user.setPassword(CommonUtil.md5(passWord + user.getSalt()));
        this.updateById(user);
        this.clearRedisUser(user.getId());
        return null;
    }

    /**
     * 优先从redis缓存中取值
     *
     * @param userId
     * @return
     */
    private User getRedisUser(int userId) {
        String userKey = RedisUtil.generateUserKey(userId);
        return (User) template.opsForValue().get(userKey);
    }

    /**
     * 取不到时，初始化redis缓存
     *
     * @param userId
     */
    private User initRedisUser(int userId) {
        String userKey = RedisUtil.generateUserKey(userId);
        User user = this.selectById(userId);
        template.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 对User有修改行为时，清除redis缓存
     *
     * @param userId
     */
    private void clearRedisUser(int userId) {
        String userKey = RedisUtil.generateUserKey(userId);
        template.delete(userKey);
    }

    /**
     * 获取用户凭证等级，供security获取
     *
     * @param userId
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.selectUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return CommonStatus.AUTHORITY_ADMIN;
                    case 2:
                        return CommonStatus.AUTHORITY_MODERATOR;
                    default:
                        return CommonStatus.AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
