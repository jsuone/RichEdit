package com.example.android_note.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @className: WebConfig
 * @description: TODO 类描述
 * @date: 2023/5/1613:58
 **/
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
       registry.addResourceHandler("/res/**")/**静态资源虚拟路径设置**/
               .addResourceLocations("classpath:/static/")
               .setCachePeriod(0);
    }
}
