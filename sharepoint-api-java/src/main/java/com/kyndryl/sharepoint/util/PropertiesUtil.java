package com.kyndryl.sharepoint.util;

import java.io.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertiesUtil {

    /**
     * 根据key读取value
     *
     * @param filePath
     * @param keyWord
     * @return
     * @return String
     * @throws
     * @Title: getProperties
     * @Description: 使用缓冲输入流读取配置文件，然后将其加载，再按需操作
     * 绝对路径或相对路径， 如果是相对路径，则从当前项目下的目录开始计算，
     */
    public static String getPropertiesByPath(String filePath, String keyWord) {
        Properties prop = new Properties();
        String value = null;
        try {
            // 通过输入缓冲流进行读取配置文件
            InputStream InputStream = new BufferedInputStream(new FileInputStream(new File(filePath)));
            // 加载输入流
            prop.load(InputStream);
            // 根据关键字获取value值
            value = prop.getProperty(keyWord);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getProperties(String fileName, String keyWord) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(fileName);

        String value = null;
        try {
            value = resourceBundle.getString(keyWord);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


}
