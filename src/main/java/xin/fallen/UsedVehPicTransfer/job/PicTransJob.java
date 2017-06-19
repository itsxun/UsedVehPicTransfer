package xin.fallen.UsedVehPicTransfer.job;

import com.google.gson.Gson;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Fallen
 * Date: 2017/6/19
 * Time: 13:02
 * Usage:
 */
@Component
@Configuration
public class PicTransJob {
    @Value("${job.pic_trans.delay}")
    private int delay;

    @Value("${job.pic_trans.cron}")
    private String cron;

    @Value("${job.pic_trans.description}")
    private String description;

    @Value("${job.pic_trans.time_out}")
    private int timeOut;

    @Value("${job.pic_trans.dir_remote}")
    private String dirRemote;

    @Value("${job.pic_trans.dir_read}")
    private String dirRead;

    @Value("${job.pic_trans.concurrent}")
    private boolean concurrent;

    @Value("${job.pic_trans.method}")
    private String method;

    @Value("${job.pic_trans.miss_fire_strategy}")
    private String missFireStrategy;

    @Value("${job.pic_trans.script_path}")
    private String scriptPath;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean(name = "picTransJobDetail")
    public MethodInvokingJobDetailFactoryBean picTransJobDetailInject(@Autowired PicTransJob picTransJob) {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setConcurrent(concurrent);
        bean.setTargetObject(picTransJob);
        bean.setTargetMethod(method);
        return bean;
    }

    @Bean(name = "picTransCronTrigger")
    public CronTriggerFactoryBean picTransCronTriggerInject(@Qualifier("picTransJobDetail") JobDetail picTransJobDetail) {
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

    @Bean(name = "picTransScheduler")
    public SchedulerFactoryBean picTransSchedulerInject(@Qualifier("picTransCronTrigger") CronTrigger picTransCronTrigger) {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        bean.setTriggers(picTransCronTrigger);
        return bean;
    }

    public void execute() {
        log.info("队列中现有{}个文件等待同步，准备调用shell...");
        String filePath = null;
        try {
            filePath = StaticConfig.PicTransQueue.take();
        } catch (InterruptedException e) {
            log.error("文件同步出现异常，原因是：{}", e.getMessage());
            return;
        }
        if (!new File(filePath).isFile()) {
            log.error("没有找到指定文件，文件地址为：{}", filePath);
            return;
        }
        CommandLine cl = new CommandLine(new File(scriptPath));
        cl.addArgument(filePath);
        cl.addArgument(dirRemote.replace("{DIR}", sdf.format(new Date())));
        Executor exec = new DefaultExecutor();
        exec.setExitValue(0);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeOut * 1000);
        ProcessLoggerHandler handler = new ProcessLoggerHandler(watchdog, filePath);
        try {
            exec.execute(cl, handler);
        } catch (IOException e) {
            log.error("文件上传失败，原因是：{}，文件路径为：{}", e.getMessage(), filePath);
            return;
        } finally {
            StaticConfig.PicTransQueue.offer(filePath);
        }
        File origin = new File(filePath);
        try {
            handler.waitFor();
        } catch (InterruptedException e) {
            log.error("等待执行返回中发生异常");
            return;
        }
        try {
            FileUtils.moveFile(origin, new File(origin.getAbsolutePath() + ".Fallen"));
        } catch (IOException e) {
            log.error("文件改名失败，原因是：{}", e.getMessage());
        }
    }

    private class ProcessLoggerHandler extends DefaultExecuteResultHandler {
        private Logger log = LoggerFactory.getLogger(this.getClass());
        private ExecuteWatchdog watchdog;
        private long startMills;
        private String filePath;

        public ProcessLoggerHandler(final ExecuteWatchdog watchdog, String filePath) {
            this.watchdog = watchdog;
            this.startMills = new Date().getTime();
            this.filePath = filePath;
        }

        public ProcessLoggerHandler(final int exitValue, String filePath) {
            super.onProcessComplete(exitValue);
            this.startMills = new Date().getTime();
            this.filePath = filePath;
        }

        @Override
        public void onProcessComplete(final int exitValue) {
            super.onProcessComplete(exitValue);
            log.info("文件上传成功，耗时(毫秒)：{}", new Date().getTime() - startMills);
        }

        @Override
        public void onProcessFailed(final ExecuteException e) {
            super.onProcessFailed(e);
            if (watchdog != null && watchdog.killedProcess()) {
                log.error("文件上传超时，文件路径为：{}", filePath);
            } else {
                log.error("文件上传失败，原因是：{}，文件路径为：{}", e.getCause(), filePath);
            }
        }
    }
}


