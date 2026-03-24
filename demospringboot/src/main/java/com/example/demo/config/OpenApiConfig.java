package com.example.demo.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("用户管理系统 API")
                        .version("1.0.0")
                        .description("提供用户登录、注册等功能的接口文档")
                        .contact(new Contact()
                                .name("毕业设计")
                                .email("2597369226@qq.com")
                        )
                );
    }

}
