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
package me.buom.shiro.authc;

import javax.servlet.ServletRequest;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Created by buom on 1/7/14.
 */
public class HmacToken extends UsernamePasswordToken {

    private ServletRequest request;
    private String accessKeyId;
    private String signature;

    public HmacToken(String accessKeyId, String signature, ServletRequest request) {
        super();
        setUsername(accessKeyId);
        setRememberMe(false);

        this.request = request;
        this.accessKeyId = accessKeyId;
        this.signature = signature;
    }

    @Override
    public Object getCredentials() {
        return getSignature();
    }

    public ServletRequest getRequest() {
        return this.request;
    }

    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean isRememberMe() {
        return false;
    }
}
