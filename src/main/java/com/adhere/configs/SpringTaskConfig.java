package com.adhere.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * adhere
 * @description: 定时任务配置
 * @EnableAsync 开启@Scheduled注解 开启基于注解的定时任务功能
 **/
@Configuration
@EnableScheduling
public class SpringTaskConfig {

}
