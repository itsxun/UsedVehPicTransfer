package xin.fallen.UsedVehPicTransfer.listerner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import xin.fallen.UsedVehPicTransfer.config.StaticConfig;
import xin.fallen.UsedVehPicTransfer.util.ConfigLoader;
import xin.fallen.UsedVehPicTransfer.util.FileFinder;


/**
 * Author: fallen
 * Date: 17-2-14
 * Time: 上午9:45
 * Usage:
 */
@Configuration
public class SpringReadyLis implements ApplicationListener<ContextRefreshedEvent> {
    private static Logger log = LoggerFactory.getLogger("logger");

    public void onApplicationEvent(ContextRefreshedEvent e) {
        ConfigLoader.load(FileFinder.find("config.xml"), StaticConfig.class);
        log.info("<==========================Config Load Complete==========================>");
    }
}