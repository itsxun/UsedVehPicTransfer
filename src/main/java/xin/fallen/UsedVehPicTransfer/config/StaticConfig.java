package xin.fallen.UsedVehPicTransfer.config;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Fallen
 * Date: 2017/6/17
 * Time: 17:58
 * Usage:
 */
public class StaticConfig {
    public static BlockingDeque<String> PicTransQueue = new LinkedBlockingDeque<>();
}