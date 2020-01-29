package com.community.controller.interceptor;

import com.community.service.impl.MessageService;
import com.community.util.UserThreadLocal;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/29/20:42
 * @Description:
 */
@Component
public class MessageCountInterceptor implements HandlerInterceptor {

    @Autowired
    MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = UserThreadLocal.getUser();
        if (user != null && modelAndView != null) {
            int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.selectNoticUnReadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
