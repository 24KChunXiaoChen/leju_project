package top.leju.homefurnishing.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Data//自动生成方法
@Table(name = "tb_project")//表名，默认为类名
public class User implements Serializable {
    @Id//主键
    @KeySql(useGeneratedKeys = true)//自增主键
    private Long p_id; //项目自增主键id
    private String p_name; //项目名称
    private Integer p_status; //项目状态,0未启用，1以启用，2已禁用
    private Integer p_tagging; //标注状态,0未分配，1已分配
    private Integer u_id; //标注人员id
    private Date p_create_time; //项目创建时间
    private Date p_update_time; //最后更新时间
    private Integer create_u_id; //创建者id
    private Integer parent_p_id; //父项目id
    private Integer p_level; //项目层级
    private String p_type; //项目类型(预留)，文本，视频，音频
    private Integer p_control; //项目权限(预留),设想用于控制图谱用户访问，检索

    //备注
    @Transient//不进行SQL添加
    private String note;
}
