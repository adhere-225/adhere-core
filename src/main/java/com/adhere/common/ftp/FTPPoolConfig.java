/** 
 * 版权 @Copyright: 2018 www.xxx.com Inc. All rights reserved.      
 * 项目名称：policy-service<br/> 
 * 文件名称： FTPjava<br/> 
 * 包名：com.chinalife.docking.entity       
 * 创建人：@author yaotang.zhang@gmail.com<br/>    
 * 创建时间：2018年1月31日/上午10:53:06<br/>    
 * 修改人：yaotang.zhang@gmail.com<br/>    
 * 修改时间：2018年1月31日/上午10:53:06<br/>    
 * 修改备注：<br/> 
 */

package com.adhere.common.ftp;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;


@Component
public class FTPPoolConfig extends GenericObjectPoolConfig {

    /**
     * 无参构造器
     */
    public FTPPoolConfig() {
        setTestWhileIdle(true);
        setTestOnReturn(true);
        setMaxIdle(5);
        setMinEvictableIdleTimeMillis(60000);
        setSoftMinEvictableIdleTimeMillis(50000);
        setTimeBetweenEvictionRunsMillis(30000);
    }

}
