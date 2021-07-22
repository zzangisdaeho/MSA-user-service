package com.example.userservice.conf;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ModelMapperBean {

    @Bean
    public ModelMapper mapperStrict(){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        log.info("strict mapper 등록됨!!!");
        return mapper;
    }

    @Bean
    public ModelMapper mapperStandard(){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
        log.info("standard mapper 등록됨!!!");
        return mapper;
    }

    @Bean
    public ModelMapper mapperLoose(){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        log.info("loose mapper 등록됨!!!");
        return mapper;
    }
}
