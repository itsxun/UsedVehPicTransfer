package xin.fallen.UsedVehPicTransfer.job;

import org.apache.commons.io.FileUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import xin.fallen.UsedVehPicTransfer.config.StaticConfig;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Author: Fallen
 * Date: 2017/6/17
 * Time: 17:57
 * Usage:
 */
@Component
@Configuration
public class PicMoveJob {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${job.pic_move.delay}")
    private int delay;

    @Value("${job.pic_move.cron}")
    private String cron;

    @Value("${job.pic_move.description}")
    private String description;

    @Value("${job.pic_move.miss_fire_strategy}")
    private String missFireStrategy;

    @Value("${job.pic_move.method}")
    private String method;

    @Value("${job.pic_move.concurrent}")
    private boolean concurrent;

    @Value("${job.pic_move.dir_read}")
    private String dirOrigin;

    @Value("${job.pic_move.dir_tmp}")
    private String dirTmp;

    @Value("${job.pic_move.filename_pattern}")
    private String fileNamePattern;

    @Bean(name = "picMoveJobDetail")
    public MethodInvokingJobDetailFactoryBean picTransJobDetailInject(@Autowired PicMoveJob picTransJob) {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setConcurrent(concurrent);
        bean.setTargetObject(picTransJob);
        bean.setTargetMethod(method);
        return bean;
    }

    @Bean(name = "picMoveCronTrigger")
    public CronTriggerFactoryBean picTransCronTriggerInject(@Qualifier("picMoveJobDetail") JobDetail picTransJobDetail) {
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

    @Bean(name = "picMoveScheduler")
    public SchedulerFactoryBean picTransSchedulerInject(@Qualifier("picMoveCronTrigger") CronTrigger picMoveCronTrigger) {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        bean.setTriggers(picMoveCronTrigger);
        return bean;
    }

    public void execute() {
        File readDir = new File(dirTmp);
        File tmpDir = new File(dirTmp);
        File[] targetFiles = readDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().matches(fileNamePattern);
            }
        });
        try {
            for (File f : targetFiles != null ? targetFiles : new File[0]) {
                FileUtils.moveFileToDirectory(f, tmpDir, true);
                StaticConfig.PicTransQueue.offer(tmpDir + File.separator + f.getName());
            }
        } catch (Exception e) {
        }
        readDir = new File(dirOrigin);
        if (!readDir.isDirectory()) {
            throw new RuntimeException("配置的读取文件夹未找到");
        }
        targetFiles = readDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().matches(fileNamePattern);
            }
        });
        log.info("job开始，找到{}个文件", targetFiles != null ? targetFiles.length : 0);
        try {
            for (File f : targetFiles != null ? targetFiles : new File[0]) {
                FileUtils.moveFileToDirectory(f, tmpDir, true);
                StaticConfig.PicTransQueue.offer(tmpDir + File.separator + f.getName());
            }
            log.info("队列中现有{}个文件等待转移,", StaticConfig.PicTransQueue.size());
        } catch (IOException e) {
            log.error("文件移动中出现异常，原因是：{}", e.getMessage());
        }
    }
}