package com.sys.mybatis.teacher;

import com.sys.mybatis.teacher.bean.User;
import com.sys.mybatis.teacher.caches.DiskCache;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.mapping.StatementType;
import org.springframework.stereotype.Component;

import java.util.List;

//持久化到硬盘
//@CacheNamespace(implementation = DiskCache.class,properties = {@Property(name = "cachePath", value ="target" )})
//溢出淘汰算法，设置大小为 10
@CacheNamespace(eviction = LruCache.class, size = 10)
public interface UserMapper {

    @Select({"select * from users where id=#{1}"})
//    @Options(flushCache = Options.FlushCachePolicy.TRUE) //设置清空缓存 <==> sqlSession.clearCache();
    User selectByid(Integer id);


    @Select({"select * from users where id=#{1}"})
    User selectByid3(Integer id);

    @Select({" select * from users where name='${name}'"})
    @Options(statementType = StatementType.PREPARED)
    List<User> selectByName(User user);

    List<User> selectByUser(User user);

    @Insert("INSERT INTO `users`( `name`, `age`, `sex`, `email`, `phone_number`) VALUES ( #{name}, #{age}, #{sex}, #{email}, #{phoneNumber})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addUser(User user);

    int editUser(User user);

    @Update("update  users set name=#{arg1} where id=#{arg0}")
    int setName(Integer id, String name);

    @Delete("delete from users where id=#{id}")
    int deleteUser(Integer id);
}
