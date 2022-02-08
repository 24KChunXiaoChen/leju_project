package top.leju.homefurnishing.component;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.leju.homefurnishing.config.SocketConfig;
import top.leju.homefurnishing.config.ThreadPoolConfig;
import top.leju.homefurnishing.dao.ThreadPoolDao;
import top.leju.homefurnishing.pojo.*;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

/**
 *  socket服务监听核心类
 *
 *  getActiveCount(),返回正在执行任务的线程的大概数量。
 *  getCompletedTaskCount(),返回完成执行的任务的大致总数。
 *  getCorePoolSize()返回核心线程数
 *  getLargestPoolSize(),返回在池中同时进行的最大线程数。
 * getMaximumPoolSize(),返回允许的最大线程数。
 * getPoolSize(),返回池中当前的线程数。
 * getQueue(),返回此执行程序使用的任务队列。
 * getTaskCount(),返回计划执行的任务的大概总数。
 *
 */
@Slf4j
@Component
public class SocketServer {
    @Autowired
    ThreadPoolExecutor threadPool;
    @Autowired
    DevicePool devicePool;
    @Autowired
    ConnectionPool connectionPool;

    @Autowired
    ThreadPoolDao dao;


    private int port;//监听端口
    private int timeOut;//监听超时时间


    private boolean isMonitor = false;//是否监听
    private ServerSocket serverSocket;
    private Socket socket;
    private String ip;
    private SocketRunnable socketRunnable;
    private TbEquipment tbEquipment;

    @Autowired
    SocketServer(SocketConfig config) throws Exception {//创建类时进行初始化
        port=config.getPort();
        timeOut=config.getTimeOut();
        init();loader();
    }

    public void coreMain() {
        try {
            while (isMonitor){
                try {
                    log.debug("socket线程监听ing。。。");
                    socket = serverSocket.accept();//堵塞获取监听连接
                }catch (SocketTimeoutException e){
                    log.debug("socket线程监听超时。。。");
                    continue;
                }
                ip = socket.getInetAddress().getHostAddress();//获取接入的socket ip
                log.info("监听到新socket连接，IP地址为："+ip+"，请求接入");
                try {
                    socketRunnable = new SocketRunnable(connectionPool,devicePool,socket);
                    tbEquipment = socketRunnable.getTbEquipment();
                    //检查是否合法
                    if(devicePool.isHistoryEquipment(tbEquipment)){
                        log.debug("历史对象接入，"+tbEquipment);
                        if(!devicePool.isLegitimateEquipment(tbEquipment)){
                            throw new IllegalHistoricalObjectException("发生非法对象事件！");
                        }
                        if(devicePool.isAccessEquipment(tbEquipment)){
                            throw new IllegalHistoricalObjectException("发生已接入对象再次接入事件！");
                        }
                    }
                    /*
                    仅仅是进行了基础认证
                    此处该可以进行专项认证
                    想法是可以预留接口，使用配置类注入，配置类通过配置决定是否启用该模式
                    然后主逻辑上判断是否注入为null，
                    不为null进行，调用接口验证，
                    验证成功进行放行，失败进行报错

                    该想法主要是向实现指定类型的设备进行特定化处理
                    用于构建几种必要的参数，已实现指向性的功能
                    使设备实现类似与java中接口方式，类型为接口，可以有自定义
                    但是必须遵循特定方法和参数，即为设备接口
                    -------------------------------------------------------------------------------------------------------
                     */
                    log.debug("设备接入请求认证成功。");
                    threadPool.submit(socketRunnable);
                    //socketRunnable就绪，里面socket连接，thread线程，
                    log.info("接入维护任务已交付线程池。");
                    //此处应该将其交付给线程本体对象池，进行统一管理，管理存活对象
                    devicePool.addActiveHashMap(tbEquipment);
                    connectionPool.addSocketRunnable(tbEquipment.getEId(),socketRunnable);
                    log.debug("资源已移交给相关资源池。");


                    dao.test();//测试方法，查看线程池数据
                    devicePool.test();//测试方法，查看设备池数据
                    connectionPool.test();//测试方法，查看连接线程池数据
                } catch (AuthenticationFailedException e) {//socket请求认证失败
                    //记录创建失败日志
                    log.error("错误，未知ip("+ip+")认证失败，拒绝接入！"+e);
                    socket.close();
                }catch (RejectedExecutionException e){//系统达到预定阈值，不在处理设备连接
                    socketRunnable.refuse();//拒绝设备连接
                    log.error("拒绝设备("+ip+")接入请求，原因：线程池及其等待队列已满！");
                }catch (IllegalHistoricalObjectException e){//历史设备非法触发
                    socketRunnable.refuse();//拒绝设备连接
                    log.error("拒绝设备("+ip+")接入请求，原因：非法历史对象接入,"+tbEquipment);
                }
                ip=null;
                socket=null;
                socketRunnable=null;
                tbEquipment=null;
            }
        } catch (Exception e) {
            log.error("未知异常发生！",e);
            close();

            //重试机制
            try {
                log.info("尝试重新启动ing");
                init();
                log.info("异步监听启动ing");
                loader();
                log.info("重启完毕！");
            } catch (Exception e1) {
                log.error("重启失败，捕获异常：",e1);
                isMonitor=false;
                close();
            }
        }
    }




    //初始化服务
    public void init() throws Exception{
        log.info("初始化ing");
        // 1.创建Socket服务端，监听指定端口
        serverSocket = new ServerSocket(port);
        log.info("监听服务端口("+port+")占用成功。。。");
        serverSocket.setSoTimeout(timeOut);//配置监听堵塞时间
        log.info("服务监听堵塞时间("+timeOut+")");
    }

    //资源关闭
    public void close(){
        try {
            if(socket!=null){socket.close();}
            if(serverSocket!=null){serverSocket.close();}
            log.info("流资源关闭成功！");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("流资源关闭异常！");
        }
    }

    //启动监听
    public void loader(){
        if(!isMonitor){//不在监听状态才进行启动异步监听
            new Thread(()->coreMain()).start();
            isMonitor=true;
            log.info("异步监听启动成功！");
        }else {
            log.info("监听启动中，无需重复启动！");
        }
    }

    //是否监听ing
    public boolean isMonitor() {
        return isMonitor;
    }

    //结束监听
    public void EndMonitor(){
        if(isMonitor){
            isMonitor=false;
            log.info("柔性结束监听ing。。。");
        }else {
            log.info("未启动监听服务，无需结束监听");
        }
    }
}
