package xin.fallen.UsedVehPicTransfer.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Collection;

/**
 * Author: fallen
 * Date: 17-2-9
 * Time: 下午4:14
 * Usage: 根据文件名找到配置文件
 */
public class FileFinder {
    public static File find(String fileName) {
        File res = null;
        try {
            //扩展名
            String extName = FilenameUtils.getExtension(fileName);
            //递归查找
            Collection<File> list = null;
            list = FileUtils.listFiles(new File(new File(Thread.currentThread().getContextClassLoader().getResource("").getPath()).getParent()), new String[]{extName.toLowerCase(), extName.toUpperCase()}, true);
            //确定文件
            for (File file : list) {
                if (file.getName().equalsIgnoreCase(fileName)) {
                    res = file;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return res;
    }
}
