/**
 * 版权 @Copyright: 2018 www.xxx.com Inc. All rights reserved.
 * 项目名称：policy-service<br/>
 * 文件名称： FTPConfiguration.java<br/>
 * 包名：com.chinalife.docking.common.ftp
 * 创建人：@author yaotang.zhang@gmail.com<br/>
 * 创建时间：2018年2月1日/上午10:04:34<br/>
 * 修改人：yaotang.zhang@gmail.com<br/>
 * 修改时间：2018年2月1日/上午10:04:34<br/>
 * 修改备注：<br/>
 */

package com.adhere.common.ftp;

import com.adhere.utils.FTPUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


/**
 * 类名称：FTPConfiguration<br/>
 * 类描述：FTP配置类
 *
 * @version <br/>
 * TODO
 */
@Component
public class FTPConfiguration {

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPConfiguration.class);

    /**
     * 连接池对象
     */
    private ObjectPool<FTPClient> pool;

    /**
     * 有参构造器
     *
     * @param config     基本配置
     * @param poolConfig 连接池配置
     */
    public FTPConfiguration(FTPClientConfig config, FTPPoolConfig poolConfig) {
        super();
        if (config.getInitialSize() != null) {
            poolConfig.setMaxTotal(config.getInitialSize());
        }
        pool = new GenericObjectPool<FTPClient>(new FTPClientFactory(config), poolConfig);
        preLoadingFtpClient(config.getInitialSize(), poolConfig.getMaxIdle());
        FTPUtils.init(pool);
    }

    /**
     * 根据配置信息往连接池中加载初始化对象
     *
     * @param initialSize 初始化大小
     * @param maxIdle     最大数量
     */
    private void preLoadingFtpClient(Integer initialSize, int maxIdle) {
        if (initialSize == null || initialSize <= 0) {
            return;
        }
        int size = Math.min(initialSize.intValue(), maxIdle);
        for (int i = 0; i < size; i++) {
            try {
                pool.addObject();
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("preLoadingFtpClient error...", e);
                }
            }
        }
    }

    /**
     * 销毁连接池
     */
    @PreDestroy
    public void destroy() {
        if (pool != null) {
            pool.close();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("destroy pool...");
            }
        }
    }

}
