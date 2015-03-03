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
package me.buom.shiro.realm.text;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.text.PropertiesRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.buom.shiro.authc.HmacToken;
import me.buom.shiro.realm.HmacRealm;
import me.buom.shiro.util.HmacBuilder;
import me.buom.shiro.util.SimpleHmacBuilder;

/**
 * Created by buom on 1/13/14.
 */
public class HmacPropertiesRealm extends PropertiesRealm implements HmacRealm {

    private static final Logger log = LoggerFactory.getLogger(HmacPropertiesRealm.class);

    protected HmacBuilder hmacBuilder;

    public HmacPropertiesRealm() {
        hmacBuilder = new SimpleHmacBuilder();
    }

    @Override
    public void onInit() {
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
        SimpleAccount account = (SimpleAccount) info;
        if (account != null) {
            // only set credentials salt on the first time!!!
            ByteSource credentialsSalt = (ByteSource) account.getCredentialsSalt();
            if (credentialsSalt == null) {
                Object credentials = account.getCredentials();
                credentialsSalt = ByteSource.Util.bytes(credentials);
                account.setCredentialsSalt(credentialsSalt);
                account.setCredentials(null);
            }

            Object oldCredentials = account.getCredentials();
            Object stringToSign = hmacBuilder.buildStringToSign((HmacToken) token);
            account.setCredentials(stringToSign);

            if (log.isDebugEnabled()) {
                log.debug("oldCredentials: {}", oldCredentials);
                log.debug("curCredentials: {}", account.getCredentials());
                log.debug("credentialsSalt: {}", account.getCredentialsSalt().toHex());
            }
        }
    }

    public HmacBuilder getHmacBuilder() {
        return hmacBuilder;
    }

    public void setHmacBuilder(HmacBuilder hmacBuilder) {
        this.hmacBuilder = hmacBuilder;
    }

}
