package com.community.controller.interceptor;

import com.community.service.impl.LoginTicketService;
import com.community.service.impl.UserService;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.LoginTicket;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/15/17:19
 * @Description:
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginTicketService loginTicketService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CommonUtil.getCookiesValue(request, "ticket");
        LoginTicket loginTicket = loginTicketService.selectLoginTicketByTicket(ticket);
        if (ticket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
            User user = userService.selectUserByParam(ticket);
            UserThreadLocal.setUser(user);
            //将用户凭证等级存入SecurityContext,便于Security完成授权
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(user, user.getPassword(), userService.getAuthorities(user.getId()));
            SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.clear();
        SecurityContextHolder.clearContext();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = UserThreadLocal.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }
}
