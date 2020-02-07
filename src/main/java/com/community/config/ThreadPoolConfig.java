package com.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/07/10:19
 * @Description:
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
