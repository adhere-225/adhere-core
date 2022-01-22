package com.adhere.common.ftp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 类名称：FTPClientConfig<br/>
 * 类描述：<br/>
 *
 * @version <br/>
 */
@Component
@ConfigurationProperties(prefix = "ftp")
@Data
public class FTPClientConfig {

    /**
     *
     */
    private String host;
    /**
     *
     */
    private int port;
    /**
     *
     */
    private String username;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String passiveMode;
    /**
     *
     */
    private String encoding;
    /**
     *
     */
    private int clientTimeout;
    /**
     *
     */
    private int threadNum;
    /**
     *
     */
    private int transferFileType;
    /**
     *
     */
    private boolean renameUploaded;
    /**
     *
     */
    private int retryTimes;

    /**
     *
     */
    private Integer initialSize;


}
