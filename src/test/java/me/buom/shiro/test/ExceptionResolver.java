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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by buom on 1/16/14.
 */
@Provider
public class ExceptionResolver implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(ExceptionResolver.class);

    @Override
    public Response toResponse(final Exception exception) {
        Integer statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase();

        if (exception instanceof ShiroException) {
            if (exception instanceof UnauthenticatedException) {
                statusCode = Response.Status.UNAUTHORIZED.getStatusCode();
                message = Response.Status.UNAUTHORIZED.getReasonPhrase();
            }
            else {
                statusCode = Response.Status.FORBIDDEN.getStatusCode();
                message = Response.Status.FORBIDDEN.getReasonPhrase();
            }
        }
        else if (exception instanceof WebApplicationException) {
            Response.StatusType statusType = ((WebApplicationException) exception).getResponse().getStatusInfo();
            statusCode = statusType.getStatusCode();
            message = statusType.getReasonPhrase();
        }

        if (log.isErrorEnabled()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exception.printStackTrace(new PrintStream(baos, true));
            log.error(baos.toString());
        }

        return Response.status(statusCode).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

}
