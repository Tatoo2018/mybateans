<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
   
  
    <settings>
        <setting name="mapUnderscoreToCamelCase" value="true"/>
        <setting name="logImpl" value="SLF4J"/>
    </settings>
    
  
    <typeAliases>
        <package name="com.sample.dbmodel"/>

    </typeAliases>
 
    <environments default="dev">
        <environment id="dev">
            <transactionManager type="${transactionManagerType}"/>
            <dataSource type="${dataSourceType}">
                <#if dataSourceType == "UNPOOLED" || dataSourceType == "POOLED">
                <property name="driver" value="${jdbcDriver}"/>
                <property name="url" value="${jdbcUrl}"/>
                <property name="username" value="${user}"/>
                <property name="password" value="${password}"/>
                </#if>
                <#if dataSourceType == "POOLED">
                <property name="poolMaximumActiveConnections" value="10"/>
                <property name="poolMaximumIdleConnections" value="5"/>
                </#if>
                <#if dataSourceType == "JNDI">
                <property name="data_source" value="${jndiName}"/>
                </#if>
            </dataSource>
        </environment>
    </environments>
    
    <mappers>
        <package name="com.sample.mapper"/>
    </mappers>
  
</configuration>
