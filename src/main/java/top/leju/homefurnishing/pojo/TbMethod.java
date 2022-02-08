package top.leju.homefurnishing.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TbMethod implements Serializable {
    private String eMId;//方法主键id
    private String eMName;//方法名称
    private String eMDescribe;//方法描述
    private List<TbParameter> eMParameters;//参数集合

}
