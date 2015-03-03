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
package me.buom.shiro.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.util.WebUtils;

import me.buom.shiro.authc.HmacToken;

/**
 * Created by buom on 1/10/14.
 */
public class SimpleHmacBuilder implements HmacBuilder {

    private HttpServletRequest httpRequest;

    public String buildStringToSign(HmacToken token) {
        httpRequest = WebUtils.toHttp(token.getRequest());
        String stringToSign = String.format(Locale.US, "%s\n%s\n%s\n%s\n%s",
                httpRequest.getMethod(),
                StringUtils.hasText(getHeader("Content-MD5")) ? DigestUtils.md5Hex(toByteArray(httpRequest)) : "",
                //getHeader("Content-MD5"),
                //getHeader("Content-Type"),
                httpRequest.getContentType(),
                getHeader("Date"),
                httpRequest.getRequestURI());

        return stringToSign;
    }

    public String getHeader(String name) {
        String value = httpRequest.getHeader(name);
        return value != null ? value : "";
    }

    protected byte[] toByteArray(HttpServletRequest request) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                byte[] buff = new byte[2048];
                int count = -1;

                while ((count = inputStream.read(buff)) > 0) {
                    byteArrayOutputStream.write(buff, 0, count);
                }
                inputStream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }

}
