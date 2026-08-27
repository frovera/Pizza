package it.fr.awesomepizza.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI awesomePizzaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Awesome Pizza API")
                .version("1.0")
                .description("API per la gestione degli ordini della pizzeria"));
    }
}
