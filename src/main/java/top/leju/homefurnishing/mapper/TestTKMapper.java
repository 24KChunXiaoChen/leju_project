package top.leju.homefurnishing.mapper;


import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import top.leju.homefurnishing.pojo.User;

/**
 * 通用mapper使用
 */
@Repository
public interface TestTKMapper extends Mapper<User> {}
