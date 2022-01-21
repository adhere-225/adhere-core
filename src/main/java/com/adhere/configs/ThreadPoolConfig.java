package com.adhere.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * adhere
 * @description: 线程池配置
 * @EnableAsync 开启@Async注解 开启基于注解异步任务功能
 **/

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    @Bean(name = "threadPoolExecutor")
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        NewTreadFactory treadFactory = new NewTreadFactory();
        NewThreadRunsPolicy RejectedHandler = new NewThreadRunsPolicy();

        int i = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(i);
        executor.setMaxPoolSize(i * 2);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadFactory(treadFactory);
        executor.setRejectedExecutionHandler(RejectedHandler);
        executor.initialize();
        return executor;
    }

    //线程工厂
    class NewTreadFactory implements ThreadFactory {
        AtomicInteger threadNumber = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "线程池创建线程--thread-" + threadNumber.getAndIncrement());
            logger.info(thread.getName() + "----id:" + thread.getId() + " has been created");
            return thread;
        }
    }

    //拒绝策略----> 单独启动一个新的临时线程来执行任务(netty也是这样)
    class NewThreadRunsPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                final Thread t = new Thread(r, "线程池拒绝策略--->新开线程");
                t.start();
            } catch (Throwable e) {
                throw new RejectedExecutionException("Failed to start a new thread", e);
            }
        }
    }


}
