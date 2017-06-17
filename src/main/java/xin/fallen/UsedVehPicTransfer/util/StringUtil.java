package xin.fallen.UsedVehPicTransfer.util;

/**
 * Author: Fallen
 * Date: 2017/5/30
 * Time: 20:06
 * Usage:
 */
public class StringUtil {
    public static String trans2CamelCase(String origin) {
        char[] tmp = origin.toCharArray();
        tmp[0] -= 32;
        return new String(tmp);
    }
}