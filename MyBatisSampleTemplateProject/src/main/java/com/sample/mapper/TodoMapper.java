package com.sample.mapper;

import com.sample.dbmodel.Todo;
import java.sql.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TodoMapper {

    List<Todo> findByTitle(@Param("title") String title);

    List<Todo> findById(@Param("id") Integer id);

    Integer getMaxId();

    int insert(Todo todo);

    int delete(@Param("id") Integer id);

    int update(Todo todo);

    int checkTableExists();
}
