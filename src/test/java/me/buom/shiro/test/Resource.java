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
package me.buom.shiro.test;

import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by buom on 1/16/14.
 */
@Path("/")
@Produces("text/plain")
public class Resource {

    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    @PostConstruct
    public void init() {
        log.info("init");
    }

    @POST
    @Path("/admin")
    @RequiresPermissions({"op:create", "op:edit", "op:delete", "op:update"})
    public String admin(@Context HttpServletRequest httpRequest, String data) {
        log.debug("data: {}", data);

        for (Enumeration<String> attributeNames = httpRequest.getAttributeNames(); attributeNames.hasMoreElements();) {
            String attrName = attributeNames.nextElement();
            log.debug("{}: {}", attrName, httpRequest.getAttribute(attrName));
        }
        return "admin";
    }

    @GET
    @Path("/user_create")
    @RequiresPermissions({"op:create"})
    public String user_create() {
        return "user_create";
    }

    @GET
    @Path("/user_read")
    @RequiresPermissions({"op:read"})
    public String user_read() {
        return "user_read";
    }

    @GET
    @Path("/superviser_read")
    @RequiresPermissions({"op:read"})
    public String superviser_read() {
        return "superviser_read";
    }

    @GET
    @Path("/superviser_create")
    @RequiresPermissions({"op:create"})
    public String superviser_create() {
        return "superviser_create";
    }

    @GET
    @Path("/superviser_edit")
    @RequiresPermissions({"op:edit"})
    public String superviser_edit() {
        return "superviser_edit";
    }

    @GET
    @Path("/superviser_delete")
    @RequiresPermissions({"op:delete"})
    public String superviser_delete() {
        return "superviser_delete";
    }


    @Context
    HttpServletRequest httpRequest;
}
