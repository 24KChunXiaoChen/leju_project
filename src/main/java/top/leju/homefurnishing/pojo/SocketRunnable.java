package top.leju.homefurnishing.pojo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import top.leju.homefurnishing.utils.Base64Util;
import top.leju.homefurnishing.utils.JsonUtils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 执行流程：
 *  SocketRunnable构造器中进行初始化，如果初始化失败，SocketRunnable构建失败(认证错误)，进行下一轮，该操作为SocketServer线程执行
 *  初始化构建SocketRunnable，通过socket进行认证编码认证，信息认证，信息构建，建立心跳长连接，进入线程等待(1.接收处理，2.拒绝处理)
 *
 *  接收处理，进行心跳维护，处理传入信息，异常处理，进行调用等
 */
@Slf4j
public class SocketRunnable implements Runnable {

    ConnectionPool connectionPool;
    DevicePool devicePool;

    private int timeOut = 1*1000;//读取超时时间

    Socket socket;
    Thread thread;
    Result inputResult;
    Result outputResult;
    StringBuilder builder;

    InputStream inputStream;
    OutputStream outputStream;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;

    Timer timer;
    int lose=0;//丢失标记
    int late=0;//迟到标记
    int loseNumber=0;//丢失次数
    int lateNumber=0;//迟到次数
    int latetimeOut=6*1000;


    TbEquipment tbEquipment;

    //socket状态
    //约定通知时间
    //超时标记时间
    //连接超时次数
    //连接丢失次数
    //队列大小
    //心跳记录队列20****************************************************************************************************************
    public SocketRunnable (ConnectionPool connectionPool,DevicePool devicePool,Socket socket) throws AuthenticationFailedException{
        this.socket=socket;
        this.connectionPool=connectionPool;
        this.devicePool=devicePool;
        try {
            init();
            reader();
            if(inputResult.isSuccess() && inputResult.getCode()==1){
                outputResult=Result.ofcall(1,"你好客户端，准许接入！");
                writer();
                reader();
                if(inputResult.isSuccess() && inputResult.getCode()==4){
                    tbEquipment = JsonUtils.jsonToPojo((String) inputResult.getData(),TbEquipment.class);
                    outputResult = Result.ofcall(1,"你好客户端，认证成功！请等待回应");
                    writer();
                    return;
                }
            }
            //虽然格式正确，但验证流程出错
            throw new AuthenticationFailedException("设备认证失败！");
        } catch (Exception e) {
            log.error("设备认证失败！错误如下：",e);
            throw new AuthenticationFailedException("设备认证失败！");
        }
    }

    public void reader() throws IOException {
        inputResult = JsonUtils.jsonToPojo(Base64Util.decode(bufferedReader.readLine()), Result.class);
    }

    public void writer() throws IOException {
        bufferedWriter.write(Base64Util.encode(JsonUtils.objectToJson(outputResult))+"\n");
        bufferedWriter.flush();
        outputResult=null;
    }

    public void refuse(){
        //拒绝设备接入
        outputResult=Result.ofThrowable(new Exception("服务器，拒绝接入"));
        try {
            writer();
        } catch (IOException e) {
            close();
        }
    }


    @Override
    public void run() {//进行维护连接操作
        try {
            thread = Thread.currentThread();  //该处应答可能可以进行失败，因为线程池满载情况下，加入队列等待有可能会进行超时
            outputResult = Result.ofcall(1,"你好客户端，准许接入！");
            writer();
            socket.setSoTimeout(latetimeOut);//读取超时
            while (true){//线程循环维护主逻辑
                try {//监听数据，进行数据交互
                    reader();//进行数据处理
                    if(inputResult.isSuccess()){
                        switch (inputResult.getCode()){
                            case 0:heartbeatResult();break;//心跳
                            case 1:authenticationResult();break;//认证
                            case 2:callResult();break;//调用
                            case 3:responseResult();break;//响应
                            case 4:dataResult();break;//数据
                            default:defaultResult();//未知
                        }
                        //定时更新
                    }else {
                        //主动进行消亡通知
                        outputResult= Result.ofThrowable(new Exception());
                        writer();
                        waitSocket();
                        deathSocket();
                        close();
                        return;
                    }
                }catch (SocketTimeoutException e){
                    if(late<3){
                        late++;
                    }else {
                        lose++;
                        late=0;
                    }
                    log.info("读取超时。。。。");
                }

                //进行活性检测，以及相应处理
                if(lose>2){
                    outputResult = Result.ofcall(0,"恢复心跳");
                    writer();
                    try {
                        reader();
                        continue;
                    } catch (IOException e) {}
                    waitSocket();
                    heartbeatTimer();
                    Thread.sleep(30*60*1000);
                    timer.cancel();
                    outputResult = Result.ofcall(0,"恢复心跳");
                    writer();
                    reader();
                    activeSocket();
                }
            }
        }catch (Exception e) {//处理非捕获性异常操作
            close();
            System.out.println(e);
        }
    }

