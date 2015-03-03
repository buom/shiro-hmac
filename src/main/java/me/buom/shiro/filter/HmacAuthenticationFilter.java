/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package me.buom.shiro.filter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.buom.shiro.authc.HmacToken;

/**
 * Created by buom on 1/7/14.
 */
public class HmacAuthenticationFilter extends AuthenticatingFilter {

    protected static final String AUTHORIZATION_HEADER = "Authorization";

    private transient static final Logger log = LoggerFactory.getLogger(HmacAuthenticationFilter.class);

    private String authzScheme = "AWS";
    private long expirationThreshold = 300000L;

    public long getExpirationThreshold() {
        return expirationThreshold;
    }

    public void setExpirationThreshold(long expirationThreshold) {
        this.expirationThreshold = expirationThreshold;
    }

    public String getAuthzScheme() {
        return authzScheme;
    }

    public void setAuthzScheme(String authzScheme) {
        this.authzScheme = authzScheme;
    }

    public HmacAuthenticationFilter() {
    }

    protected long getClientTime(ServletRequest request) {
        long clientTime = 0L;
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String dateHeader = httpRequest.getHeader("Date");
        if (dateHeader != null) {
            try {
                Date date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse(dateHeader);
                clientTime = date.getTime();
            }
            catch (ParseException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
        return clientTime;
    }

    protected long getServerTime() {
        return System.currentTimeMillis();
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loggedIn = false;
        if (isLoginAttempt(request, response) && isValidTime(request, response)) {
            loggedIn = executeLogin(request, response);
        }
        if (!loggedIn) {
            sendChallenge(request, response);
        }

        return loggedIn;
    }

    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        String authzHeader = getAuthzHeader(request);
        return authzHeader != null && isLoginAttempt(authzHeader);
    }

    protected boolean isLoginAttempt(String authzHeader) {
        String authzScheme = getAuthzScheme().toLowerCase(Locale.ENGLISH);
        return authzHeader.toLowerCase(Locale.ENGLISH).startsWith(authzScheme);
    }

    protected boolean isValidTime(ServletRequest request, ServletResponse response) {
        long clientTime = getClientTime(request);
        long serverTime = getServerTime();
        return (serverTime - clientTime) <= getExpirationThreshold();
    }

    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Authentication required: sending 401 Authentication challenge response.");
        }
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    protected String getAuthzHeader(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return httpRequest.getHeader(AUTHORIZATION_HEADER);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        String authorizationHeader = getAuthzHeader(request);
        if (authorizationHeader == null || authorizationHeader.length() == 0) {
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Attempting to execute login with headers [" + authorizationHeader + "]");
        }

        String[] prinCred = getPrincipalsAndCredentials(authorizationHeader, request);
        if (prinCred == null || prinCred.length < 2) {
            return null;
        }

        String accessKeyId = prinCred[0];
        String signature = prinCred[1];

        return createToken(accessKeyId, signature, request, response);
    }

    @Override
    protected AuthenticationToken createToken(String accessKeyId, String signature, ServletRequest request, ServletResponse response) {
        return new HmacToken(accessKeyId, signature, request);
    }

    protected String[] getPrincipalsAndCredentials(String authorizationHeader, ServletRequest request) {
        if (authorizationHeader == null) {
            return null;
        }
        String[] authTokens = authorizationHeader.split(" ");
        if (authTokens == null || authTokens.length < 2) {
            return null;
        }
        return getPrincipalsAndCredentials(authTokens[0], authTokens[1]);
    }

    protected String[] getPrincipalsAndCredentials(String scheme, String encoded) {
        return encoded.split(":", 2);
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (StringUtils.hasText(httpRequest.getHeader("Content-MD5"))) {
            HmacRequestWrapper requestWrapper = new HmacRequestWrapper(httpRequest);
            super.doFilterInternal(requestWrapper, response, chain);
        }
        else {
            super.doFilterInternal(request, response, chain);
        }
    }

}
