package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate;

    private final Environment env;

    private final OrderServiceClient orderServiceClient;

    private final CircuitBreakerFactory circuitBreakerFactory;



    @Autowired
    @Qualifier("mapperStrict")
    private ModelMapper mapper;

    public UserServiceImpl(UserRepository userRepository, @Lazy BCryptPasswordEncoder passwordEncoder,
                           RestTemplate restTemplate, Environment env, OrderServiceClient orderServiceClient,
                           CircuitBreakerFactory circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.env = env;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

//        ModelMapper mapper = new ModelMapper();
//        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserEntity userEntity = mapper.map(userDto, UserEntity.class);

        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        UserEntity save = userRepository.save(userEntity);

        return mapper.map(save, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        Optional<UserEntity> findUser = userRepository.findByUserId(userId);
        findUser.orElseThrow(() -> new UsernameNotFoundException("없는 유저입니다."));
        UserDto userDto = mapper.map(findUser.get(), UserDto.class);

        //Using as rest template
//        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
//        ResponseEntity<List<ResponseOrder>> orderListResponse =
//                restTemplate.exchange(orderUrl, HttpMethod.GET, null
//                , new ParameterizedTypeReference<List<ResponseOrder>>() {
//                });
//
//        List<ResponseOrder> orderList = orderListResponse.getBody();

        //Using a feign client
        /* Feign exception handling*/
//        List<ResponseOrder> orderList = null;
//        try {
//
//            orderList = orderServiceClient.getOrders(userId);
//        }catch (FeignException ex){
//            log.error(ex.getMessage());
//        }

        /* Feign errorDecoder*/
//        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
        log.info("Before call orders microservice");
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orderList = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>());
        log.info("After called order microservice");

        userDto.setOrders(orderList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        Optional<UserEntity> findUser = userRepository.findByEmail(email);

        return mapper.map(findUser.orElseThrow(() -> new UsernameNotFoundException("없는 유저입니다")), UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> findUser = userRepository.findByEmail(username);

        findUser.orElseThrow(() -> new UsernameNotFoundException("해당하는 유저 정보가 없습니다"));

        System.out.println("==============2. loadUserByUsername==============");

        return new User(findUser.get().getEmail(), findUser.get().getEncryptedPwd()
                , true, true, true, true
                , new ArrayList<>());
    }
}
