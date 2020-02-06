package com.community.config;

import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/03/18:00
 * @Description:
 */
@Configuration
public class SerurityConfig extends WebSecurityConfigurerAdapter {


    //配置不需要过滤的路径
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    //配置过滤需要的授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //需要有授权的请求路径
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/user/upload/changePassWord",
                        "/addDiscussPost",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        CommonStatus.AUTHORITY_ADMIN,
                        CommonStatus.AUTHORITY_MODERATOR,
                        CommonStatus.AUTHORITY_USER
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful",
                        "/data/**"
                ).hasAnyAuthority(
                        CommonStatus.AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete"
                )
                .hasAnyAuthority(
                        CommonStatus.AUTHORITY_ADMIN,
                        CommonStatus.AUTHORITY_MODERATOR
                )
                //除以上路径外，不需授权，放行
                .anyRequest().permitAll()
                //暂时关闭生成csrf凭证
                .and().csrf().disable();
        //权限不够时的处理
        http.exceptionHandling()
                //未登录的处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //是否异步请求
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            response.getWriter().write(CommonUtil.getJSONString(403, "您还未登录！"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                //权限不足的处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //是否异步请求
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            response.getWriter().write(CommonUtil.getJSONString(403, "权限不足！"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        //security底层会自己拦截/logout请求，要想使用自己的登出逻辑，需覆盖其默认路径(此路径不存在，仅为覆盖)
        http.logout()
                .logoutUrl("/securityLogout");
    }
}