    public void callByMethod(TbMethod method) throws IOException {
        outputResult=Result.ofcall(2,JsonUtils.objectToJson(method));
        writer();
    }

    public void testByData(Result result) throws IOException {
        outputResult=result;
        writer();
    }

    private void heartbeatTimer(){//进行心跳超时记录
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                outputResult = Result.ofcall(0,"恢复心跳");
                try {
                    writer();
                } catch (IOException e) {
                    deathSocket();
                }
                try {reader();
                    thread.interrupt();
                } catch (IOException e) {}
            }
        },0,5000);
    }

    private void heartbeatResult() throws SocketTimeoutException {//处理心跳指令
        late=0;lose=0;
        log.debug("收到心跳指令，正在处理");
        if(tbEquipment.getEHeartbeat()!= 0L){
            tbEquipment.setEShake((int) (tbEquipment.getEHeartbeat()-Long.parseLong(inputResult.getMsg())));
        }
        tbEquipment.setEHeartbeat(Long.parseLong(inputResult.getMsg()));
        tbEquipment.setEStatus(1);
        try {
            outputResult = Result.ofcall(0,"收到心跳");
            writer();
        } catch (IOException e) {}
    }
    private void authenticationResult() throws SocketTimeoutException {//处理认证指令
        log.error("socket连接维护中发现错误认证指令，已进行忽视。。。");
        throw new SocketTimeoutException();
    }
    private void callResult() throws SocketTimeoutException {//处理调用指令
        log.error("socket连接维护中发现错误调用指令，已进行忽视。。。");
        throw new SocketTimeoutException();
    }
    private void responseResult() throws SocketTimeoutException {//处理响应指令
        late=0;lose=0;
        log.debug("收到响应指令，正在处理");
        builder=new StringBuilder(inputResult.getMsg());
    }
    private void dataResult() throws SocketTimeoutException {//处理数据指令
        log.error("socket连接维护中发现错误数据指令，已进行忽视。。。");
        throw new SocketTimeoutException();
    }
    private void defaultResult() throws SocketTimeoutException {//处理未知指令
        log.error("socket连接维护中发现错误未知指令，已进行忽视。。。");
        throw new SocketTimeoutException();
    }

    //资源结束处理
    private void close(){
        deathSocket();
        connectionPool=null;
        devicePool=null;
        thread=null;
        inputResult=null;
        outputResult=null;
        builder=null;
        tbEquipment=null;
        timer.cancel();
        try {
            if(socket!=null){
                socket.close();
            }
            if(inputStream!=null){
                inputStream.close();
            }
            if(outputStream!=null){
                outputStream.close();
            }
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
        } catch (IOException e) {log.debug("流资源关闭失败"+e);}
    }

    //资源池维护(通过连接状态维护设备池和连接对象池)
    //线程资源活化
    private void activeSocket(){
        connectionPool.activeSocketRunnable(tbEquipment.getEId());
        devicePool.activeEquipment(tbEquipment);
        connectionPool.test();
        devicePool.test();
    }
    //线程资源失活
    private void waitSocket(){
        connectionPool.waitSocketRunnable(tbEquipment.getEId());
        devicePool.inactivationEquipment(tbEquipment);
        connectionPool.test();
        devicePool.test();
    }
    //结束线程资源
    private void deathSocket(){
        connectionPool.dellSocketRunnable(tbEquipment.getEId());
        devicePool.eliminateEquipment(tbEquipment);
        connectionPool.test();
        devicePool.test();
    }

    private void init() throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.outputStream));
        socket.setSoTimeout(timeOut);//读取超时
    }

    public TbEquipment getTbEquipment() {
        return tbEquipment;
    }
}
