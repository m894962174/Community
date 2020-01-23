package com.community.controller.exceptionHandler;

import com.community.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/21/16:26
 * @Description:
 */
@ControllerAdvice(annotations = Controller.class)
public class GloableExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(GloableExceptionHandler.class);

    @ExceptionHandler
    public void exceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        //控制台打印
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
        String xRequestedWith = request.getHeader("x-requested-with");
        //如果是异步请求的异常则返回包装的json字符串
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.println(CommonUtil.getJSONString(1, "服务器异常!"));
        } else {
            //否则跳到错误提醒页面
            response.sendRedirect(request.getContextPath() +"/error");
        }
    }
}
