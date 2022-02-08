package top.leju.homefurnishing.pojo;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Data
public class TbEquipment implements Serializable {

    //设备基础信息，在每一个实例初始化时进行，基本不更改，所以相当于实例常量
    private String eId;//设备id
    private String eName;//设备名称
    private String eDescribe;//设备描述
    private String eType;//设备类型
    private String eMac;//设备mac
    private String eIp;//设备ip

    private Integer eStatus ;//网络状态,-1连接错误，0未连接，1已连接
    private Long eHeartbeat ;//服务器心跳，-1连接错误，0第一次连接，正数 上一次心跳毫秒数
    private Integer eShake ;//心跳抖动
    private HashMap<String,String> eDynamic ;//动态参数集合

    //调用方法信息
    private List<TbMethod> tbMethods;//方法集合


    public boolean isLegitimate(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TbEquipment equipment = (TbEquipment) o;
        return Objects.equals(eId, equipment.eId) &&
                Objects.equals(eType, equipment.eType) &&
                Objects.equals(eMac, equipment.eMac) &&
                Objects.equals(eDynamic, equipment.eDynamic) &&
                Objects.equals(tbMethods, equipment.tbMethods);
    }
}
