package xin.fallen.UsedVehPicTransfer.config;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import xin.fallen.UsedVehPicTransfer.job.PicTransJob;

import static org.quartz.CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;


/**
 * Author: Fallen
 * Date: 2017/6/17
 * Time: 17:55
 * Usage:
 */
@Component
@Configuration
public class SchdulerConfig {

    @Bean(name = "scheduler")
    public SchedulerFactoryBean picTransSchedulerInject(@Autowired CronTrigger picTransCronTrigger) {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        bean.setTriggers(picTransCronTrigger);
        return bean;
    }
}