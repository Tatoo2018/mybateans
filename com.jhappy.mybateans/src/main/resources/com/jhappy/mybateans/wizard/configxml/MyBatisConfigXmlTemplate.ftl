<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    
    <settings>
        <setting name="logImpl" value="STDOUT_LOGGING"/>
        <!-- <setting name="mapUnderscoreToCamelCase" value="true"/> -->
    </settings>
    
    <typeAliases>
      
        <!-- <package name="com.sample.dbmodel"/> -->
        <!-- <typeAlias alias="SampleModel" type="com.sample.dbmodel.SampleModel"/> -->
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
        <!-- <package name="com.sample.mapper"/> -->
        <!-- <mapper resource="com.sample.mapper.SampleMapper.xml"/> -->
    </mappers>
  
</configuration>
