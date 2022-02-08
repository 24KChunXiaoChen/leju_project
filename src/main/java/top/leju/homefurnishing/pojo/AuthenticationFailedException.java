package top.leju.homefurnishing.pojo;

//设备认证失败异常
public class AuthenticationFailedException extends Exception {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
