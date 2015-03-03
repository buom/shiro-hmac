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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;
import liquibase.integration.servlet.LiquibaseServletListener;
import me.buom.junit.Repeat;
import me.buom.junit.RepeatRule;
import me.buom.shiro.util.HmacSha1;

/**
 * Created by buom on 1/16/14.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class AppTest {

    protected static final String TEST_NAME = AppTest.class.getSimpleName();
    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    protected HttpClient client = new DefaultHttpClient();

    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createDeployment() {
        PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile("pom.xml");
        File[] libs = pom.resolve(
            "ch.qos.logback:logback-classic").withTransitivity().asFile();

        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
            .version(WebAppVersionType._3_0)
            .createContextParam()
                .paramName("liquibase.changelog")
                .paramValue("src/test/resources/db.changelog.xml").up()
            .createContextParam()
                .paramName("liquibase.datasource")
                .paramValue("shiro").up()
            .createContextParam()
                .paramName("liquibase.onerror.fail")
                .paramValue("true").up()
            .createListener()
                .listenerClass(ContextListener.class.getName()).up()
            .createListener()
                .listenerClass(LiquibaseServletListener.class.getName()).up()
            .createListener()
                .listenerClass(EnvironmentLoaderListener.class.getName()).up()
            .createContextParam()
                .paramName("javax.ws.rs.Application")
                .paramValue(Activator.class.getName()).up()
            .createFilter()
                .filterName("ShiroFilter")
                .filterClass(ShiroFilter.class.getName()).up()
            .createFilterMapping()
                .filterName("ShiroFilter")
                .urlPattern("/*").up()
            .createFilter()
                .filterName("Resteasy")
                .filterClass(FilterDispatcher.class.getName()).up()
            .createFilterMapping()
                .filterName("Resteasy")
                .urlPattern("/*").up()
            .createResourceEnvRef()
                .resourceEnvRefName("BeanManager")
                .resourceEnvRefType(BeanManager.class.getName()).up()
            .createErrorPage()
                .exceptionType("java.lang.Exception")
                .location("/error.jsp").up()
            .createErrorPage()
                .errorCode(400)
                .location("/error.jsp").up()
            .createErrorPage()
                .errorCode(401)
                .location("/error.jsp").up()
            .createErrorPage()
                .errorCode(403)
                .location("/error.jsp").up()
            .createErrorPage()
                .errorCode(404)
                .location("/error.jsp").up()
            .createErrorPage()
                .errorCode(500)
                .location("/error.jsp").up();

        WebArchive war = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
            .setWebXML(new StringAsset(descriptor.exportAsString()))
            .addAsLibraries(libs)
            .addPackages(true, "me.buom.shiro")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebInfResource(new File("src/test/resources/jetty-env.xml"), "jetty-env.xml")
            .addAsManifestResource(new File("src/test/resources/META-INF/aop.xml"), "aop.xml")
            .addAsWebResource(new File("src/test/resources/error.jsp"), "error.jsp");

        //System.out.println(descriptor.exportAsString());
        //System.out.println(war.toString(true));
        return war;
    }

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("\n>> " + description.getMethodName() + " being run...");
        }

        @Override
        protected void finished(Description description) {
            System.out.println("\n");
        }
    };


    private ThreadLocal<SimpleDateFormat> dateFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        }
    };


    @Test
    @Repeat(times = 3, warmUp = 1)
    public void test_admin() throws Exception {

        URL url = new URL(baseUrl.toExternalForm() + "admin");
        HttpPost method = new HttpPost(url.toURI());

        String contentType = "application/json; charset=utf-8";
        String entity = "lady gaga";
        Header[] headers = buildHeader(ApiKey.ADMIN, method, contentType, entity);

        method.setHeaders(headers);
        method.setEntity(new StringEntity(entity, ContentType.create(contentType)));

        HttpResponse execute = client.execute(method);
        StatusLine statusLine = execute.getStatusLine();

        printResponse(execute);
        printHeader(execute);

        Assert.assertFalse(statusLine.getStatusCode() != 200);
    }

    @Test
    @Repeat(times = 3, warmUp = 1)
    public void test_user_create() throws Exception {

        URL url = new URL(baseUrl.toExternalForm() + "user_create");
        HttpGet method = new HttpGet(url.toURI());

        String contentType = "application/json; charset=utf-8";
        Header[] headers = buildHeader(ApiKey.USER, method, contentType, null);

        method.setHeaders(headers);

        HttpResponse execute = client.execute(method);
        StatusLine statusLine = execute.getStatusLine();

        printResponse(execute);
        printHeader(execute);

        Assert.assertFalse(statusLine.getStatusCode() != 401);
    }

    @Test
    @Repeat(times = 3, warmUp = 1)
    public void test_user_read() throws Exception {

        URL url = new URL(baseUrl.toExternalForm() + "user_read");
        HttpGet method = new HttpGet(url.toURI());

        String contentType = "application/json; charset=utf-8";
        Header[] headers = buildHeader(ApiKey.USER, method, contentType, null);

        method.setHeaders(headers);

        HttpResponse execute = client.execute(method);
        StatusLine statusLine = execute.getStatusLine();

        printResponse(execute);
        printHeader(execute);

        Assert.assertFalse(statusLine.getStatusCode() != 200);
    }

    @Test
    @Repeat(times = 3, warmUp = 1)
    public void test_superviser_read() throws Exception {

        URL url = new URL(baseUrl.toExternalForm() + "superviser_read");
        HttpGet method = new HttpGet(url.toURI());

        String contentType = "application/json; charset=utf-8";
        Header[] headers = buildHeader(ApiKey.SUPERVISER, method, contentType, null);

        method.setHeaders(headers);

        HttpResponse execute = client.execute(method);
        StatusLine statusLine = execute.getStatusLine();

        printResponse(execute);
        printHeader(execute);

        Assert.assertFalse(statusLine.getStatusCode() != 200);
    }

    @Test
    @Repeat(times = 3, warmUp = 1)
    public void test_superviser_create() throws Exception {

        URL url = new URL(baseUrl.toExternalForm() + "superviser_create");
        HttpGet method = new HttpGet(url.toURI());

        String contentType = "application/json; charset=utf-8";
        Header[] headers = buildHeader(ApiKey.SUPERVISER, method, contentType, null);

        method.setHeaders(headers);

        HttpResponse execute = client.execute(method);
        StatusLine statusLine = execute.getStatusLine();

        printResponse(execute);
        printHeader(execute);

        Assert.assertFalse(statusLine.getStatusCode() != 200);
    }

    @Test
    @Repeat(times = 3, warmUp = 1)
    public void test_superviser_edit() throws Exception {

        URL url = new URL(baseUrl.toExternalForm() + "superviser_edit");
        HttpGet method = new HttpGet(url.toURI());

        String contentType = "application/json; charset=utf-8";
        Header[] headers = buildHeader(ApiKey.SUPERVISER, method, contentType, null);

        method.setHeaders(headers);

        HttpResponse execute = client.execute(method);
        StatusLine statusLine = execute.getStatusLine();

        printResponse(execute);
        printHeader(execute);

        Assert.assertFalse(statusLine.getStatusCode() != 200);
    }

    @Test
    @Repeat(times = 3, warmUp = 1)
    public void test_superviser_delete() throws Exception {

        URL url = new URL(baseUrl.toExternalForm() + "superviser_delete");
        HttpGet method = new HttpGet(url.toURI());

        String contentType = "application/json; charset=utf-8";
        Header[] headers = buildHeader(ApiKey.SUPERVISER, method, contentType, null);

        method.setHeaders(headers);

        HttpResponse execute = client.execute(method);
        StatusLine statusLine = execute.getStatusLine();

        printResponse(execute);
        printHeader(execute);

        Assert.assertFalse(statusLine.getStatusCode() != 401);
    }


    ///////////////////////////////////////////////////////////////////////////

    private void printHeader(HttpResponse execute) {
        for (Header header : execute.getAllHeaders()) {
            log.debug("{} = {}", header.getName(), header.getValue());
        }
    }

    private void printResponse(HttpResponse execute) throws Exception {
        StatusLine statusLine = execute.getStatusLine();

        log.info("Reason: {}", statusLine.getReasonPhrase());
        log.info("Status: {}", statusLine.getStatusCode());
        log.info("Read: {}", getEntity(execute.getEntity().getContent()));
    }

    protected String getEntity(InputStream stream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private Header[] buildHeader(ApiKey apiKey, HttpRequestBase httpRequest, String contentType, String entity) throws Exception {

        String accessKey = apiKey.getAccessKey();
        String secretKey = apiKey.getSecretKey();

        String contentMd5 = entity != null ? DigestUtils.md5Hex(entity) : "";
        String dateString = dateFormatter.get().format(new Date());

        String stringToSign = String.format(Locale.US, "%s\n%s\n%s\n%s\n%s",
                httpRequest.getMethod(),
                contentMd5,
                contentType,
                dateString,
                httpRequest.getURI().getPath());

        byte[] hexHmac = HmacSha1.hash(secretKey, stringToSign);
        String base64Hmac = Base64.encodeToString(hexHmac);

        Header[] headers = new Header[] {
                new BasicHeader("Content-Type", contentType),
                new BasicHeader("Content-MD5", contentMd5),
                new BasicHeader("Date", dateString),
                new BasicHeader("Authorization", String.format(Locale.US, "AWS %s:%s", accessKey, base64Hmac)),
        };

        return headers;
    }

    private enum ApiKey {
        ADMIN("J4KUF0NALQ20QUQHBNXF", "J4XpaMB/1CtTXH4KSbk2QpcIAiFtTSBcErEwg5gQ"),
        SUPERVISER("I55CLFCUIOE99NYNR1YE", "Fui3BLpDQME4jAImmLtCIB9r+3RAyNHvrC2zZglS"),
        USER("XLKURDI8HZZSCYV99USU", "XVEZp1/iASYwbhszCVZ7GOa3fXyzW45XdwnD6OMv");

        ApiKey(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }
        public String getAccessKey() {
            return accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        private String accessKey;
        private String secretKey;
    };
}
