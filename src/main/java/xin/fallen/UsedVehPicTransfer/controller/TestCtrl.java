package xin.fallen.UsedVehPicTransfer.controller;

import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Author: Fallen
 * Date: 2017/6/17
 * Time: 17:53
 * Usage:
 */
@RestController
public class TestCtrl {

    @RequestMapping("/ruok")
    public String ping() throws SchedulerException {
        return "fine";
    }

}
