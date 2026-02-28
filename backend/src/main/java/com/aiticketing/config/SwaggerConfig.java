package com.aiticketing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "AI-Powered Automated Ticket Management API",
        version = "v1",
        description =
            """
            Backend API for AI-powered automated ticket management platform.

            - User registration/login (JWT)
            - Ticket creation + listing + details
            - Agent assigned ticket views
            - Admin user role updates
            - Comments, overrides, status updates (extensible for AI worker)
            """
    )
)
public class SwaggerConfig {
	
	@Bean
    public OpenAPI openAPI() {
		
		//Schemas for ApiResponseBean & ValidationError
        Schema<?> validationErrorSchema = new ObjectSchema()
                .addProperty("field", new StringSchema().example("email"))
                .addProperty("message", new StringSchema().example("must be a well-formed email address"));

        Schema<?> apiResponseSchema = new ObjectSchema()
                .addProperty("success", new BooleanSchema().example(true))
                .addProperty("message", new StringSchema().nullable(true).example("Ticket created"))
                .addProperty("data", new ObjectSchema().nullable(true))
                .addProperty("errors", new ArraySchema().items(validationErrorSchema).nullable(true));

        Components components = new Components()
                .addSchemas("ValidationError", validationErrorSchema)
                .addSchemas("ApiResponseBean", apiResponseSchema)
        		.addSecuritySchemes("bearerAuth", new SecurityScheme()
	                    .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
	                    .scheme("bearer")
	                    .bearerFormat("JWT")
	            );
        return new OpenAPI()
            .components(components)
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("AI-Powered Automated Ticket Management API")
                .version("v1")
                .description(
                    "APIs for user auth and ticket management. Responses are wrapped in ApiResponseBean for consistency."
                )
            );
    }
}
