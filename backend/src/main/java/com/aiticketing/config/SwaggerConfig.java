package com.aiticketing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "AI-Powered Automated Ticket Management API",
        version = "v1",
        description =
            """
            Backend API for AI-powered automated ticket management platform.

            Core Features (current):
            - User registration & login
            - Create and submit tickets
            - View ticket details
            - List tickets for a user

            Planned Features (next iterations):
            - AI-based ticket categorization & priority suggestions
            - Duplicate ticket detection using embeddings (pgvector)
            - Knowledge base suggestions and KB article drafting
            - Admin workflows (all tickets, overrides, audit trail)
            - Outbox/event-driven processing for async workflows

            Notes:
            - Most endpoints return a consistent wrapper: ApiResponseBean{ success, message, data, errors }.
            - Temporary user context header is used as of now: X-User-Id.
              (To be replaced by JWT Bearer authentication.)
            """
    )
)
@SecurityRequirement(name = "bearerAuth")
@SecurityScheme(
	    name = "bearerAuth",
	    type = SecuritySchemeType.HTTP,
	    scheme = "bearer",
	    bearerFormat = "JWT"
	)
public class SwaggerConfig {
	
	@Bean
    public OpenAPI openAPI() {
		
		// Build schemas once, attach to OpenAPI.components
        Schema<?> validationErrorSchema = new ObjectSchema()
                .addProperty("field", new StringSchema().example("email"))
                .addProperty("message", new StringSchema().example("must be a well-formed email address"));

        Schema<?> apiResponseSchema = new ObjectSchema()
                .addProperty("success", new BooleanSchema().example(true))
                .addProperty("message", new StringSchema().nullable(true).example("Ticket created"))
                // Generic data: leave as object. Specific endpoints will show concrete response types anyway.
                .addProperty("data", new ObjectSchema().nullable(true))
                .addProperty("errors", new ArraySchema().items(validationErrorSchema).nullable(true));

        Components components = new Components()
                .addSchemas("ValidationError", validationErrorSchema)
                .addSchemas("ApiResponseBean", apiResponseSchema);
        
        return new OpenAPI()
            .components(new Components())
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("AI-Powered Automated Ticket Management API")
                .version("v1")
                .description(
                    "APIs for user auth and ticket management. Responses are wrapped in ApiResponseBean for consistency."
                )
            );
    }
}
