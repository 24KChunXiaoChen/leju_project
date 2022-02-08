package top.leju.homefurnishing.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.*;

/*
    创建线程池时调用getThreadPool，如果不配置的话，直接进行默认创建，如果配置，则加载配置。进行创建
 */

@Configuration
@ConfigurationProperties(prefix = "thread-pool-config")
@Setter
public class ThreadPoolConfig {
    private int corePoolSize = Runtime.getRuntime().availableProcessors()*3; //核心线程数
    private int maximumPoolSize = Runtime.getRuntime().availableProcessors()*6; //最大线程数
    private long keepAliveTime = 60; //线程空闲时间，例如：时间60，单位秒
    private TimeUnit unit = TimeUnit.SECONDS; //线程时间粒度
    private int capacity = 1;//队列容量
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(capacity); //使用队列，存放等待任务
    private ThreadFactory threadFactory = Executors.defaultThreadFactory(); //默认线程创建工厂
    private RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy(); //拒绝策略。

    @Bean
    public ThreadPoolExecutor getThreadPool(){
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(corePoolSize,//核心线程数，默认当前cup资源线程数，配置为该线程会减少线程上下文切换次数
                maximumPoolSize,//最大线程数，默认cup资源线程数*6，因为该项目资源使用并不强，可以尝试较大
                keepAliveTime,//线程空闲时间，默认时间60，单位秒
                unit,//线程时间粒度，当前默认为秒
                workQueue,//默认使用有界队列，避免OOM
                threadFactory,//使用默认工厂创建方式
                handler);//拒绝策略。默认在资源耗尽时抛出RejectedExecutionException
        return executorService;
    }
}
