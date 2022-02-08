package top.leju.homefurnishing.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TbParameter implements Serializable {
    private String eMPId;//参数主键id
    private String eMPName;//参数名称
    private String eMPValue;//参数值
}
