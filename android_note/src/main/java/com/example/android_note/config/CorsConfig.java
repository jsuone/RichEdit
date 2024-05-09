package com.example.android_note.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @className: CorsConfig
 * @description: TODO 类描述
 * @date: 2023/5/100:26
 **/
@Configuration
public class CorsConfig {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOriginPatterns("*")
//                .allowedHeaders("*")
//                .allowedMethods("GET", "POST", "DELETE", "PUT")
//                .allowCredentials(true);
//    }
/**当使用  addCorsMappings 时如果请求被过滤器拦截是不会加上跨域请求回应的。因为是在servlet容器里进行添加跨域允许信息的。过滤器在请求到达容器之前就拦截了**/

  /**FilterRegistrationBean是一个注册过滤器的bean，可以查看它的父类信息会发现顶层有一个ServletContextInitializer接口，
   *  ServletContextInitializer是用于在Servlet 3.0+容器中注册过滤器的接口，它具有与ServletContext相似的注册功能，但设计更符合Spring应用程序的特点。可能是统一管理，依赖注入，可以利用spring框架特性更灵活
   *ServletContextInitializer的文档介绍中翻译如下
   * 该接口用于以编程方式配置Servlet 3.0+上下文。与WebApplicationInitializer不同，实现该接口的类（并且不实现WebApplicationInitializer）不会被SpringServletContainerInitializer检测到，因此不会被Servlet容器自动引导。
   *
   * 该接口的设计目的是以类似于ServletContainerInitializer的方式操作，但其生命周期由Spring管理而不是Servlet容器。
   *
   * 简而言之，实现该接口的类可以在Spring中以编程方式配置Servlet上下文，而无需依赖Servlet容器的自动引导。它提供了更多的灵活性和控制权，可以与Spring的生命周期和其他组件集成得更好。
   *
   *  **/
  @Bean
  FilterRegistrationBean corsFilter(){

   //CorsFilter(CorsConfigurationSource configSource)->UrlBasedCorsConfigurationSource implements CorsConfigurationSource->CorsConfiguration
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedHeader(CorsConfiguration.ALL);
    configuration.setAllowCredentials(true);//是否发送身份验证请求
    configuration.addAllowedMethod(CorsConfiguration.ALL);
    //configuration.addAllowedOrigin(CorsConfiguration.ALL);
    configuration.addAllowedOriginPattern(CorsConfiguration.ALL);
    configuration.setMaxAge(1800L);
    /**CorsConfigurationSource that uses URL path patterns to select the CorsConfiguration for a request.
     * UrlBasedCorsConfigurationSource实现了CorsConfigurationSource接口，在这个类中配置的url用于匹配请求路径，确定哪些请求需要加上CorsConfiguration配置
     * **/
    UrlBasedCorsConfigurationSource  source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**",configuration);
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new CorsFilter(source));
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);/**优先级设置**/
    return filterRegistrationBean;
  }
}
