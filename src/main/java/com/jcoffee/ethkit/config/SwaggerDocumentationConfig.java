package com.jcoffee.ethkit.config;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerDocumentationConfig {
    public SwaggerDocumentationConfig() {
    }

    ApiInfo apiInfoVcgBoss() {
        return (new ApiInfoBuilder()).title("adam-lua 1.0相关接口").description("adam-lua相关接口").license("adam-lua 1.0").licenseUrl("").termsOfServiceUrl("").version("1.0").build();
    }

    @Bean
    public Docket customImplementationVcgBoss() {
        Set<String> protocols = new HashSet();
        protocols.add("http");
        return (new Docket(DocumentationType.SWAGGER_2)).groupName("1-vcgboss").select().apis(RequestHandlerSelectors.basePackage("com.adam")).build().directModelSubstitute(LocalDate.class, Date.class).directModelSubstitute(DateTime.class, java.util.Date.class).apiInfo(this.apiInfoVcgBoss()).protocols(protocols);
    }
}
