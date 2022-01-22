/**
 * 版权 @Copyright: 2018 www.xxx.com Inc. All rights reserved.
 * 项目名称：policy-service<br/>
 * 文件名称： FtpClientFactory.java<br/>
 * 包名：com.chinalife.docking.common
 * 创建人：@author yaotang.zhang@gmail.com<br/>
 * 创建时间：2018年1月31日/上午10:55:19<br/>
 * 修改人：yaotang.zhang@gmail.com<br/>
 * 修改时间：2018年1月31日/上午10:55:19<br/>
 * 修改备注：<br/>
 */

package com.adhere.common.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * 类名称：FtpClientFactory<br/>
 * 类描述：获取FTPClient的工厂类 <br/>
 *
 * @version <br/>
 * TODO
 */
@Component
public class FTPClientFactory implements PooledObjectFactory<FTPClient> {

    /**
     * 日志记录类
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPClientFactory.class);

    /**
     * 配置信息实体
     */
    private FTPClientConfig config;

    /**
     * 有参构造器
     *
     * @param config 配置实体
     */
    public FTPClientFactory(FTPClientConfig config) {
        super();
        this.config = config;
    }

    @Override
    public void activateObject(PooledObject<FTPClient> obj) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.apache.commons.pool2.PooledObjectFactory#destroyObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> obj) throws Exception {
        FTPClient client = getObject(obj);
        try {
            if (client != null && client.isConnected()) {
                client.logout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

    }

    /* (non-Javadoc)
     * @see org.apache.commons.pool2.PooledObjectFactory#makeObject()
     */
    @Override
    public PooledObject<FTPClient> makeObject() throws Exception {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Create FTP Conn ....");
        }
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.setControlEncoding(config.getEncoding());
            ftpClient.connect(config.getHost(), config.getPort());
            // ftpClient.setControlKeepAliveTimeout(5);// set timeout to 5
            // minutes
            boolean login = ftpClient.login(config.getUsername(), config.getPassword());
            if (!login) {
                LOGGER.error(" Ftp client login is error : userName is {} , passWord is {} ..", config.getUsername(),
                        config.getPassword());
                throw new FTPConnectionClosedException(" Ftp client login is error : userName is "
                        + config.getUsername() + " , passWord is " + config.getPassword());
            }
            if (config.getPassiveMode() == null || Boolean.parseBoolean(config.getPassiveMode()) == true) {
                ftpClient.enterLocalPassiveMode();
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(1024);
            return new DefaultPooledObject<FTPClient>(ftpClient);
        } catch (Exception e) {
            throw new Exception("Create FTP connection is failed", e);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.commons.pool2.PooledObjectFactory#passivateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void passivateObject(PooledObject<FTPClient> obj) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.apache.commons.pool2.PooledObjectFactory#validateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> obj) {
        try {
            FTPClient client = getObject(obj);
            if (client == null || !client.isConnected()) {
                return false;
            }
            return client.sendNoOp();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param p PooledObject<FTPClient>对象
     * @return FTPClient实体
     */
    private FTPClient getObject(PooledObject<FTPClient> p) {
        if (p == null || p.getObject() == null) {
            return null;
        }
        return p.getObject();
    }

}
