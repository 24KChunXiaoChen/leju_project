package top.leju.homefurnishing.pojo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.leju.homefurnishing.utils.JsonUtils;

import java.util.HashMap;

/**
 * 线程本体对象池
 */
@Slf4j
@Component
public class ConnectionPool {

    //本次系统历史接入次数
    private int history;

    //当前接入总线程数
    private int total;

    //活跃连接线程数
    private int active;

    //异常连接线程数
    private int wait;

    //已凋亡线程数
    private int death;


    //活跃线程区
    HashMap<String,SocketRunnable> activeHashMap;
    //等待线程区
    HashMap<String,SocketRunnable> waitHashMap;

    public ConnectionPool(){
        log.debug("通道线程池创建ing");
        init();
        log.debug("通道线程池创建成功！");
    }

    void init(){
        history=0;
        total=0;
        active=0;
        wait=0;
        death=0;
        activeHashMap=new HashMap<>();
        waitHashMap=new HashMap<>();
        log.debug("参数初始化成功。。。");
    }

    /*
    ****************************************线程处理
     */
    //添加活跃线程处理
    public void addSocketRunnable(String eid,SocketRunnable sr){
        activeHashMap.put(eid,sr);
        history++;
        total++;
        this.active=activeHashMap.size();
        log.debug("活跃通道线程添加成功，"+sr);
    }

    //失活线程处理
    public void waitSocketRunnable(String eid){
        SocketRunnable wait = activeHashMap.remove(eid);
        waitHashMap.put(eid,wait);
        this.active=activeHashMap.size();
        this.wait=waitHashMap.size();
        log.debug("通道线程进入失活，"+wait);
    }

    //活化线程处理
    public void activeSocketRunnable(String eid){
        SocketRunnable active = waitHashMap.remove(eid);
        activeHashMap.put(eid,active);
        this.active=activeHashMap.size();
        this.wait=waitHashMap.size();
        log.debug("通道线程进入活化，"+active);
    }

    //清除线程
    public void dellSocketRunnable(String eid){
        SocketRunnable remove = activeHashMap.remove(eid);
        if(remove==null){remove = waitHashMap.remove(eid);}
        death++;
        total--;
        this.wait=waitHashMap.size();
        this.active=activeHashMap.size();
        log.debug("已清除异常通道线程，"+remove);
    }


    /*
    ****************************************信息读取
     */

    //获取线程状态
    public int isActive(String eid){//-1.已死亡连接或未知连接，0，连接异常陷入等待，1连接正常，线程活跃
        if(activeHashMap.get(eid)!=null){return 1;}
        if(waitHashMap.get(eid)!=null){return 0;}
        return -1;
    }

    //单体获取
    public SocketRunnable getActiveSocket(String eid) {
        log.debug("活跃通道线程已获取"+eid);
        return activeHashMap.get(eid);
    }

    public SocketRunnable getWaitSocket(String eid) {
        log.debug("失活通道线程已获取"+eid);
        return waitHashMap.get(eid);
    }

    //集合信息获取
    public HashMap<String, SocketRunnable> getActiveHashMap() {
        log.debug("活跃通道集合已获取");
        return activeHashMap;
    }

    public HashMap<String, SocketRunnable> getWaitHashMap() {
        log.debug("失活通道集合已获取");
        return waitHashMap;
    }

    //基础信息获取
    public int getHistory() {
        log.debug("history 信息已获取");
        return history;
    }

    public int getTotal() {
        log.debug("total 信息已获取");
        return total;
    }

    public int getActive() {
        log.debug("active 信息已获取");
        return active;
    }

    public int getWait() {
        log.debug("wait 信息已获取");
        return wait;
    }

    public int getDeath() {
        log.debug("death 信息已获取");
        return death;
    }

    public void test() {
        String s= "连接线程池{" +"\n"+
                "历史接入总数=" + history +"\n"+
                ", 当前连接总数=" + total +"\n"+
                ", 活跃连接数=" + active +"\n"+
                ", 等待连接数=" + wait +"\n"+
                ", 消亡连接数=" + death +"\n"+
                '}';
        System.out.println(s);
    }
}
