package com.community.controller.interceptor;

import com.community.annotation.CheckLogin;
import com.community.util.UserThreadLocal;
import com.community.vo.User;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/16/16:54
 * @Description:
 */
@Component
@Deprecated
public class CheckLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod =(HandlerMethod) handler;
            Method method=handlerMethod.getMethod();
            CheckLogin checkLogin=method.getAnnotation(CheckLogin.class);
            User user= UserThreadLocal.getUser();
            if(checkLogin!=null && user==null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
