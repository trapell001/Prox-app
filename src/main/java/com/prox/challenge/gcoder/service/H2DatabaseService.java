package com.prox.challenge.gcoder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class H2DatabaseService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void changeTypeColumn(String table, String column, Type type){
        String query = """
                ALTER TABLE %table ADD COLUMN new_column %type;
                UPDATE %table SET new_column = %column;
                ALTER TABLE %table DROP COLUMN %column;
                ALTER TABLE %table RENAME COLUMN new_column TO %column;
                """
                .replaceAll("%table", table)
                .replaceAll("%type", type.typeName)
                .replaceAll("%column", column);
        jdbcTemplate.execute(query);
    }



    public List<Map<String, Object>> getTableNames() {
        String query = "SHOW TABLES";
        return jdbcTemplate.queryForList(query);
    }
    public static class TableEntity{
        public String name;
        public String schema ;
    }
    public List<Map<String, Object>> getTableColumns(String tableName) {
        String query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?";
        return jdbcTemplate.queryForList(query, tableName);
    }
    public enum Type {
        INT("INT"),
        BIGINT("BIGINT"),
        BOOLEAN("BOOLEAN"),
        DOUBLE("DOUBLE"),
        VARCHAR("VARCHAR"),
        CHAR("CHAR"),
        LONGVARCHAR("LONGVARCHAR"),
        BINARY("BINARY"),
        VARBINARY("VARBINARY"),
        BLOB("BLOB"),
        CLOB("CLOB"),
        DATE("DATE"),
        TIME("TIME"),
        TIMESTAMP("TIMESTAMP");

        public String typeName;

        Type(String typeName) {
            this.typeName = typeName;
        }
    }

}
