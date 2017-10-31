package com.okta.examples.originexample;

import com.okta.examples.originexample.config.RequestContextUser;
import com.okta.examples.originexample.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class OriginExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(OriginExampleApplication.class, args);
	}

    @Bean
    @Scope("request")
    public User user() {
        return RequestContextUser.findUser();
    }
}
