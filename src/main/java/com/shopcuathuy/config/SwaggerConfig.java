package com.shopcuathuy.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ShopCuaThuy API",
        version = "1.0.0",
        description = "REST API cho nền tảng thương mại điện tử ShopCuaThuy"
    ),
    security = @SecurityRequirement(name = "bearer-jwt"),
    servers = {
        @Server(url = "/", description = "Default environment"),
        @Server(url = "http://localhost:8080", description = "Local development")
    }
)
@SecurityScheme(
    name = "bearer-jwt",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact()
            .name("ShopCuaThuy Team")
            .url("https://shopcuathuy.dev")
            .email("support@shopcuathuy.dev");

        License license = new License()
            .name("Apache 2.0")
            .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .name("Authorization")))
            .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearer-jwt"))
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("ShopCuaThuy API")
                .version("1.0.0")
                .description("Tài liệu OpenAPI cho hệ thống thương mại điện tử ShopCuaThuy")
                .contact(contact)
                .license(license));
    }
}

