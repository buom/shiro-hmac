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
package me.buom.shiro.realm.jdbc;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.buom.shiro.authc.HmacToken;
import me.buom.shiro.realm.HmacRealm;
import me.buom.shiro.util.HmacBuilder;
import me.buom.shiro.util.SimpleHmacBuilder;

/**
 * Created by buom on 1/10/14.
 */
public class HmacJdbcRealm extends JdbcRealm implements HmacRealm {

    protected static final String DEFAULT_SALTED_AUTHENTICATION_QUERY = "select 1, password_salt from users where username = ?";

    private static final Logger log = LoggerFactory.getLogger(HmacJdbcRealm.class);

    protected HmacBuilder hmacBuilder;

    public HmacJdbcRealm() {
        super();
        setSaltStyle(SaltStyle.COLUMN);
        setPermissionsLookupEnabled(true);
        setAuthenticationQuery(DEFAULT_SALTED_AUTHENTICATION_QUERY);
        hmacBuilder = new SimpleHmacBuilder();
    }

    @Override
    protected void onInit() {
        super.onInit();
    }

    @Override
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return super.doGetAuthorizationInfo(principals);
    }

    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return super.doGetAuthenticationInfo(token);
    }

    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
        beforeAssertCredentialsMatch(token, info);
        super.assertCredentialsMatch(token, info);
    }

    protected void beforeAssertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        SimpleAuthenticationInfo authInfo = (SimpleAuthenticationInfo) info;

        Object oldCredentials = authInfo.getCredentials();
        Object stringToSign = hmacBuilder.buildStringToSign((HmacToken) token);
        authInfo.setCredentials(stringToSign);

        if (log.isDebugEnabled()) {
            log.debug("oldCredentials: {}", oldCredentials);
            log.debug("credentials: {}", authInfo.getCredentials());
            log.debug("credentialsSalt: {}", authInfo.getCredentialsSalt().toHex());
        }
    }

    public HmacBuilder getHmacBuilder() {
        return hmacBuilder;
    }

    public void setHmacBuilder(HmacBuilder hmacBuilder) {
        this.hmacBuilder = hmacBuilder;
    }

}
