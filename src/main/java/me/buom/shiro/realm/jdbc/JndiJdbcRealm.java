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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by buom on 1/23/14.
 */
public class JndiJdbcRealm extends JdbcRealm {

    private static final Logger log = LoggerFactory.getLogger(JndiJdbcRealm.class);

    protected String jndiDataSourceName;

    public JndiJdbcRealm() {
        super();
    }

    public String getJndiDataSourceName() {
        return jndiDataSourceName;
    }

    public void setJndiDataSourceName(String jndiDataSourceName) {
        this.jndiDataSourceName = jndiDataSourceName;
        this.dataSource = getDataSourceFromJndi(jndiDataSourceName);
    }

    private DataSource getDataSourceFromJndi(String jndiDataSourceName) {
        try {
            InitialContext ic = new InitialContext();
            return (DataSource) ic.lookup(jndiDataSourceName);
        }
        catch (NamingException e) {
            log.error("JNDI error while retrieving " + jndiDataSourceName, e);
            throw new AuthorizationException(e);
        }
    }
}
