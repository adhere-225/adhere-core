package com.adhere.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 类名称：FTPUtils<br/>
 * 类描述：FTP操作工具类<br/>
 */
@Component
public class FTPUtils {

    /**
     * 日志实体
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPUtils.class);

    /**
     * 是否初始化连接池
     */
    private static volatile boolean hasInit = false;

    /**
     * FTPClient连接池
     */
    private static ObjectPool<FTPClient> ftpClientPool;

    /**
     * 初始化连接池
     *
     * @param ftpClientPool 连接池对象
     */
    public static void init(ObjectPool<FTPClient> ftpClientPool) {
        if (!hasInit) {
            synchronized (FTPUtils.class) {
                if (!hasInit) {
                    FTPUtils.ftpClientPool = ftpClientPool;
                    hasInit = true;
                }
            }
        }
    }

    /**
     * 获取链接资源
     *
     * @return FTPClient
     */
    public static FTPClient getFTPClient() {
        checkFTPClientPoolAvailable();
        FTPClient ftpClient = null;
        Exception ex = null;
        // 获取连接最多尝试3次
        for (int i = 0; i < 3; i++) {
            try {
                ftpClient = ftpClientPool.borrowObject();
                ftpClient.changeWorkingDirectory("/");
                break;
            } catch (Exception e) {
                ex = e;
            }
        }
        if (ftpClient == null) {
            throw new RuntimeException("FTPUtils: Could not get a ftpClient from the pool", ex);
        }
        return ftpClient;
    }

    /**
     * 判断FTP连接池是否可用
     */
    private static void checkFTPClientPoolAvailable() {
        Assert.state(hasInit, "FTP未启用或连接失败！");
    }

    /**
     * 释放链接资源
     *
     * @param ftpClient 需要释放的ftpclient对象
     */
    private static void releaseFTPClient(FTPClient ftpClient) {
        if (ftpClient == null) {
            return;
        }
        try {
            ftpClientPool.returnObject(ftpClient);
        } catch (Exception e) {
            LOGGER.error("FTPUtils: Could not return the ftpClient to the pool", e);
            // Destory client
            if (ftpClient.isAvailable()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
    }

    /**
     * FTP上传文件
     *
     * @param localDirectoryAndFileName 文件本地位置
     * @param ftpFileName               上传后的文件名称
     * @param ftpDirectory              上传路径
     * @return 是否上传成功
     */
    public static boolean uploadFile(String localDirectoryAndFileName, String ftpFileName, String ftpDirectory) {
        boolean flag = false;
        File srcFile = new File(localDirectoryAndFileName);
        FileInputStream fis;
        try {
            fis = new FileInputStream(srcFile);
            uploadFile(fis, ftpFileName, ftpDirectory);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("FTPUtils: upload file is error : file not found !");
            return false;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("FTPUtils: upload file is success, local file is  {}  .", localDirectoryAndFileName);
        }
        return flag;
    }

    /**
     * 上传文件
     *
     * @param inputStream  文件流
     * @param ftpFileName  存放的文件名称
     * @param ftpDirectory 存放的文件路径
     * @return 是否上传成功
     */
    public static boolean uploadFile(FileInputStream inputStream, String ftpFileName, String ftpDirectory) {
        FTPClient ftpClient = getFTPClient();
        boolean flag = false;
        if (ftpClient != null) {
            try {
                // 创建目录
                ftpClient.makeDirectory(ftpDirectory);
                ftpClient.changeWorkingDirectory(ftpDirectory);
                // 上传
                flag = ftpClient.storeFile(new String(ftpFileName.getBytes(), ftpClient.getControlEncoding()),
                        inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                releaseFTPClient(ftpClient);
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("FTPUtils: upload file is success, path is  {} .", ftpDirectory + "/" + ftpFileName);
        }
        return flag;
    }

    /**
     * 从FTP服务器上下载文件
     *
     * @param ftpDirectoryAndFileName   FTP存储的文件路径以及文件名
     * @param localDirectoryAndFileName 存储到本地的路径以及文件名
     * @return true or false
     */
    public static boolean downloadFile(String ftpDirectoryAndFileName, String localDirectoryAndFileName) {
        FTPClient ftpClient = getFTPClient();
        ftpClient.enterLocalPassiveMode(); // Use passive mode as default
        try {
            ftpDirectoryAndFileName = ftpDirectoryAndFileName.replace("\\", "/");
            String filePath = ftpDirectoryAndFileName.substring(0, ftpDirectoryAndFileName.lastIndexOf("/"));
            String fileName = ftpDirectoryAndFileName.substring(ftpDirectoryAndFileName.lastIndexOf("/") + 1);
            ftpClient.changeWorkingDirectory(filePath);
            ftpClient.retrieveFile(new String(fileName.getBytes(), ftpClient.getControlEncoding()),
                    new FileOutputStream(localDirectoryAndFileName)); // download
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("FTPUtils: download file is success , save path is {} ", localDirectoryAndFileName);
            }
            return true;
        } catch (IOException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("FTPUtils:  download file is error !");
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从FTP服务器上下载文件
     *
     * @param ftpDirectoryAndFileName FTP存储的文件路径以及文件名
     * @return InputStream
     */
    public static InputStream downloadFile(String ftpDirectoryAndFileName) {
        FTPClient ftpClient = getFTPClient();
        ftpClient.enterLocalPassiveMode(); // Use passive mode as default
        try {
            ftpDirectoryAndFileName = ftpDirectoryAndFileName.replace("\\", "/");
            String filePath = ftpDirectoryAndFileName.substring(0, ftpDirectoryAndFileName.lastIndexOf("/"));
            String fileName = ftpDirectoryAndFileName.substring(ftpDirectoryAndFileName.lastIndexOf("/") + 1);
            ftpClient.changeWorkingDirectory(filePath);
            return ftpClient.retrieveFileStream(new String(fileName.getBytes(), ftpClient.getControlEncoding()));
        } catch (IOException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("FTPUtils:  download file is error !");
            }
            e.printStackTrace();
            return null;
        }
    }

}
