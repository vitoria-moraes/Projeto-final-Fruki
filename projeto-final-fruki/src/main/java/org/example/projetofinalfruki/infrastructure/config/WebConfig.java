package org.example.projetofinalfruki.infrastructure.config;

import org.example.projetofinalfruki.application.controller.CatalogoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CatalogoInterceptor catalogoInterceptor;

    public WebConfig(CatalogoInterceptor catalogoInterceptor) {
        this.catalogoInterceptor = catalogoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(catalogoInterceptor)
                .addPathPatterns("/catalogo/**"); // Protege todas as rotas do catálogo com o JWT
    }
}