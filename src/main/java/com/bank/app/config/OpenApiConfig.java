package com.bank.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingOpenApi() {
        return new OpenAPI().info(
                new Info()
                        .title("Enterprise Banking Platform API")
                        .version("v1")
                        .description("Production-ready Digital Banking and Operations API")
                        .contact(new Contact().name("Bank Platform Team").email("platform@bank.local"))
                        .license(new License().name("Proprietary"))
        );
    }
}
