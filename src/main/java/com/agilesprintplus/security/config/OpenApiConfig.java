package com.agilesprintplus.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Landry",
                        email = "ltchiengue73@gmail.com",
                        url = "++++++++++"
                ),
                description = "OpenApi documentation for AgileSprintPlus",
                title = "OpenApi specification - landryDev",
                version = "1.0",
                license = @License(name = "Licence name", url = "https://some-url.com"),
                termsOfService = "Terms of service"
        ),
        servers = {
                @Server(description = "Local ENV", url = "http://localhost:2025"),
                @Server(description = "PROD ENV", url = "++++++++++")
        },
        security = @SecurityRequirement(name = "bearer-jwt")
)
@SecurityScheme(
        name = "bearer-jwt",
        description = "Provide the JWT access token. Do NOT type the 'Bearer ' prefix here.",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {}
