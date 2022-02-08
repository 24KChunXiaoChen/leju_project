package top.leju.homefurnishing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import top.leju.homefurnishing.config.ThreadPoolConfig;
import top.leju.homefurnishing.mapper.Impl.EquipmentMapperImpl;
import top.leju.homefurnishing.mapper.EquipmentMapper;
import top.leju.homefurnishing.pojo.*;
import top.leju.homefurnishing.utils.Base64Util;
import top.leju.homefurnishing.utils.JsonUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;

@SpringBootTest
class HomeFurnishingApplicationTests {
    @Autowired
    EquipmentMapperImpl mapper;

    @Transactional
    @Test
    void test(){
        TbEquipment e = mapper.getTbEquipment("7c5be053-8c49-4657-9e64-465598d6f341");
        show(e);
        e.setEId(UUID.randomUUID().toString());
        e.setEMac("A9:7A:36:33:54:48");
        for(TbMethod m:e.getTbMethods()){
            m.setEMId(UUID.randomUUID().toString());
            for (TbParameter p:m.getEMParameters()) {
                p.setEMPId(UUID.randomUUID().toString());
            }
        }
        mapper.setTbEquipment(e);
        System.out.println(JsonUtils.objectToJson(mapper.getTbEquipment(e.getEId())));
        System.out.println(mapper.getTbEquipments());
    }

    void show(TbEquipment e){
        System.out.println("固定参数："+e.getEId());
        System.out.println("固定参数："+e.getEName());
        System.out.println("固定参数："+e.getEDescribe());
        System.out.println("固定参数："+e.getEType());
        System.out.println("固定参数："+e.getEMac());
        System.out.println("固定参数："+e.getEIp());
        System.out.println("固定参数："+e.getEStatus());
        System.out.println("固定参数："+e.getEHeartbeat());
        System.out.println("固定参数："+e.getEShake());
        HashMap<String, String> d = e.getEDynamic();
        for (String key:d.keySet()) {
            System.out.println("动态参数："+key+" = "+d.get(key));
        }
        List<TbMethod> ms = e.getTbMethods();
        for (TbMethod m:ms) {
            List<TbParameter> ps = m.getEMParameters();
            System.out.println("方法：\n"+
                    "   方法ID："+m.getEMId()+"\n"+
                    "   方法名称："+m.getEMName()+"\n"+
                    "   方法描述："+m.getEMDescribe()
            );
            for (TbParameter p:ps) {
                System.out.println("    参数：\n       参数ID："+p.getEMPId()+"\n"+
                        "      参数名称："+p.getEMPName()+"\n"+
                        "      参数值："+p.getEMPValue()
                );
            }
        }
    }


}
