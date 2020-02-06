package com.community.controller.interceptor;

import com.community.service.impl.DataStatisticsService;
import com.community.util.UserThreadLocal;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/06/12:12
 * @Description:
 */
@Component
public class StatisticsDataInterceptor implements HandlerInterceptor {

    @Autowired
    DataStatisticsService dataStatisticsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        dataStatisticsService.addUVData(ip);

        User user = UserThreadLocal.getUser();
        if (user != null) {
            dataStatisticsService.addDAUData(user.getId());
        }
        return true;
    }
}
