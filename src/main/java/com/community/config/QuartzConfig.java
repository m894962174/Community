package com.community.config;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import quartz.PostScoreReflushJob;
import quartz.TestJob;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/07/13:47
 * @Description:
 */
@Configuration
public class QuartzConfig {

    //BeanFactory是为了简化Bean的实例化过程， 封装了其bean的实例化过程，再装配到spring容器中， 将此FactoryBean注入到其他bean中去创建实例

    /**
     * 帖子分数定时刷新任务
     * @return
     */
    @Bean
    public JobDetailFactoryBean setPostScoreReflushJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(PostScoreReflushJob.class);
        jobDetailFactoryBean.setName("postScoreReflush");
        jobDetailFactoryBean.setGroup("communityJob");
        jobDetailFactoryBean.setDurability(true);
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    /**
     * 帖子分数定时刷新任务 触发器
     * @param postScoreReflushJobDetail
     * @return
     */
    @Bean
    public SimpleTriggerFactoryBean setPostScoreReflushJobDetailTrigger(JobDetail postScoreReflushJobDetail){
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(postScoreReflushJobDetail);
        simpleTriggerFactoryBean.setName("postScoreReflushTrigger");
        simpleTriggerFactoryBean.setGroup("communityTrigger");
        simpleTriggerFactoryBean.setRepeatInterval(1000*60*5);
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());
        return simpleTriggerFactoryBean;
    }

    /**
     * For Test
     * 配置JobDetail
     * @return
     */
    //@Bean
    public JobDetailFactoryBean setTestJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(TestJob.class);
        jobDetailFactoryBean.setName("quartzTest");
        jobDetailFactoryBean.setGroup("test");
        jobDetailFactoryBean.setDurability(true);
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    /**
     * For Test
     * 配置定时任务对应触发器
     * @param testJobDetail
     * @return
     */
    //@Bean
    public SimpleTriggerFactoryBean setJobDetailTrigger(JobDetail testJobDetail){
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(testJobDetail);
        simpleTriggerFactoryBean.setName("quartzTestTrigger");
        simpleTriggerFactoryBean.setGroup("testTrigger");
        simpleTriggerFactoryBean.setRepeatInterval(3000);
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());
        return simpleTriggerFactoryBean;
    }
}
