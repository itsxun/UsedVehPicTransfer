package xin.fallen.UsedVehPicTransfer.job;

import com.google.gson.Gson;
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
import xin.fallen.UsedVehPicTransfer.util.HttpUtil;
import xin.fallen.UsedVehPicTransfer.vo.Callback;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * Author: Fallen
 * Date: 2017/6/19
 * Time: 13:02
 * Usage:
 */
@Component
@Configuration
public class PicAddrUpdateJob {
    @Value("${job.pic_addr_update.delay}")
    private int delay;

    @Value("${job.pic_addr_update.cron}")
    private String cron;

    @Value("${job.pic_addr_update.description}")
    private String description;

    @Value("${job.pic_addr_update.remote_update_url}")
    private String remoteUpdatUrl;

    @Value("${job.pic_addr_update.dir_read}")
    private String dirRead;

    @Value("${job.pic_addr_update.remote_url_prefix}")
    private String remoteUrlPrefix;

    @Value("${job.pic_addr_update.concurrent}")
    private boolean concurrent;

    @Value("${job.pic_addr_update.method}")
    private String method;

    @Value("${job.pic_addr_update.miss_fire_strategy}")
    private String missFireStrategy;

    @Value("${job.pic_addr_update.filename_pattern}")
    private String filenamePattern;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean(name = "picAddrUpdateJobDetail")
    public MethodInvokingJobDetailFactoryBean picTransJobDetailInject(@Autowired PicAddrUpdateJob picAddrUpdateJob) {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setConcurrent(concurrent);
        bean.setTargetObject(picAddrUpdateJob);
        bean.setTargetMethod(method);
        return bean;
    }

    @Bean(name = "picAddrUpdateCronTrigger")
    public CronTriggerFactoryBean picTransCronTriggerInject(@Qualifier("picAddrUpdateJobDetail") JobDetail picAddrUpdateJobDetail) {
        CronTriggerFactoryBean bean = new CronTriggerFactoryBean();
        int index = 2;
        try {
            Field field = CronTrigger.class.getDeclaredField(missFireStrategy.toUpperCase());
            index = field.getInt(null);
        } catch (Exception e) {
            log.error("没有找到配置的miss file strategy，默认 MISFIRE_INSTRUCTION_DO_NOTHING，原因是：{}", e.getMessage());
        }
        bean.setMisfireInstruction(index);
        bean.setJobDetail(picAddrUpdateJobDetail);
        bean.setCronExpression(cron);
        bean.setStartDelay(delay);
        bean.setDescription(description);
        return bean;
    }

    @Bean(name = "picAddrUpdateScheduler")
    public SchedulerFactoryBean picTransSchedulerInject(@Qualifier("picAddrUpdateCronTrigger") CronTrigger picAddrUpdateCronTrigger) {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        bean.setTriggers(picAddrUpdateCronTrigger);
        return bean;
    }

    public void execute() {
        File file = new File(dirRead);
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().matches(filenamePattern);
            }
        });
        log.info("找到{}个文件等待远程请求", files != null ? files.length : 0);
        String filePath;
        for (File f : files != null ? files : new File[0]) {
            filePath = f.getAbsolutePath();
            if (!filePath.contains("_"))
                continue;
            String[] strs = filePath.split("_");
            String url = remoteUpdatUrl.replace("{PREFIX}", strs[1]).replace("{KEYWORD}", strs[2]).replace("{TYPE}", strs[3]).replace("{URL}", remoteUrlPrefix + "/" + strs[3] + "_" + UUID.randomUUID().toString() + ".JPG");
            log.info("请求数据库接口：{}", url);
            Callback callback = new Gson().fromJson(HttpUtil.get(url).replace("null(", "").replace(")", ""), Callback.class);
            if (callback != null && "1".equals(callback.getRes())) {
                try {
                    FileUtils.forceDelete(new File(filePath));
                } catch (IOException e) {
                    log.error("文件上传成功，但是本地文件删除失败，原因是：{}", e.getMessage());
                }
                log.info("文件上传成功，数据库写入完成");
                return;
            }
            log.error("文件内网地址写数据库失败，原因是：{}", (callback != null ? callback.getMsg() : null) == null ? "" : callback.getMsg());
        }
    }
}