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

import java.security.SecureRandom;

import org.apache.shiro.codec.Base64;

/**
 * Created by buom on 2/10/14.
 */
public class SimpleKeyGenerator {

    private int accessKeyLength = 20;
    private int secretKeyLength = 40;

    private String[] result;

    public SimpleKeyGenerator(int accessKeyLength, int secretKeyLength) {
        this.accessKeyLength = accessKeyLength;
        this.secretKeyLength = secretKeyLength;
    }

    public void generateKeys() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[accessKeyLength];
        random.nextBytes(bytes);
        String accessKey = Base64.encodeToString(bytes).toUpperCase().replace('+', '0').replace('/', '9');

        bytes = new byte[secretKeyLength];
        random.nextBytes(bytes);
        String secretKey = Base64.encodeToString(bytes);

        result = new String[] {accessKey, secretKey};
    }

    public String[] getKeys() {
        return this.result;
    }

    public static void main(String[] args) {
        int accessKeyLength = 20;
        int secretKeyLength = 40;

        if (args.length == 2) {
            try {
                accessKeyLength = Integer.valueOf(args[0]);
                secretKeyLength = Integer.valueOf(args[1]);
            }
            catch (NumberFormatException e) {
            }
        }

        SimpleKeyGenerator keyGenerator = new SimpleKeyGenerator(accessKeyLength, secretKeyLength);
        keyGenerator.generateKeys();
        String[] keys = keyGenerator.getKeys();

        System.out.println("AccessKey: " + keys[0]);
        System.out.println("SecretKey: " + keys[1]);
    }
}
