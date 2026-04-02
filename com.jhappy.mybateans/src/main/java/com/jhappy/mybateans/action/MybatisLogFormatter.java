package com.jhappy.mybateans.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MybatisLogFormatter {

    private static final List<String> NON_QUAOTE_TYPES = Arrays.asList("Integer", "Long", "Double",
            "Float", "Boolean");
    
   private static final Set<String> KNOWN_TYPES = Set.of(
            "Integer", "Long", "Double", "Float", "Short", "Byte",
            "Boolean",
            "String", "Character",
            "BigDecimal",
            "Date", "Timestamp",
            "LocalDate", "LocalDateTime", "LocalTime",
            "byte[]"
    );


    public static List<SqlLog> exactLog(String text) {

        String regex = "(?s)==>\\s+Preparing:\\s*(.*?)\\s*DEBUG.*?==>\\s+Parameters:(.*?)\\s*DEBUG";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<SqlLog> result = new ArrayList<>();

        while (matcher.find()) {

            String sql = matcher.group(1).trim();
            String params = matcher.group(2).trim();
            SqlLog sqllog = new SqlLog(new Statement(sql), new Parameter(params));
            result.add(sqllog);
        }

        return result;

    }

    public static String format(String src) {

        List<SqlLog> sqllosglist = exactLog(src);

        List<String> result = new ArrayList<>();

        for (SqlLog sqllog : sqllosglist) {
            String sql = sqllog.statement.sql;
            String params = sqllog.parameter.parameter;

            List<Param> paramlist = parseSafe(params);

            String sqlwithparam = buildSql(sql, paramlist);

            result.add(sqlwithparam);
        }

        return String.join("\n-----------------\n", result);
    }

    public static String buildSql(String sql, List<Param> params) {

        StringBuilder result = new StringBuilder();

        int paramIndex = 0;
        int len = sql.length();

        for (int i = 0; i < len; i++) {
            char c = sql.charAt(i);

            if (c == '?' && paramIndex < params.size()) {

                Param p = params.get(paramIndex++);

                String value = formatValue(p);

                result.append(value);

            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    static class Block {

        int start; // '('
        int end;   // ')'
        String type;

        Block(int start, int end, String type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }
    }

    public static List<Param> parseSafe(String input) {

        List<Block> blocks = new ArrayList<>();
        int i = input.length() - 1;

        while (i >= 0) {

            if (input.charAt(i) != ')') {
                i--;
                continue;
            }

            int end = i;
            i--;

            StringBuilder typeBuilder = new StringBuilder();
            while (i >= 0 && input.charAt(i) != '(') {
                typeBuilder.append(input.charAt(i));
                i--;
            }

            if (i < 0) {
                break;
            }

            int start = i;

            String type = typeBuilder.reverse().toString().trim();

            if (!type.isEmpty() && KNOWN_TYPES.contains(type)) {
                blocks.add(0, new Block(start, end, type));
            }

            i = start - 1;
        }

        List<Param> result = new ArrayList<>();

        int prevEnd = -1;

        for (int idx = 0; idx < blocks.size(); idx++) {
            Block b = blocks.get(idx);

            int valueStart = (prevEnd == -1) ? 0 : prevEnd + 1;
            int valueEnd = b.start;

            String value = input.substring(valueStart, valueEnd);

            if (value.startsWith(",")) {
                value = value.substring(1);
            }

            result.add(new Param(value, b.type));

            prevEnd = b.end;
        }

        return result;
    }

    private static String formatValue(Param p) {

        if (p.value == null || p.value.equalsIgnoreCase("null")) {
            return "null";
        }

        // 数値系はそのまま
        if (NON_QUAOTE_TYPES.contains(p.type)) {
            return p.value;
        }

        // それ以外はクォート
        return "'" + p.value.replace("'", "''") + "'";
    }
}

class Param {

    String value;
    String type;

    Param(String value, String type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return "type:" + type + "  value:" + value;
    }

}

class Statement {

    Statement(String sql) {
        this.sql = sql;
    }
    String sql;
}

class Parameter {

    Parameter(String parameter) {
        this.parameter = parameter;
    }
    String parameter;
}

class SqlLog {

    SqlLog(Statement statement, Parameter parameter) {
        this.statement = statement;
        this.parameter = parameter;

    }

    Statement statement;
    Parameter parameter;
}
