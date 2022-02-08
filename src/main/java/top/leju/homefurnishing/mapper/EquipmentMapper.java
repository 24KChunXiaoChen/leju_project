package top.leju.homefurnishing.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;
import top.leju.homefurnishing.pojo.TbEquipment;
import top.leju.homefurnishing.pojo.TbMethod;
import top.leju.homefurnishing.pojo.TbParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 正经mybatis操作
 */
@Repository
public interface EquipmentMapper {

    @Select("SELECT * FROM tb_equipment")
    @Results(id = "equipmentPojo",value = {
            @Result(id = true,column = "e_id",property = "eId"),
            @Result(column = "e_name",property = "eName"),
            @Result(column = "e_describe",property = "eDescribe"),
            @Result(column = "e_type",property = "eType"),
            @Result(column = "e_mac",property = "eMac"),
            @Result(column = "e_ip",property = "eIp"),
            @Result(column = "e_status",property = "eStatus"),
            @Result(column = "e_heartbeat",property = "eHeartbeat"),
            @Result(column = "e_shake",property = "eShake"),
            //@Result(column = "e_id",property = "eDynamic",many = @Many(select = "top.leju.homefurnishing.mapper.EquipmentMapper.findDynamicByEId",fetchType = FetchType.DEFAULT)),
            @Result(column = "e_id",property = "tbMethods",many = @Many(select = "top.leju.homefurnishing.mapper.EquipmentMapper.findMethodsByEId",fetchType = FetchType.DEFAULT))
    })
    List<TbEquipment> findEquipmentAll();

    @Select("SELECT * FROM tb_equipment WHERE e_id = #{e_id}")
    @ResultMap("equipmentPojo")
    TbEquipment findEquipmentById(String e_id);

    @Select("SELECT e_d_name,e_d_value FROM tb_equipment_dynamic WHERE e_id = #{e_id}")
    @MapKey("e_d_name")
    HashMap<String,Map<String,String>> findDynamicByEId(String e_id);


    @Select("SELECT * FROM tb_equipment_methods WHERE e_id = #{e_id} ")
    @Results(id = "method",value = {
            @Result(id = true,column = "e_m_id",property = "eMId"),
            @Result(column = "e_m_name",property = "eMName"),
            @Result(column = "e_m_describe",property = "eMDescribe"),
            @Result(column = "e_m_id",property = "eMParameters",many = @Many(select = "top.leju.homefurnishing.mapper.EquipmentMapper.findParametersByEId",fetchType = FetchType.DEFAULT))
    })
    List<TbMethod> findMethodsByEId(String e_id);


    @Select("SELECT * FROM tb_equipment_methods_parameters WHERE e_m_id = #{e_m_id} ")
    List<TbParameter> findParametersByEId(String e_m_id);
    


    @Insert("INSERT INTO tb_equipment(e_id, e_name, e_describe, e_type, e_mac, e_ip, e_status, e_heartbeat, e_shake, e_dynamic, e_methods) VALUES (#{e_id}, #{e_name}, #{e_describe}, #{e_type}, #{e_mac}, #{e_ip}, #{e_status}, #{e_heartbeat}, #{e_shake}, #{e_dynamic}, #{e_methods}) ")
    void saveEquipment(@Param("e_id") String e_id,
                       @Param("e_name") String e_name,
                       @Param("e_describe") String e_describe,
                       @Param("e_type") String e_type,
                       @Param("e_mac") String e_mac,
                       @Param("e_ip") String e_ip,
                       @Param("e_status") Integer e_status,
                       @Param("e_heartbeat") Long e_heartbeat,
                       @Param("e_shake") Integer e_shake,
                       @Param("e_dynamic") String e_dynamic,
                       @Param("e_methods") String e_methods);

    @Insert("INSERT INTO tb_equipment_dynamic(e_d_id, e_d_name, e_d_value, e_id) VALUES (#{e_d_id}, #{e_d_name}, #{e_d_value}, #{e_id}) ")
    void saveDynamic(@Param("e_d_id") String e_d_id,
                     @Param("e_d_name") String e_d_name,
                     @Param("e_d_value") String e_d_value,
                     @Param("e_id") String e_id);

    @Insert("INSERT INTO tb_equipment_methods(e_m_id, e_m_name, e_m_describe, e_m_parameters, e_id) VALUES (#{e_m_id}, #{e_m_name}, #{e_m_describe}, #{e_m_parameters}, #{e_id}) ")
    void saveMethods(@Param("e_m_id") String e_m_id,
                     @Param("e_m_name") String e_m_name,
                     @Param("e_m_describe") String e_m_describe,
                     @Param("e_m_parameters") String e_m_parameters,
                     @Param("e_id") String e_id);

    @Insert("INSERT INTO tb_equipment_methods_parameters(e_m_p_id, e_m_p_name, e_m_p_value, e_m_id) VALUES (#{e_m_p_id}, #{e_m_p_name}, #{e_m_p_value}, #{e_m_id}) ")
    void saveParameters(@Param("e_m_p_id") String e_m_p_id,
                        @Param("e_m_p_name") String e_m_p_name,
                        @Param("e_m_p_value") String e_m_p_value,
                        @Param("e_m_id") String e_m_id);
}
