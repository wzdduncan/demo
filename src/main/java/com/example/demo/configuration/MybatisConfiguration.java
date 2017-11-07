package com.example.demo.configuration;

import com.example.demo.dao.base.SqlMapper;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis配置类
 * @author wayne
 */

@Configuration
public class MybatisConfiguration {
	
	/**
	 * mybatis的mapper扫描配置
	 * @return
	 */
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage("com.example.demo.dao");
        mapperScannerConfigurer.setMarkerInterface(SqlMapper.class);
        return mapperScannerConfigurer;
    }
}
