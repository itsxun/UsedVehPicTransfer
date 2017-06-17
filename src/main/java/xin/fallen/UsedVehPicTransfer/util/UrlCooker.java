package xin.fallen.UsedVehPicTransfer.util;

/**
 * Created by fallen on 16-12-5.
 */
public class UrlCooker {
    public static String format(String oUrl) {
        String dUrl = null;
        int pos = oUrl.indexOf("?") + 1;
        String path = oUrl.substring(0, pos);
        String param = oUrl.substring(pos);
        dUrl = param
                .replaceAll("%", "%25")
                .replaceAll("\\+", "%2b")
                .replaceAll("\\\\", "%5C")
                .replaceAll("\\|", "%7C")
                .replaceAll("#", "%23")
                .replaceAll(" ", "%20")
                .replaceAll("\\{", "%7B")
                .replaceAll("}", "%7D")
                .replaceAll("\"", "%22")
                .replaceAll(":", "%3A")
                .replaceAll(",", "%2C")
                .replaceAll("<", "%3C")
                .replaceAll(">", "%3E")
                .replaceAll("`", "%60")
                .replaceAll("\\^", "%5e");
        return path + dUrl;
    }
}