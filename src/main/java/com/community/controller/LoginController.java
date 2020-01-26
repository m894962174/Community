package com.community.controller;

import com.community.service.impl.LoginTicketService;
import com.community.service.impl.UserService;
import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import com.community.util.RedisUtil;
import com.community.vo.User;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private Producer producer;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginTicketService loginTicketService;

    @Autowired
    RedisTemplate<String, Object> template;

    /**
     * 跳转注册页面
     *
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String toRegisterPage() {
        return "/site/register";
    }

    /**
     * 跳转登录页面
     *
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 注册
     *
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model, @Validated User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已向您的邮箱发送了一封激活邮件，请尽快前往激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("user", user);
            return "/site/register";
        }
    }

    /**
     * 激活账号
     *
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable int userId, @PathVariable String code) {
        int result = userService.activtion(userId, code);
        if (result == CommonStatus.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == CommonStatus.ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已被激活过了!");
            model.addAttribute("target", "/index");
        } else if (result == CommonStatus.USER_NOTEXIST) {
            model.addAttribute("msg", "激活失败,用户不存在!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 后台生成验证码，并存入session(已重构：存入redis)
     *
     * @param response
     */
    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*HttpSession session*/) {
        String text = producer.createText();
        //session.setAttribute("kaptcha", text);
        //生成随机字符串存入cookie标识验证码
        String kaptchaOwner = CommonUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setPath(contextPath);
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        //验证码存入redis
        String kaptchaKey = RedisUtil.generateKaptchaKey(kaptchaOwner);
        template.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);
        BufferedImage image = producer.createImage(text);
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("验证码生成错误：" + e.getMessage());
        }
    }

    /**
     * 登录时生成LoginTicket
     *
     * @param username
     * @param password
     * @param code
     * @param rememberme
     * @param model
     * @param session
     * @param response
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@Validated String username, @CookieValue("kaptchaOwner") String kaptchaOwner, String password, String code, boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response) {
        // 检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (kaptchaOwner != null) {
            String kaptcheKey = RedisUtil.generateKaptchaKey(kaptchaOwner);
            kaptcha = (String) template.opsForValue().get(kaptcheKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }
        // 检查账号,密码
        int expiredSeconds = rememberme ? CommonStatus.REMEMBER_EXPIRED_SECONDS : CommonStatus.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = loginTicketService.generateLoginTicket(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue String ticket) {
        loginTicketService.updateLoginTicketStatus(ticket);
        return "redirect:/login";
    }
}
