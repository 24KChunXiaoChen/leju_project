package top.leju.homefurnishing.pojo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.leju.homefurnishing.mapper.Impl.EquipmentMapperImpl;
import top.leju.homefurnishing.utils.JsonUtils;

import java.util.HashMap;

/**
 * 设备对象池
 */
@Slf4j
@Component
public class DevicePool {

    //设备持久化
    EquipmentMapperImpl mapper;

    //历史对象数
    private int history;//仅供数据读操作，只有初始化和新设备死亡时才进行操作，应该提供删除接口
    //活跃对象数
    private int active;//设备接入时构建，同步历史对象，先检查id和mac，通过后检查合法性，通过后接入，否则拒绝
    //失活对象数
    private int inactivation;//线程检查socket连接出错后进行失活处理，线程消亡后进行清除

    HashMap<String,TbEquipment> historyHashMap;//历史对象集
    HashMap<String,TbEquipment> activeHashMap;//活跃对象集
    HashMap<String,TbEquipment> inactivationHashMap;//失活对象集

    @Autowired
    public DevicePool(EquipmentMapperImpl mapper) {
        this.mapper=mapper;
        log.debug("对象池创建ing");
        init();
        log.debug("对象池创建成功！");
    }


    //启动时历史对象从数据库读取
    void init(){
        //参数初始化
        history=0;
        active=0;
        inactivation=0;
        historyHashMap = new HashMap<>();
        activeHashMap = new HashMap<>();
        inactivationHashMap = new HashMap<>();
        log.debug("参数初始化成功。。。");
        for (TbEquipment e :mapper.getTbEquipments()) {
            historyHashMap.put(e.getEId(),e);
        }
        history=historyHashMap.size();
        log.debug("参数装载成功。。。");
    }

    //对象失活操作
    public void inactivationEquipment(TbEquipment e){
        TbEquipment inactivation = activeHashMap.remove(e.getEId());
        if(inactivation==null){
            log.error("对象操作失败",new NullPointerException());
            return;
        }
        this.active=activeHashMap.size();
        setInactivationHashMap(e);
        log.debug("对象失活，"+e);
    }
    //对象活化操作
    public void activeEquipment(TbEquipment e){
        TbEquipment active = inactivationHashMap.remove(e.getEId());
        if(active==null){
            log.error("对象操作失败",new NullPointerException());
            return;
        }
        this.inactivation=inactivationHashMap.size();
        setActiveHashMap(e);
        log.debug("对象活化，"+e);
    }
    //对象清除操作
    public void eliminateEquipment(TbEquipment e){
        TbEquipment eliminate = activeHashMap.remove(e.getEId());
        if(eliminate==null){eliminate = inactivationHashMap.remove(e.getEId());}
        if(eliminate==null){
            log.error("对象操作失败",new NullPointerException());
            return;
        }
        this.active=activeHashMap.size();
        this.inactivation=inactivationHashMap.size();
        setHistoryEquipment(e);
        log.debug("对象凋亡，"+e);
    }

    //失活对象集操作
    private void setInactivationHashMap(TbEquipment equipment){
        inactivationHashMap.put(equipment.getEId(),equipment);
        this.inactivation=inactivationHashMap.size();
        log.debug("失活对象添加成功。"+equipment);
    }

    //活跃对象集操作
    private void setActiveHashMap(TbEquipment equipment){
        activeHashMap.put(equipment.getEId(),equipment);
        this.active=activeHashMap.size();
        log.debug("活跃对象添加成功。"+equipment);
    }

    //添加历史对象
    private void setHistoryEquipment(TbEquipment equipment){//用于失活对象进行更新历史
        historyHashMap.put(equipment.getEId(),equipment);
        this.history=historyHashMap.size();
        log.debug("历史对象添加成功，"+equipment);
    }

    //加入新设备
    public void addActiveHashMap(TbEquipment equipment){
        activeHashMap.put(equipment.getEId(),equipment);
        if(!isHistoryEquipment(equipment)){
            mapper.setTbEquipment(equipment);
            log.debug("数据库新增对象，"+equipment);
        }
        this.active=activeHashMap.size();
        log.debug("活跃对象添加成功。"+equipment);
    }


    //是否是已接入对象，正在维护的对象
    public boolean isAccessEquipment(TbEquipment equipment){
        TbEquipment access = activeHashMap.get(equipment.getEId());
        if(access!=null){return true;}
        access = inactivationHashMap.get(equipment.getEId());
        if(access!=null){return true;}
        return false;//如果检查没有，返回未接入
    }

    //historyHashMap历史对象集合
    //检查是否是合法历史对象
    public boolean isLegitimateEquipment(TbEquipment equipment){
        TbEquipment history = historyHashMap.get(equipment.getEId());
        return history.isLegitimate(equipment);//如果部分检查相同，返回合法
    }

    //检查eid和emac是否有相同
    public boolean isHistoryEquipment(TbEquipment equipment){
        TbEquipment history = historyHashMap.get(equipment.getEId());
        if(equipment==null || history==null){ return false; }
        //应该是有一个相等
        if(StringUtils.equals(history.getEId(),equipment.getEId()) ||
                StringUtils.equals(history.getEId(),equipment.getEId())){
            return true;
        }
        return false;
    }

    //从设备池中获取设备，失败处理，null
    public TbEquipment getEquipment(String eid){
        TbEquipment equipment = activeHashMap.get(eid);
        if(equipment==null){equipment = inactivationHashMap.get(eid);}
        if(equipment==null){equipment = historyHashMap.get(eid);}
        if(equipment==null){log.debug("设备获取失败，eid："+eid);return null; }
        return JsonUtils.jsonToPojo(JsonUtils.objectToJson(equipment),TbEquipment.class);
    }

    //判断是否是对象
    public int isEquipment(String eid){//-1.不是设备对象，0.历史对象，1失活对象，2活跃对象
        if(inactivationHashMap.get(eid)!=null){return 2; }
        if(activeHashMap.get(eid)!=null){return 1; }
        if(historyHashMap.get(eid)!=null){return 0; }
        return -1;
    }

    /*
    获取对象集合副本仅供展示
     */
    public HashMap<String,TbEquipment> getHistoryHashMap(){
        log.debug("设备池获取historyHashMap信息集："+historyHashMap);
        return JsonUtils.jsonToPojo(JsonUtils.objectToJson(historyHashMap),HashMap.class);
    }

    public HashMap<String,TbEquipment> getActiveHashMap(){
        log.debug("设备池获取activeHashMap信息集："+activeHashMap);
        return JsonUtils.jsonToPojo(JsonUtils.objectToJson(activeHashMap),HashMap.class);
    }

    public HashMap<String,TbEquipment> getInactivationHashMap(){
        log.debug("设备池获取inactivationHashMap信息集："+inactivationHashMap);
        return JsonUtils.jsonToPojo(JsonUtils.objectToJson(inactivationHashMap),HashMap.class);
    }

    /*
    简单连接池基础信息获取
     */
    public int getHistory() {
        log.debug("设备池获取history信息："+history);
        return history;
    }

    public int getActive() {
        log.debug("设备池获取active信息："+active);
        return active;
    }

    public int getInactivation() {
        log.debug("设备池获取Inactivation信息："+inactivation);
        return inactivation;
    }


    public void test() {
        String s="设备池{" +"\n"+
                "历史设备数=" + history +"\n"+
                ", 活跃设备数=" + active +"\n"+
                ", 失活设备数=" + inactivation +"\n"+
                '}';
        System.out.println(s);
    }
}
