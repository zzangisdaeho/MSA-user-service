package com.example.userservice.error;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    Environment env;

    FeignErrorDecoder(Environment env){
        this.env = env;
    }

    /*
        Feign에서 에러가 발생했을때만 실행된다. 고로 200번대에서 실행되지 않는다.
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        System.out.println("response status : " + response.status());
        switch (response.status()){
            case 400:
                break;
            case 404:
                //getOrders라는 함수명을 가진 경우
               if(methodKey.contains("getOrders")){
                   System.out.println("property value : " + env.getProperty("order_service.exception_orders_is_empty"));
                   return new ResponseStatusException(HttpStatus.valueOf(response.status())
                           , env.getProperty("order_service.exception_orders_is_empty"));

               }
               break;

            default:
                return new Exception(response.reason());
        }
        return null;
    }
}
