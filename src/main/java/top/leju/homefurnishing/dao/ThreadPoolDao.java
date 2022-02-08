package top.leju.homefurnishing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 提供给前端监控监控线程池
 */
@Component
public class ThreadPoolDao {

    @Autowired
    ThreadPoolExecutor threadPool;
    BlockingQueue<Runnable> queue;

    //线程池核心线程数
    int getCorePoolSize(){
        return threadPool.getCorePoolSize();
    }
    //线程池最大线程数
    int getMaximumPoolSize(){
        return threadPool.getMaximumPoolSize();
    }
    //线程池当前线程数
    int getPoolSize(){
        return threadPool.getPoolSize();
    }
    //线程池剩余线程数
    int getPoolFreeSize(){
        return threadPool.getPoolSize() - threadPool.getActiveCount();
    }
    //当前执行线程数
    int getActiveCount(){
        return threadPool.getActiveCount();
    }

    //getQueue(),返回此执行程序使用的任务队列。
    //当前等待任务数
    int getQueueSize(){
        if(queue==null){
            queue = threadPool.getQueue();
        }
        return queue.size();
    }
    //队列剩余容量
    int getQueueFreeSize(){
        if(queue==null){
            queue = threadPool.getQueue();
        }
        return queue.remainingCapacity();
    }

    public String test(){
        String s ="线程池核心线程数:"+getCorePoolSize()+"\n"+
                "线程池最大线程数:"+getMaximumPoolSize()+"\n"+
                "线程池当前线程数:"+getPoolSize()+"\n"+
                "线程池剩余线程数:"+getPoolFreeSize()+"\n"+
                "当前执行线程数:"+getActiveCount()+"\n"+
                "当前等待任务数:"+getQueueSize()+"\n"+
                "队列剩余容量:"+getQueueFreeSize()+"\n";
        System.out.println(s);
        return s;
    }
}
