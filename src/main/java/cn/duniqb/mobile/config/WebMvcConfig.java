package cn.duniqb.mobile.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //和页面有关的静态目录都放在项目的static目录下
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        //其中OTA表示访问的前缀。"file:D:/OTA/"是文件真实的存储路径
        registry.addResourceHandler("/verify/**").addResourceLocations("file:D:/verify/");
    }
}
