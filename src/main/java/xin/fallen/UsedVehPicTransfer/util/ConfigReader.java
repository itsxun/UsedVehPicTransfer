package xin.fallen.UsedVehPicTransfer.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by itsxun on 16/12/2.
 */
public class ConfigReader {
    private static Logger logger = LoggerFactory.getLogger("logger");

    private static Document doc;

    public static String getValue(String key) {
        String value = null;
        if (doc == null) {
            throw new NullPointerException("请先调用init方法初始化配置文件");
        }
        try {
            List<Element> eleList = doc.selectNodes("//" + key);
            if (eleList != null) {
                value = eleList.get(0).getTextTrim();
            }
        } catch (Exception e) {
            logger.error("请检查配置文件中是否有{}这个属性", key);
        }
        return value;
    }

    public static HashMap<String, String> getValues(ArrayList<String> keys) {
        if (doc == null) {
            throw new NullPointerException("请先调用init方法初始化配置文件");
        }
        HashMap<String, String> values = new HashMap<String, String>(keys.size());
        int i = 0;
        try {
            for (String s : keys) {
                values.put(s, ((String) doc.selectNodes("//" + s).get(0)).trim());
                i++;
            }
        } catch (Exception e) {
            logger.error("解析异常,{}的属性值未找到", keys.get(i));
        }
        return values;
    }

    public static boolean configInit() {
        boolean flag = false;
        File config = FileFinder.find("config.xml");
        if (config == null) {
            return flag;
        }
        SAXReader reader = new SAXReader();
        try {
            doc = reader.read(config);
        } catch (DocumentException e) {
            logger.error("配置文件解析异常,原因是{}", e.getMessage());
        }
        if (doc != null) {
            flag = true;
        }
        return flag;
    }

    public static void configDestory() {
        doc = null;
    }
}