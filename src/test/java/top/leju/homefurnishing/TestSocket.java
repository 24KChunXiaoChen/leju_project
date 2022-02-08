package top.leju.homefurnishing;

import org.junit.jupiter.api.Test;
import top.leju.homefurnishing.pojo.Result;
import top.leju.homefurnishing.pojo.TbEquipment;
import top.leju.homefurnishing.utils.Base64Util;
import top.leju.homefurnishing.utils.JsonUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class TestSocket {

    Timer timer;
    Socket socket;
    Result inputResult;
    Result outputResult;

    InputStream inputStream;
    OutputStream outputStream;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;



    @Test
    void contextLoads() throws IOException {
        socket = new Socket("127.0.0.1",8888);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.outputStream));
        //socket.setSoTimeout(1*1000);//读取超时
        outputResult=Result.ofcall(1,"你好服务器，请求接入！");
        writer();
        reader();
        if(inputResult.isSuccess() && inputResult.getCode()==1){
            TbEquipment equipment = JsonUtils.jsonToPojo("{\"tbMethods\":[{\"emname\":\"开灯\",\"emparameters\":[{\"empid\":\"c3ca233e-c054-4e3a-a247-cdfe6c14afc9\",\"empvalue\":\"0\",\"empname\":\"延迟开灯\"}],\"emid\":\"fdc83526-65b5-4226-9855-1299389b8c09\",\"emdescribe\":\"打开模拟灯，将灯状态置为开。\"},{\"emname\":\"亮度调节\",\"emparameters\":[{\"empid\":\"adc59f19-b2dd-4b80-9174-d167f0508ef0\",\"empvalue\":\"0\",\"empname\":\"延迟调节\"},{\"empid\":\"6266e062-02c7-41d8-9e7a-dd06a5ddb21b\",\"empvalue\":\"0\",\"empname\":\"开灯亮度\"}],\"emid\":\"930d7ed2-5985-4d65-8ea4-4042593420a7\",\"emdescribe\":\"调节模拟灯亮度，将灯亮度设置为指定亮度。\"}],\"edescribe\":\"台灯设备，模拟的台灯\",\"etype\":\"TableLamp\",\"estatus\":0,\"eshake\":0,\"emac\":\"A9:7A:36:33:54:48\",\"eid\":\"d1f03370-bcfb-45db-a186-52d6dbb31a9f\",\"edynamic\":{\"灯亮度\":\"0\",\"灯状态\":\"关闭\"},\"eip\":\"127.0.0.1\",\"eheartbeat\":0,\"ename\":\"模拟台灯\"}\n",TbEquipment.class);
            outputResult=Result.ofData(JsonUtils.objectToJson(equipment));
            writer();
            reader();
            if(inputResult.isSuccess() && inputResult.getCode()==1){
                System.out.println(inputResult);
                //socket.setSoTimeout(10*1000);
                reader();
                if(inputResult.isSuccess() && inputResult.getCode()==1){
                    System.out.println(inputResult);
                    //socket.setSoTimeout(1*1000);
                    heartbeatTimer();
                    while (true){
                        try {
                            //心跳
                            reader();
                            if (inputResult.isSuccess()){
                                switch (inputResult.getCode()){
                                    case 0:System.out.println("心跳指令："+inputResult);break;
                                    case 1:System.out.println("模拟调用方法："+inputResult.getMsg());break;
                                    default:System.out.println("未知指令。。。");
                                }
                            }else {
                                System.out.println("服务器请求结束连接");
                                break;
                            }
                        }catch (IOException e){}
                    }
                }
                System.out.println(inputResult);
                System.out.println("客户端即将退出，baybay。");
                return;
            }
        }
    }

    private void heartbeatTimer(){//进行心跳超时记录
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                outputResult = Result.ofcall(0,new Date().getTime()+"");
                try {
                    writer();
                } catch (IOException e) { }
            }
        },0,5000);
    }

    public void reader() throws IOException {
        inputResult = JsonUtils.jsonToPojo(Base64Util.decode(bufferedReader.readLine()), Result.class);
    }

    public void writer() throws IOException {
        bufferedWriter.write(Base64Util.encode(JsonUtils.objectToJson(outputResult))+"\n");
        bufferedWriter.flush();
    }

}
