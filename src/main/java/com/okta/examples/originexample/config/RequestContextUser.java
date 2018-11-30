package com.okta.examples.originexample.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.examples.originexample.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

public class RequestContextUser {

    private static final Logger log = LoggerFactory.getLogger(RequestContextUser.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public  static final String USER_HEADER = "x-userinfo";

    public static User findUser() {
        String userInfoHeader;
        HttpServletRequest req;

        try {
            RequestAttributes reqAttr = RequestContextHolder.currentRequestAttributes();
            if (
                reqAttr instanceof ServletRequestAttributes &&
                (req = ((ServletRequestAttributes) reqAttr).getRequest()) != null &&
                (userInfoHeader = req.getHeader(USER_HEADER)) != null
            ) {
                log.debug("Found user info header {}", USER_HEADER);
                User user =  mapper.readValue(DatatypeConverter.parseBase64Binary(userInfoHeader), User.class);
                log.debug("Resolved user {}",user.getFullName());
                req.setAttribute(User.class.getName(), user);
                return user;
            }
        } catch (IllegalStateException|IOException e) {
            log.error("Unable to resolve user from {} header", USER_HEADER, e);
        }

        log.debug("Did not find user from {} header.", USER_HEADER);
        return null;
    }
}