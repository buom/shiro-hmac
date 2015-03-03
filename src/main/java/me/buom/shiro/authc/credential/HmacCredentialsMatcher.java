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
package me.buom.shiro.authc.credential;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.buom.shiro.util.HmacSha1;

/**
 * Created by buom on 1/7/14.
 */
public class HmacCredentialsMatcher extends SimpleCredentialsMatcher {

    private static Logger log = LoggerFactory.getLogger(HmacCredentialsMatcher.class);

    public HmacCredentialsMatcher() {
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        Object tokenCredentials = getCredentials(token);
        Object accountCredentials = getCredentials(info);

        if (log.isDebugEnabled()) {
            log.debug("tokenCredentials: {}", tokenCredentials);
            log.debug("accountCredentials: {}", accountCredentials);
        }

        return equals(tokenCredentials, accountCredentials);
    }

    /**
     *
     * @param info the {@code AuthenticationInfo} stored in the data store to be compared against the submitted authentication
     *             token's credentials.
     * @return Signature = Base64( HMAC-SHA1( YourSecretAccessKeyID, UTF-8-Encoding-Of( StringToSign ) ) );
     */
    @Override
    protected Object getCredentials(AuthenticationInfo info) {
        Object signature = null;
        byte[] saltBytes = null;

        if (info instanceof SaltedAuthenticationInfo) {
            saltBytes = ((SaltedAuthenticationInfo) info).getCredentialsSalt().getBytes();
        }

        try {
            String stringToSign = (String) info.getCredentials();
            byte[] hmacSha1 = HmacSha1.hash(saltBytes, stringToSign);
            signature = Base64.encodeToString(hmacSha1);
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return signature;
    }
}
