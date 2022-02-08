package top.leju.homefurnishing.pojo;

import java.util.Date;

/**
 * 返回对象
 *
 * 应用状态：分为通过转化为json，传输到客户端，客户端也会模拟该对象格式json，进行交互
 * 如果success返回false，将代表结果对象返回的是错误信息，设备记录错误日志
 * 如果success返回true，将代表结果对象返回的是指令信息，其中code是指令类型，msg是指令，data是附加数据
 *
 * 指令码code：
 *      0：心跳
 *      1: 认证
 *      2：调用
 *      3：响应
 *      4：数据
 *
 * 在传输过程中是通过json进行传输的，Result对象先json化在加密(Base64)，进行tcp传输，对端进行解密，在进行对象化。
 * 在json化和对象化中，Result对象中的附属对象，也应该进行json化，对象化时，将json提取出进行，对象化
 * Result转json对象，结束符为"\n"
 *
 * 接入响应流程：
 *
 *
 *
 *
 * @param <R>
 */
public class Result<R> {

    private boolean success = false;//是否成功
    private int code = -1;//指令码
    private String msg = "";//响应信息
    private R data = null;//响应数据

    public static <R> Result<R> ofData(R data) {//直接传入数据
        return new Result<R>()
                .setSuccess(true)
                .setCode(4)
                .setMsg("data")
                .setData(data);
    }

    public static <R> Result<R> ofcall(int code, String msg) {//传入指令码和指令信息
        Result<R> result = new Result<>();
        result.setSuccess(true);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <R> Result<R> ofThrowable(Throwable throwable) {//传入状态码和异常
        Result<R> result = new Result<>();
        result.setSuccess(false);
        result.setMsg(throwable.getClass().getName() + ", " + throwable.getMessage() + ", " + new Date());
        return result;
    }



    /*
    参数setter/getter-----------------------------------------------------
     */
    public boolean isSuccess() {//判断是否响应成功
        return success;
    }

    public Result<R> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public int getCode() {
        return code;
    }

    public Result<R> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<R> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public R getData() {
        return data;
    }

    public Result<R> setData(R data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}