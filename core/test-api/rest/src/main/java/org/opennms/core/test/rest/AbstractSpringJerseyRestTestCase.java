/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.db.XADataSourceFactory;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.StringUtils;
import org.opennms.test.DaoTestConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public abstract class AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpringJerseyRestTestCase.class);

    public static String GET = "GET";
    public static String POST = "POST";
    public static String DELETE = "DELETE";
    public static String PUT = "PUT";

    ///String contextPath = "/opennms/rest";
    public static String contextPath = "/";

    private ServletContainer dispatcher;
    private MockServletConfig servletConfig;

    @Autowired
    protected ServletContext servletContext;

    private ContextLoaderListener contextListener;
    private Filter filter;
    private WebApplicationContext m_webAppContext;

    // Use thread locals for the authentication information so that if
    // multithreaded tests change it, they only change their copy of it.
    //
    // @see http://issues.opennms.org/browse/NMS-6898
    //
    private static ThreadLocal<String> m_username = new InheritableThreadLocal<String>();
    private static ThreadLocal<Set<String>> m_roles = new InheritableThreadLocal<Set<String>>();
    
    @Before
    public void setUp() throws Throwable {
        beforeServletStart();

        setUser("admin", new String[] { "ROLE_ADMIN" });

        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();

        MockDatabase db = new MockDatabase(true);
        DataSourceFactory.setInstance(db);
        XADataSourceFactory.setInstance(db);

        setServletConfig(new MockServletConfig(getServletContext(), "dispatcher"));    
        getServletConfig().addInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        getServletConfig().addInitParameter("com.sun.jersey.config.property.packages", "org.codehaus.jackson.jaxrs;org.opennms.web.rest;org.opennms.web.rest.config");
        getServletConfig().addInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");
        getServletConfig().addInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters", "com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");

        try {

            MockFilterConfig filterConfig = new MockFilterConfig(getServletContext(), "openSessionInViewFilter");
            setFilter(new OpenSessionInViewFilter());        
            getFilter().init(filterConfig);

            setDispatcher(new SpringServlet());
            getDispatcher().init(getServletConfig());

        } catch (ServletException se) {
            throw se.getRootCause();
        }

        setWebAppContext(WebApplicationContextUtils.getWebApplicationContext(getServletContext()));
        afterServletStart();
        System.err.println("------------------------------------------------------------------------------");
    }

    protected static void cleanUpImports() {
        final Iterator<File> fileIterator = FileUtils.iterateFiles(new File("target/test/opennms-home/etc/imports"), null, true);
        while (fileIterator.hasNext()) {
            if(!fileIterator.next().delete()) {
            	LOG.warn("Could not delete file: {}", fileIterator.next().getPath());
            }
        }
    }

    protected ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * By default, don't do anything.
     */
    protected void beforeServletStart() throws Exception {
    }

    /**
     * By default, don't do anything.
     */
    protected void afterServletStart() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        System.err.println("------------------------------------------------------------------------------");
        beforeServletDestroy();
        if (getDispatcher() != null) {
            getDispatcher().destroy();
        }
        afterServletDestroy();
    }

    /**
     * By default, don't do anything.
     */
    protected void beforeServletDestroy() throws Exception {
    }

    /**
     * By default, don't do anything.
     */
    protected void afterServletDestroy() throws Exception {
    }

    protected void dispatch(final MockHttpServletRequest request, final MockHttpServletResponse response) throws Exception {
        final FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest filterRequest, final ServletResponse filterResponse) throws IOException, ServletException {
                getDispatcher().service(filterRequest, filterResponse);
            }
        };
        if (getFilter() != null) {
            getFilter().doFilter(request, response, filterChain);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    protected static MockHttpServletResponse createResponse() {
        return new MockHttpServletResponse();
    }

    protected static MockHttpServletRequest createRequest(final ServletContext context, final String requestType, final String urlPath) {
        final Set<String> emptySet = Collections.emptySet();
        return createRequest(context, requestType, urlPath, "admin", emptySet);
    }

    protected static MockHttpServletRequest createRequest(final ServletContext context, final String requestType, final String urlPath, final String username, final Collection<String> roles) {
        final MockHttpServletRequest request = new MockHttpServletRequest(context, requestType, contextPath + urlPath) {

            @Override
            // FIXME: remove when we update to Spring 3.1
            public void setContentType(final String contentType) {
                super.setContentType(contentType);
            }

        };
        request.setContextPath(contextPath);
        request.setUserPrincipal(MockUserPrincipal.getInstance());
        MockUserPrincipal.setName(username);
        if (username != null) {
            for (final String role : roles) {
                request.addUserRole(role);
            }
        }
        return request;
    }

    protected static void setUser(final String user, final String[] roles) {
        m_username.set(user);
        m_roles.set(new HashSet<String>(Arrays.asList(roles)));
    }

    private static String getUser() {
        return m_username.get();
    }

    private static Collection<String> getUserRoles() {
        final Set<String> roles = m_roles.get();
        return roles == null? new HashSet<String>() : new HashSet<>(roles);
    }

    /**
     * @param url
     * @param xml
     * @deprecated use {@link #sendPost(String, String, int, String)} instead
     */
    protected MockHttpServletResponse sendPost(String url, String xml) throws Exception {
        return sendData(POST, MediaType.APPLICATION_XML, url, xml, /* POST/Redirect/GET */ 303);
    }

    /**
     * @param url
     * @param xml
     * @param statusCode
     * @deprecated use {@link #sendPost(String, String, int, String)} instead
     */
    protected MockHttpServletResponse sendPost(String url, String xml, int statusCode) throws Exception {
        return sendData(POST, MediaType.APPLICATION_XML, url, xml, statusCode);
    }

    /**
     * @param url
     * @param xml
     * @param statusCode
     */
    protected MockHttpServletResponse sendPost(String url, String xml, int statusCode, final String expectedUrlSuffix) throws Exception {
        LOG.debug("POST {}, expected status code = {}, expected URL suffix = {}", url, statusCode, expectedUrlSuffix);
        final MockHttpServletResponse response = sendData(POST, MediaType.APPLICATION_XML, url, xml, statusCode);
        if (expectedUrlSuffix != null) {
            final Object header = response.getHeader("Location");
            assertNotNull(header);
            final String location = URLDecoder.decode(header.toString(), "UTF-8");
            final String decodedExpectedUrlSuffix = URLDecoder.decode(expectedUrlSuffix, "UTF-8");
            assertTrue("location '" + location + "' should end with '" + decodedExpectedUrlSuffix + "'", location.endsWith(decodedExpectedUrlSuffix));
        }
        return response;
    }

    /**
     * @param url
     * @param formData
     * @deprecated use {@link #sendPut(String, String, int, String)} instead
     */
    protected MockHttpServletResponse sendPut(String url, String formData) throws Exception {
        return sendData(PUT, MediaType.APPLICATION_FORM_URLENCODED, url, formData, /* PUT/Redirect/GET */ 303);
    }

    /**
     * @param url
     * @param formData
     * @param statusCode
     * @deprecated use {@link #sendPut(String, String, int, String)} instead
     */
    protected MockHttpServletResponse sendPut(String url, String formData, int statusCode) throws Exception {
        return sendData(PUT, MediaType.APPLICATION_FORM_URLENCODED, url, formData, statusCode);
    }

    /**
     * @param url
     * @param formData
     * @param statusCode
     * @param expectedUrlSuffix
     */
    protected MockHttpServletResponse sendPut(String url, String formData, int statusCode, final String expectedUrlSuffix) throws Exception {
        LOG.debug("PUT {}, formData = {}, expected status code = {}, expected URL suffix = {}", url, formData, statusCode, expectedUrlSuffix);
        final MockHttpServletResponse response = sendData(PUT, MediaType.APPLICATION_FORM_URLENCODED, url, formData, statusCode);
        if (expectedUrlSuffix != null) {
            final String location = response.getHeader("Location").toString();
            assertTrue("location '" + location + "' should end with '" + expectedUrlSuffix + "'", location.endsWith(expectedUrlSuffix));
        }
        return response;
    }

    /**
     * @param requestType
     * @param contentType
     * @param url
     * @param data
     */
    protected MockHttpServletResponse sendData(String requestType, String contentType, String url, String data) throws Exception {
        return sendData(requestType, contentType, url, data, 200);
    }

    /**
     * @param requestType
     * @param contentType
     * @param url
     * @param data
     * @param statusCode
     */
    protected MockHttpServletResponse sendData(String requestType, String contentType, String url, String data, int statusCode) throws Exception {
        MockHttpServletRequest request = createRequest(getServletContext(), requestType, url, getUser(), getUserRoles());
        request.setContentType(contentType);

        if(contentType.equals(MediaType.APPLICATION_FORM_URLENCODED)){
            request.setParameters(parseParamData(data));
            request.setContent(new byte[] {});
        }else{
            request.setContent(data.getBytes());
        }

        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);

        LOG.debug("Received response: {}", stringifyResponse(response));
        assertEquals(response.getErrorMessage(), statusCode, response.getStatus());

        return response;
    }

    protected String stringifyResponse(final MockHttpServletResponse response) {
        final StringBuilder string = new StringBuilder();
        try {
            string.append("HttpServletResponse[")
            .append("status=").append(response.getStatus())
            .append(",content=").append(response.getContentAsString())
            .append(",headers=[");
            boolean first = true;
            for (final Iterator<String> i = response.getHeaderNames().iterator(); i.hasNext(); first = false) {
                if (!first) {
                    string.append(",");
                }
                final String name = i.next();
                string.append("name=").append(response.getHeader(name));
            }
            string.append("]").append("]");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Unable to get response content", e);
        }
        return string.toString();
    }

    protected static Map<String, String> parseParamData(String data) throws UnsupportedEncodingException {
        Map<String, String> retVal = new HashMap<String, String>();
        for (String item : data.split("&")) {
            int idx = item.indexOf("=");
            if (idx > 0) {
                retVal.put(URLDecoder.decode(item.substring(0, idx), "UTF-8"), URLDecoder.decode(item.substring(idx + 1), "UTF-8"));
            }
        }
        return retVal;
    }

    protected String sendRequest(String requestType, String url, Map<?,?> parameters, int expectedStatus) throws Exception {
        return sendRequest(requestType, url, parameters, expectedStatus, null);
    }

    protected String sendRequest(final String requestType, final String url, final Map<?,?> parameters, final int expectedStatus, final String expectedUrlSuffix) throws Exception {
        final MockHttpServletRequest request = createRequest(getServletContext(), requestType, url, getUser(), getUserRoles());
        request.setParameters(parameters);
        request.setQueryString(getQueryString(parameters));
        return sendRequest(request, expectedStatus, expectedUrlSuffix);
    }

    protected static String getQueryString(final Map<?,?> parameters) {
        final StringBuffer sb = new StringBuffer();

        try {
            for (final Entry<?,?> entry : parameters.entrySet()) {
                final Object key = entry.getKey();
                if (key instanceof String) {
                    final Object value = entry.getValue();
                    String[] valueEntries = null;
                    if (value instanceof String[]) {
                        valueEntries = (String[])value;
                    } else if (value instanceof String) {
                        valueEntries = new String[] { (String)value };
                    } else {
                        LOG.warn("value was not a string or string array! ({})", value);
                        continue;
                    }

                    for (final String valueEntry : valueEntries) {
                        sb.append(URLEncoder.encode((String)key, "UTF-8")).append("=").append(URLEncoder.encode((String)valueEntry, "UTF-8")).append("&");
                    }
                } else {
                    LOG.warn("key was not a string! ({})", key);
                }
            }
        } catch (final UnsupportedEncodingException e) {
            LOG.warn("unsupported encoding UTF-8?!?  WTF??!", e);
        }

        return sb.toString();
    }

    protected String sendRequest(String requestType, String url, int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(getServletContext(), requestType, url, getUser(), getUserRoles());
        return sendRequest(request, expectedStatus);
    }

    protected String sendRequest(final MockHttpServletRequest request, int expectedStatus) throws Exception, UnsupportedEncodingException {
        return sendRequest(request, expectedStatus, null);
    }

    protected String sendRequest(MockHttpServletRequest request, int expectedStatus, final String expectedUrlSuffix) throws Exception, UnsupportedEncodingException {
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        final String xml = response.getContentAsString();
        if (xml != null && !xml.isEmpty()) {
            try {
                System.err.println(StringUtils.prettyXml(xml));
            } catch (Exception e) {
                System.err.println(xml);
            }
        }
        assertEquals(expectedStatus, response.getStatus());
        if (expectedUrlSuffix != null) {
            final String location = response.getHeader("Location").toString();
            assertTrue("location '" + location + "' should end with '" + expectedUrlSuffix + "'", location.endsWith(expectedUrlSuffix));
        }
        Thread.sleep(50);
        return xml;
    }

    protected <T> T getXmlObject(JAXBContext context, String url, int expectedStatus, Class<T> expectedClass) throws Exception {
        MockHttpServletRequest request = createRequest(getServletContext(), GET, url, getUser(), getUserRoles());
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(expectedStatus, response.getStatus());

        System.err.printf("xml: %s%n", response.getContentAsString());

        InputStream in = new ByteArrayInputStream(response.getContentAsByteArray());

        Unmarshaller unmarshaller = context.createUnmarshaller();

        T result = expectedClass.cast(unmarshaller.unmarshal(in));

        return result;

    }

    protected void putXmlObject(final JAXBContext context, final String url, final int expectedStatus, final Object object, final String expectedUrlSuffix) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        final Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(object, out);
        final byte[] content = out.toByteArray();

        final MockHttpServletRequest request = createRequest(getServletContext(), PUT, url, getUser(), getUserRoles());
        request.setContentType(MediaType.APPLICATION_XML);
        request.setContent(content);
        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(expectedStatus, response.getStatus());

        final String location = response.getHeader("Location").toString();
        assertTrue("location '" + location + "' should end with '" + expectedUrlSuffix + "'", location.endsWith(expectedUrlSuffix));
    }

    protected void createNode() throws Exception {
        String node = "<node type=\"A\" label=\"TestMachine\">" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("/nodes", node, 303, "/nodes/1");
    }

    protected void createIpInterface() throws Exception {
        createNode();
        String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<hostName>TestMachine</hostName>" +
                "<ipStatus>1</ipStatus>" +
                "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface, 303, "/nodes/1/ipinterfaces/10.10.10.10");
    }

    protected void createSnmpInterface() throws Exception {
        createIpInterface();
        String snmpInterface = "<snmpInterface ifIndex=\"6\">" +
                "<ifAdminStatus>1</ifAdminStatus>" +
                "<ifDescr>en1</ifDescr>" +
                "<ifName>en1</ifName>" +
                "<ifOperStatus>1</ifOperStatus>" +
                "<ifSpeed>10000000</ifSpeed>" +
                "<ifType>6</ifType>" +
                "<netMask>255.255.255.0</netMask>" +
                "<physAddr>001e5271136d</physAddr>" +
                "</snmpInterface>";
        sendPost("/nodes/1/snmpinterfaces", snmpInterface, 303, "/nodes/1/snmpinterfaces/6");
    }

    protected void createService() throws Exception {
        createIpInterface();
        String service = "<service source=\"P\" status=\"N\">" +
                "<notify>Y</notify>" +
                "<serviceType>" +
                "<name>ICMP</name>" +
                "</serviceType>" +
                "</service>";
        sendPost("/nodes/1/ipinterfaces/10.10.10.10/services", service, 303, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP");
    }

    protected void createCategory() throws Exception {
        createNode();
        String service = "<category name=\"Routers\">" +
                "<description>Core Routers</description>" +
                "</category>";
        sendPost("/categories", service, 303, "/categories/Routers");
    }

    public void setWebAppContext(WebApplicationContext webAppContext) {
        m_webAppContext = webAppContext;
    }

    public WebApplicationContext getWebAppContext() {
        return m_webAppContext;
    }

    public <T> T getBean(String name, Class<T> beanClass) {
        return m_webAppContext.getBean(name, beanClass);
    }

    public void setContextListener(ContextLoaderListener contextListener) {
        this.contextListener = contextListener;
    }

    public ContextLoaderListener getContextListener() {
        return contextListener;
    }

    public void setServletConfig(MockServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    public MockServletConfig getServletConfig() {
        return servletConfig;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setDispatcher(ServletContainer dispatcher) {
        this.dispatcher = dispatcher;
    }

    public ServletContainer getDispatcher() {
        return dispatcher;
    }
}
