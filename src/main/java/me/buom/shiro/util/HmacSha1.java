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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

/**
 * Created by buom on 1/10/14.
 */
public abstract class HmacSha1 {

    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static byte[] hash(byte[] privateKey, String stringToSign) throws NoSuchAlgorithmException, InvalidKeyException {
        // Get an hmac_sha1 key from the raw key bytes
        SecretKeySpec signingKey = new SecretKeySpec(privateKey, HMAC_SHA1_ALGORITHM);

        // Get an hmac_sha1 Mac instance and initialize with the signing key
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);

        // Compute the hmac on input data bytes
        byte[] rawHmac = mac.doFinal(stringToSign.getBytes());

        // Convert raw bytes to Hex
        return new Hex().encode(rawHmac);
    }

    public static byte[] hash(String privateKey, String stringToSign) throws InvalidKeyException, NoSuchAlgorithmException {
        return hash(privateKey.getBytes(), stringToSign);
    }
}
