package com.jhappy.mybateans.action;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MybatisLogFormatterTest {

    static String str1 = """
DEBUG [org.mybatis.example.TestMapper] - ==>  Preparing: SELECT * FROM test_table WHERE id = ?
DEBUG [org.mybatis.example.TestMapper] - ==> Parameters: 1(Integer)
DEBUG [org.mybatis.example.TestMapper] - <==      Total: 1
DEBUG [org.mybatis.example.TestMapper] - ==>  Preparing: SELECT * FROM test_table WHERE id = ? and user = ?
DEBUG [org.mybatis.example.TestMapper] - ==> Parameters: 1(Integer),T()ES,T(String)
DEBUG [org.mybatis.example.TestMapper] - <==      Total: 1
        """;

    @Test
    void testParseNormal() {

        List<SqlLog> sqlLogList = MybatisLogFormatter.exactLog(str1);

        assertEquals(2, sqlLogList.size());

        List<Param> params1 = MybatisLogFormatter.parseSafe(sqlLogList.get(0).parameter.parameter);
        assertEquals(1, params1.size());
        assertEquals("Integer", params1.get(0).type);
        assertEquals("1", params1.get(0).value);

        List<Param> params2 = MybatisLogFormatter.parseSafe(sqlLogList.get(1).parameter.parameter);
        assertEquals(2, params2.size());
        assertEquals("Integer", params2.get(0).type);
        assertEquals("1", params2.get(0).value);
        assertEquals("String", params2.get(1).type);
        assertEquals("T()ES,T", params2.get(1).value);

    }

    @Test
    void testBrokenLog() {
        String input = "1(Integer),T()ES,T(String)";

        List<Param> params = MybatisLogFormatter.parseSafe(input);

        assertNotNull(params);
    }

    @Test
    void testBuildSql() {
        String sql = "SELECT * FROM test WHERE id = ?";

        List<Param> params = List.of(new Param("1", "Integer"));

        String result = MybatisLogFormatter.buildSql(sql, params);

        assertEquals("SELECT * FROM test WHERE id = 1", result);
    }
}
