package top.leju.homefurnishing.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.leju.homefurnishing.dao.ThreadPoolDao;
import top.leju.homefurnishing.mapper.EquipmentMapper;
import top.leju.homefurnishing.mapper.TestTKMapper;
import top.leju.homefurnishing.pojo.User;

import java.util.Date;

@Controller
public class TestController {
    @Autowired
    EquipmentMapper mapper;
    @Autowired
    ThreadPoolDao dao;


    @GetMapping("test")
    @ResponseBody
    @Transactional
    public String test() throws Exception {
        return mapper.findEquipmentAll().toString();
    }


    private Long getID (){
        return new Date().getTime()*1000+(long) (Math.random()*1000);
    }
}
