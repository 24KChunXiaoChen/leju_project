package top.leju.homefurnishing.mapper.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.leju.homefurnishing.mapper.EquipmentMapper;
import top.leju.homefurnishing.pojo.TbEquipment;
import top.leju.homefurnishing.pojo.TbMethod;
import top.leju.homefurnishing.pojo.TbParameter;

import java.util.*;

@Component
public class EquipmentMapperImpl {

    @Autowired
    EquipmentMapper mapper;

    public List<TbEquipment> getTbEquipments(){
        List<TbEquipment> equipmentAll = mapper.findEquipmentAll();
        for (TbEquipment e : equipmentAll) {
            HashMap<String,Map<String,String>> hashMap = mapper.findDynamicByEId(e.getEId());
            e.setEDynamic(mapToMap(hashMap));
        }
        return equipmentAll;
    }




    public TbEquipment getTbEquipment(String e_id){
        TbEquipment equipment = mapper.findEquipmentById(e_id);
        HashMap<String, Map<String, String>> hashMap = mapper.findDynamicByEId(equipment.getEId());
        equipment.setEDynamic(mapToMap(hashMap));
        return equipment;
    }


    public void setTbEquipment(TbEquipment e){
        mapper.saveEquipment(e.getEId(),e.getEName(),e.getEDescribe(),e.getEType(),e.getEMac(),e.getEIp(),0,0L,0,null,null);
        if(e.getEDynamic()!=null){
            for (String key:
                    e.getEDynamic().keySet()) {
                mapper.saveDynamic(UUID.randomUUID().toString(),key,e.getEDynamic().get(key),e.getEId());
            }
        }
        if(e.getTbMethods()!=null){
            for (TbMethod m:
                    e.getTbMethods()) {
                mapper.saveMethods(m.getEMId(),m.getEMName(),m.getEMDescribe(),null,e.getEId());
                if(m.getEMParameters()!=null){
                    for (TbParameter p:
                            m.getEMParameters()) {
                        mapper.saveParameters(p.getEMPId(),p.getEMPName(),p.getEMPValue(),m.getEMId());
                    }
                }
            }
        }
    }


    public void setTbEquipments(List<TbEquipment> list){
        for (TbEquipment equipment: list) {
            setTbEquipment(equipment);
        }
    }




    private HashMap<String,String> mapToMap(HashMap<String,Map<String,String>> hashMap){
        HashMap<String,String> dynamic = new HashMap();
        Set<String> set = hashMap.keySet();
        for (String s :set) {
            dynamic.put(s,hashMap.get(s).get("e_d_value"));
        }
        return dynamic;
    }
}
