package xin.fallen.UsedVehPicTransfer.job;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Author: Fallen
 * Date: 2017/6/17
 * Time: 17:57
 * Usage:
 */
@Component
public class PicTransJob {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${job.pic_trans.delay}")
    private int delay;

    @Value("${job.pic_trans.cron}")
    private String cron;

    @Value("${job.pic_trans.description}")
    private String description;

    @Value("${job.pic_trans.miss_fire_strategy}")
    private String missFireStrategy;

    @Value("${job.pic_trans.method}")
    private String method;

    @Value("${job.pic_trans.concurrent}")
    private boolean concurrent;

    @Bean(name = "picTransJobDetail")
    public MethodInvokingJobDetailFactoryBean picTransJobDetailInject(@Autowired PicTransJob picTransJob) {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setConcurrent(concurrent);
        bean.setTargetObject(picTransJob);
        bean.setTargetMethod(method);
        return bean;
    }

    @Bean(name = "picTransCronTrigger")
    public CronTriggerFactoryBean picTransCronTriggerInject(@Autowired JobDetail picTransJobDetail) {
        CronTriggerFactoryBean bean = new CronTriggerFactoryBean();
        int index = 2;
        try {
            Field field = CronTrigger.class.getDeclaredField(missFireStrategy.toUpperCase());
            index = field.getInt(null);
        } catch (Exception e) {
            log.error("没有找到配置的miss file strategy，默认 MISFIRE_INSTRUCTION_DO_NOTHING，原因是：{}", e.getMessage());
        }
        bean.setMisfireInstruction(index);
        bean.setJobDetail(picTransJobDetail);
        bean.setCronExpression(cron);
        bean.setStartDelay(delay);
        bean.setDescription(description);
        return bean;
    }

    public void execute() throws JobExecutionException {
        System.out.println("I am running");
    }
}
