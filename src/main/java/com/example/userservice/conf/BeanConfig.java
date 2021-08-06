package com.example.userservice.conf;

import com.example.userservice.error.FeignErrorDecoder;
import feign.Logger;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfig {

    @Bean
    @LoadBalanced // ip+port주소 대신 microservice application id로 서비스 호출하도록 하는 어노테이션이라는데?
    // eureka client쪽 어노테이션인걸로 보아 eureka에 직접 물어보는듯하다
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    //Feign client Logger 등록
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

//    @Bean
//    public FeignErrorDecoder getFeignErrorDecoder(){
//        return new FeignErrorDecoder();
//    }
}
