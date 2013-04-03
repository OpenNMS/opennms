/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.netmgt.poller.monitors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.http.client.params.CookiePolicy;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.EmptyKeyRelaxedTrustSSLContext;
import org.opennms.core.utils.HttpResponseRange;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.MatchTable;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.pagesequence.Page;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.config.pagesequence.Parameter;
import org.opennms.netmgt.config.pagesequence.SessionVariable;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class is designed to be used by the service poller framework to test the availability of the HTTP service on
 * remote interfaces. The class implements the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <a mailto:brozow@opennms.org>Mathew Brozowski</a>
 * @version $Id: $
 */
@Distributable
public class PageSequenceMonitorEnhanced extends AbstractServiceMonitor {

    private static NodeDao s_nodeDao;

    
    private static class MyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("start: " + method.getName());
            Object returnValue = method.invoke(proxy, args);
            System.out.println("end: " + method.getName());
            return returnValue;
        }
    }

    protected class SequenceTracker {

        TimeoutTracker m_tracker;

        public SequenceTracker(Map<String, Object> parameterMap, int defaultSequenceRetry, int defaultTimeout) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("retry", ParameterMap.getKeyedInteger(parameterMap, "sequence-retry", defaultSequenceRetry));
            parameters.put("timeout", ParameterMap.getKeyedInteger(parameterMap, "timeout", defaultTimeout));
            parameters.put("strict-timeout", ParameterMap.getKeyedBoolean(parameterMap, "strict-timeout", false));
            m_tracker = new TimeoutTracker(parameters, defaultSequenceRetry, defaultTimeout);
        }

        public void reset() {
            m_tracker.reset();
        }

        public boolean shouldRetry() {
            return m_tracker.shouldRetry();
        }

        public void nextAttempt() {
            m_tracker.nextAttempt();
        }

        public void startAttempt() {
            m_tracker.startAttempt();
        }

        public double elapsedTimeInMillis() {
            return m_tracker.elapsedTimeInMillis();
        }
    }
    private static final int DEFAULT_SEQUENCE_RETRY = 0;

    //FIXME: This should be wired with Spring
    // Make sure that the {@link EmptyKeyRelaxedTrustSSLContext} algorithm
    // is available to JSSE
    static {
        java.security.Security.addProvider(new EmptyKeyRelaxedTrustProvider());
    }

    public static class PageSequenceMonitorException extends RuntimeException {

        private static final long serialVersionUID = 1346757238604080088L;

        public PageSequenceMonitorException(String message) {
            super(message);
        }

        public PageSequenceMonitorException(Throwable cause) {
            super(cause);
        }

        public PageSequenceMonitorException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RETRY = 0;

    public static class HttpPageSequence {

        final PageSequence m_sequence;
        final List<HttpPage> m_pages;
        Properties m_sequenceProperties;
        Map<String, String> m_parameters = new HashMap<String, String>();

        HttpPageSequence(MonitoredService svc, PageSequence sequence) {
            m_sequence = sequence;

            m_pages = new ArrayList<HttpPage>(m_sequence.getPageCount());
            for (Page page : m_sequence.getPage()) {
                substitute(svc, page);
//                PropertiesUtils.substitute(new , symbolsArray)
//                 HttpPage newHttpPage = (HttpPage) Proxy.newProxyInstance(HttpPage.class.getClassLoader(),
//                                          new Class[] { HttpPage.class },
//                                          new MyInvocationHandler());
//                newHttpPage.set
                HttpPage newHttpPage = new HttpPage(this, page);
//                substitute(newHttpPage);
                m_pages.add(newHttpPage);
            }

            m_sequenceProperties = new Properties();
        }

        public Map<String, String> getParameters() {
            return m_parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            m_parameters = parameters;
        }

        List<HttpPage> getPages() {
            return m_pages;
        }

        private void execute(DefaultHttpClient client, MonitoredService svc, Map<String, Number> responseTimes) {
            // Clear the sequence properties before each run
            clearSequenceProperties();

            // Initialize the response time on each page that saves it
            for (HttpPage page : getPages()) {
                if (page.getDsName() != null) {
                    responseTimes.put(page.getDsName(), Double.NaN);
                }
            }

            for (HttpPage page : getPages()) {
                if (log().isDebugEnabled()) {
                    log().debug("Executing HttpPage: " + page.toString());
                }
                page.execute(client, svc);
                if (page.getDsName() != null) {
                    if (log().isDebugEnabled()) {
                        log().debug("Recording response time " + page.getResponseTime() + " for ds " + page.getDsName());
                    }
                    responseTimes.put(page.getDsName(), page.getResponseTime());
                }
            }
        }

        protected Properties getSequenceProperties() {
            return m_sequenceProperties;
        }

        protected void setSequenceProperties(Properties newProps) {
            m_sequenceProperties = newProps;
        }

        protected void clearSequenceProperties() {
            m_sequenceProperties.clear();
        }

        private ThreadCategory log() {
            return ThreadCategory.getInstance(getClass());
        }
    }

    public interface PageSequenceHttpUriRequest extends HttpUriRequest {

        public void setQueryParameters(List<NameValuePair> parms);
    }

    public static class PageSequenceHttpPost extends HttpPost implements PageSequenceHttpUriRequest {

        public PageSequenceHttpPost(URI uri) {
            super(uri);
        }

        @Override
        public void setQueryParameters(List<NameValuePair> parms) {
            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parms, "UTF-8");
                this.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                // Should never happen
            }
        }
    }

    public static class PageSequenceHttpGet extends HttpGet implements PageSequenceHttpUriRequest {

        public PageSequenceHttpGet(URI uri) {
            super(uri);
        }

        @Override
        public void setQueryParameters(List<NameValuePair> parms) {
            URI uri = this.getURI();
            URI uriWithQueryString = null;
            try {
                String query = URLEncodedUtils.format(parms, "UTF-8");
                uriWithQueryString = URIUtils.createURI(
                        uri.getScheme(),
                        uri.getHost(),
                        uri.getPort(),
                        uri.getPath(),
                        // Do we need to merge any existing query params?
                        // Probably not... shouldn't be any.
                        query,
                        uri.getFragment());
                this.setURI(uriWithQueryString);
            } catch (URISyntaxException e) {
                ThreadCategory.getInstance("Cannot add query parameters to URI: " + this.getClass()).warn(e.getMessage(), e);
            }
        }
    }
    
    public static class HttpPage {

        private final Page m_page;
        private final HttpResponseRange m_range;
        private final Pattern m_successPattern;
        private final Pattern m_failurePattern;
        private final Pattern m_locationPattern;
        private final HttpPageSequence m_parentSequence;
        private double m_responseTime;
        private final List<NameValuePair> m_parms = new ArrayList<NameValuePair>();

        HttpPage(HttpPageSequence parent, Page page) {
            m_page = page;
            m_range = new HttpResponseRange(page.getResponseRange());
            m_successPattern = (page.getSuccessMatch() == null ? null : Pattern.compile(page.getSuccessMatch()));
            m_failurePattern = (page.getFailureMatch() == null ? null : Pattern.compile(page.getFailureMatch()));
            m_locationPattern = (page.getLocationMatch() == null ? null : Pattern.compile(page.getLocationMatch()));
            m_parentSequence = parent;

            for (Parameter parm : m_page.getParameter()) {
                m_parms.add(new BasicNameValuePair(parm.getKey(), parm.getValue()));
            }
        }

        @Override
        public String toString() {
            ToStringBuilder retval = new ToStringBuilder(this);
            retval.append("page.httpVersion", m_page.getHttpVersion());
            retval.append("page.host", m_page.getHost());
            retval.append("page.requireIPv4", m_page.getRequireIPv4());
            retval.append("page.requireIPv6", m_page.getRequireIPv6());
            retval.append("page.port", m_page.getPort());
            retval.append("page.method", m_page.getMethod());
            retval.append("page.virtualHost", m_page.getVirtualHost());
            retval.append("page.path", m_page.getPath());
            retval.append("page.query", m_page.getQuery());
            retval.append("page.successMatch", m_page.getSuccessMatch());
            retval.append("page.failureMatch", m_page.getFailureMatch());
            retval.append("page.locationMatch", m_page.getLocationMatch());
            return retval.toString();
        }

        void execute(DefaultHttpClient client, MonitoredService svc) {
            try {
                URI uri = getURI();
                PageSequenceHttpUriRequest method = getMethod(uri);

                if (getVirtualHost() != null) {
                    method.getParams().setParameter(ClientPNames.VIRTUAL_HOST, new HttpHost(getVirtualHost(), uri.getPort()));
                }

                if (getUserAgent() != null) {
                    method.getParams().setParameter(CoreProtocolPNames.USER_AGENT, getUserAgent());
                } else {
                    method.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "OpenNMS PageSequenceMonitor (Service name: " + svc.getSvcName() + ")");
                }

                if ("https".equals(uri.getScheme())) {
                    if (Boolean.parseBoolean(m_page.getDisableSslVerification())) {
                        final SchemeRegistry registry = client.getConnectionManager().getSchemeRegistry();
                        final Scheme https = registry.getScheme("https");

                        // Override the trust validation with a lenient implementation
                        final SSLSocketFactory factory = new SSLSocketFactory(SSLContext.getInstance(EmptyKeyRelaxedTrustSSLContext.ALGORITHM), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                        final Scheme lenient = new Scheme(https.getName(), https.getDefaultPort(), factory);
                        // This will replace the existing "https" schema
                        registry.register(lenient);
                    }
                }

                if (m_parms.size() > 0) {
                    method.setQueryParameters(expandParms(svc));
                }

                if (getUserInfo() != null) {
                    String userInfo = getUserInfo();
                    String[] streetCred = userInfo.split(":", 2);
                    if (streetCred.length == 2) {
                        client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(streetCred[0], streetCred[1]));
                    } else {
                        log().warn("Illegal value found for username/password HTTP credentials: " + userInfo);
                    }
                }

                long startTime = System.nanoTime();
                HttpResponse response = client.execute(method);
                long endTime = System.nanoTime();
                m_responseTime = (endTime - startTime) / 1000000.0;

                int code = response.getStatusLine().getStatusCode();
                if (!getRange().contains(code)) {
                    throw new PageSequenceMonitorException("response code out of range for uri:" + uri + ".  Expected " + getRange() + " but received " + code);
                }

                String responseString = EntityUtils.toString(response.getEntity());

                if (getLocationPattern() != null) {
                    Header locationHeader = response.getFirstHeader("location");
                    if (locationHeader == null) {
//TODO Markus please replace this old style log checks
//                        if (log().isDebugEnabled()) {
//                            log().debug("locationMatch was set, but no Location: header was returned at " + uri, new Exception());
                        LogUtils.debugf(this, "locationMatch was set, but no Location: header was returned at %s", uri);
//                        }
                        throw new PageSequenceMonitorException("locationMatch was set, but no Location: header was returned at " + uri);
                    }
                    Matcher matcher = getLocationPattern().matcher(locationHeader.getValue());
                    if (!matcher.find()) {
                        if (log().isDebugEnabled()) {
                            log().debug("failed to find '" + getLocationPattern() + "' in Location: header at " + uri + ":\n" + locationHeader.getValue(), new Exception());
                        }
                        throw new PageSequenceMonitorException("failed to find '" + getLocationPattern() + "' in Location: header at " + uri);
                    }
                }

                if (getFailurePattern() != null) {
                    Matcher matcher = getFailurePattern().matcher(responseString);
                    if (matcher.find()) {
                        throw new PageSequenceMonitorException(getResolvedFailureMessage(matcher));
                    }
                }

                if (getSuccessPattern() != null) {
                    Matcher matcher = getSuccessPattern().matcher(responseString);
                    if (!matcher.find()) {
                        if (log().isDebugEnabled()) {
                            log().debug("failed to find '" + getSuccessPattern() + "' in page content at " + uri + ":\n" + responseString.trim(), new Exception());
                        }
                        throw new PageSequenceMonitorException("failed to find '" + getSuccessPattern() + "' in page content at " + uri);
                    }
                    updateSequenceProperties(m_parentSequence.getSequenceProperties(), matcher);
                }

            } catch (NoSuchAlgorithmException e) {
                // Should never happen
                throw new PageSequenceMonitorException("Could not find appropriate SSL context provider: " + e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("unable to construct URL for page: " + e, e);
            } catch (IOException e) {
                if (log().isDebugEnabled()) {
                    log().debug("I/O Error " + e, e);
                }
                throw new PageSequenceMonitorException("I/O Error " + e, e);
            }
        }

        private List<NameValuePair> expandParms(MonitoredService svc) {
            List<NameValuePair> expandedParms = new ArrayList<NameValuePair>();
//            Properties svcProps = getServiceProperties(svc);
//            if (svcProps != null && log().isDebugEnabled()) {
//                log().debug("I have " + svcProps.size() + " service properties.");
//            }
//            Properties seqProps = getSequenceProperties();
//            if (seqProps != null && log().isDebugEnabled()) {
//                log().debug("I have " + seqProps.size() + " sequence properties.");
//            }
//            Properties assetProps = getNodeAssetProperties(svc);
//            if (assetProps != null && log().isDebugEnabled()) {
//                log().debug("I have " + assetProps.size() + "asset properties");
//            }
            for (NameValuePair nvp : m_parms) {
                String value = nvp.getValue(); //substitute((String) nvp.getValue(), svcProps, seqProps, assetProps);
                expandedParms.add(new BasicNameValuePair(nvp.getName(), value));
                if (log().isDebugEnabled() && !nvp.getValue().equals(value)) {
                    log().debug("Expanded parm with name '" + nvp.getName() + "' from '" + nvp.getValue() + "' to '" + value + "'");
                }
            }
            return expandedParms;
        }

        private void updateSequenceProperties(Properties props, Matcher matcher) {
            for (SessionVariable varBinding : m_page.getSessionVariableCollection()) {
                String vbName = varBinding.getName();
                String vbValue = matcher.group(varBinding.getMatchGroup());
                if (vbValue == null) {
                    vbValue = "";
                }
                props.put(vbName, vbValue);
                if (log().isDebugEnabled()) {
                    log().debug("Just set session variable '" + vbName + "' to '" + vbValue + "'");
                }
            }
            // TODO MVR was ist mit den SequenceProperties? Die müssen wir auch noch substituten bzw. fürs substitute verwenden *Grrr*
        }

        private String getUserAgent() {
            return m_page.getUserAgent();
        }

        private String getVirtualHost() {
            return m_page.getVirtualHost();
        }

        private URI getURI() throws URISyntaxException {
            String host = getHost();
            if (m_page.getRequireIPv4()) {
                try {
                    InetAddress address = InetAddressUtils.resolveHostname(host, false);
                    if (!(address instanceof Inet4Address)) {
                        throw new UnknownHostException();
                    }
                    host = InetAddressUtils.str(address);
                } catch (UnknownHostException e) {
                    throw new PageSequenceMonitorException("failed to find IPv4 address for hostname: " + host);
                }
            } else if (m_page.getRequireIPv6()) {
                try {
                    InetAddress address = InetAddressUtils.resolveHostname(host, true);
                    host = "[" + InetAddressUtils.str(address) + "]";
                } catch (UnknownHostException e) {
                    throw new PageSequenceMonitorException("failed to find IPv6 address for hostname: " + host);
                }
            } else {
                // Just leave the hostname as-is, let httpclient resolve it using the platform preferences
            }
            return URIUtils.createURI(getScheme(), host, getPort(), getPath(), getQuery(), getFragment());
        }

        private String getFragment() {
            return m_page.getFragment();
        }

        private String getQuery() {
            return m_page.getQuery();
        }

        private String getPath() {
            return m_page.getPath();
        }

        private int getPort() {
            return m_page.getPort();
        }

        private String getHost() {
            return m_page.getHost();
        }

        private String getUserInfo() {
            return m_page.getUserInfo();
        }

        private String getScheme() {
            return m_page.getScheme();
        }

        private PageSequenceHttpUriRequest getMethod(URI uri) {
            String method = m_page.getMethod();
            return ("GET".equalsIgnoreCase(method) ? new PageSequenceHttpGet(uri) : new PageSequenceHttpPost(uri));
        }

        private HttpResponseRange getRange() {
            return m_range;
        }

        private Pattern getSuccessPattern() {
            return m_successPattern;
        }

        private Pattern getLocationPattern() {
            return m_locationPattern;
        }

        private Pattern getFailurePattern() {
            return m_failurePattern;
        }

        private String getFailureMessage() {
            return m_page.getFailureMessage();
        }

        private String getResolvedFailureMessage(Matcher matcher) {
            return PropertiesUtils.substitute(getFailureMessage(), new MatchTable(matcher));
        }

        private Properties getSequenceProperties() {
            return m_parentSequence.getSequenceProperties();
        }

        private void setSequenceProperties(Properties props) {
            m_parentSequence.setSequenceProperties(props);
        }

        public Number getResponseTime() {
            return m_responseTime;
        }

        public String getDsName() {
            return m_page.getDsName();
        }

        private ThreadCategory log() {
            return ThreadCategory.getInstance(getClass());
        }
    }

    public static class PageSequenceMonitorParameters {

        public static final String KEY = PageSequenceMonitorParameters.class.getName();

        @SuppressWarnings("unchecked")
        static synchronized PageSequenceMonitorParameters get(MonitoredService svc, Map paramterMap) {
            PageSequenceMonitorParameters parms = (PageSequenceMonitorParameters) paramterMap.get(KEY);
            if (parms == null) {
                parms = new PageSequenceMonitorParameters(svc, paramterMap);
                paramterMap.put(KEY, parms);
            }
            return parms;
        }
        private final Map<String, String> m_parameterMap;
        private final HttpParams m_clientParams;
        private final HttpPageSequence m_pageSequence;

        @SuppressWarnings("unchecked")
        PageSequenceMonitorParameters(MonitoredService svc, Map<String, String> parameterMap) {
            m_parameterMap = parameterMap;
            String pageSequence = getStringParm("page-sequence", null);
            if (pageSequence == null) {
                throw new IllegalArgumentException("page-sequence must be set in monitor parameters");
            }
            // Perform parameter expansion on the page-sequence string
//            pageSequence = substitute(pageSequence, m_parameterMap);
            PageSequence sequence = parsePageSequence(pageSequence);
            m_pageSequence = new HttpPageSequence(svc, sequence);
            m_pageSequence.setParameters(m_parameterMap);

            m_clientParams = createClientParams();
        }

        Map<String, String> getParameterMap() {
            return m_parameterMap;
        }

        HttpPageSequence getPageSequence() {
            return m_pageSequence;
        }

        PageSequence parsePageSequence(String sequenceString) {
            try {
                return CastorUtils.unmarshal(PageSequence.class, new ByteArrayInputStream(sequenceString.getBytes("UTF-8")));
            } catch (MarshalException e) {
                throw new IllegalArgumentException("Unable to parse page-sequence for HttpMonitor: " + e + "\nConfig: " + sequenceString, e);
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Unable to validate page-sequence for HttpMonitor: " + e + "\nConfig: " + sequenceString, e);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("UTF-8 encoding not supported", e);
            }

        }

        private String getStringParm(String key, String defaultValue) {
            return ParameterMap.getKeyedString(this.getParameterMap(), key, defaultValue);
        }

        private int getIntParm(String key, int defaultValue) {
            return ParameterMap.getKeyedInteger(getParameterMap(), key, defaultValue);
        }

        private HttpParams createClientParams() {
            HttpParams clientParams = new BasicHttpParams();
            clientParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, getTimeout());
            clientParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, getTimeout());
            clientParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
            // Not sure if this flag has any effect under the new httpcomponents-client code
            clientParams.setBooleanParameter("http.protocol.single-cookie-header", true);
            return clientParams;
        }

        public int getRetries() {
            return getIntParm("retry", PageSequenceMonitorEnhanced.DEFAULT_RETRY);
        }

        public int getTimeout() {
            return getIntParm("timeout", PageSequenceMonitorEnhanced.DEFAULT_TIMEOUT);
        }

        public HttpParams getClientParams() {
            return m_clientParams;
        }

        DefaultHttpClient createHttpClient() {
            DefaultHttpClient client = new DefaultHttpClient(getClientParams());

            client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(getRetries(), false));

            return client;
        }
    }

//    // TODO MVR
//    private static class AssetRecordSymbolTable implements PropertiesUtils.SymbolTable {
//
//        @Override
//        public String getSymbolValue(String symbol) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//    }
//
//    private static String substitute(String initialString, PropertiesUtils.SymbolTable symbolTable) {
//        return PropertiesUtils.substitute(initialString, symbolTable);
//    }
//
//    private static String substitute(String initialString, Properties... properties) {
//        return PropertiesUtils.substitute(initialString, properties);
//    }
//
//    private static String substitute(String pageSequence, Map<String, String> parameterMap) {
//        return PropertiesUtils.substitute(pageSequence, parameterMap);
//    }
    
    private static void substitute(MonitoredService svc, Object bean) {
        Properties[] properties = new Properties[]{getNodeAssetProperties(svc), getServiceProperties(svc)};
        for (Entry<String, String> eachEntry : describe(bean).entrySet()) {
            try {
                PropertyUtils.setProperty(bean, eachEntry.getKey(), PropertiesUtils.substitute(eachEntry.getValue(), properties));
            } catch (IllegalAccessException ex) {
                // TODO MVR logging + exception handling
                Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                // TODO MVR logging + exception handling
                Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                // TODO MVR logging + exception handling
                Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // TODO MVR comment: 
    // updates the parameterMap and substitutes all string values in the map with values from propertiees....
    private static void substituteValuesInParameterMap(MonitoredService svc, Map<String, Object> parameterMap) {
        Properties[] properties = new Properties[]{getNodeAssetProperties(svc), getServiceProperties(svc)};
        for (Entry<String, Object> eachEntry : parameterMap.entrySet()) {
            if (eachEntry.getValue() instanceof String) {
                parameterMap.put(eachEntry.getKey(), PropertiesUtils.substitute((String) eachEntry.getValue(), properties));
            } else {
                for (Entry<String, String> eachSubEntry : describe(eachEntry.getValue()).entrySet()) {
                    parameterMap.put(eachSubEntry.getKey(), PropertiesUtils.substitute(eachSubEntry.getValue(), properties));
                }
            }
        }
    }

    // TODO MVR comment, map die zurückgegeben wird ist nicht substituted!
    private static Map<String, String> describe(Object bean) {
        Map<String, String> propertyMap = new HashMap<String, String>();
        if (bean != null) {
            try {
                Map descriptionMap = PropertyUtils.describe(bean);
                for (Object eachEntryObject : descriptionMap.entrySet()) {
                    Entry<Object, Object> eachEntry = (Entry<Object, Object>) eachEntryObject;
                    if (eachEntry.getValue() != null && eachEntry.getValue() instanceof String) {
                        String eachKey = (String) eachEntry.getKey();
                        propertyMap.put(eachKey, eachEntry.getValue().toString());
                    }
                }
            } catch (IllegalAccessException ex) {
                Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
                // TODO MVR error handling + logging
            } catch (InvocationTargetException ex) {
                Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
                // TODO MVR error handling + logging
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
                // TODO MVR error handling + logging
            }
        }
        return propertyMap;
    }

    private static Properties getServiceProperties(MonitoredService svc) {
        Properties properties = new Properties();
        properties.put("ipaddr", svc.getIpAddr());
        properties.put("nodeid", svc.getNodeId());
        properties.put("nodelabel", svc.getNodeLabel());
        properties.put("svcname", svc.getSvcName());
        return properties;
    }

    private static Properties getNodeAssetProperties(MonitoredService svc) {
        Properties assetProperties = new Properties();

//        // get the DAO for the node
//        s_nodeDao = (NodeDao) BeanUtils.getFactory("commonContext", ClassPathXmlApplicationContext.class).getBean("nodeDao");
//        OnmsNode node = s_nodeDao.get(svc.getNodeId());
//
//        // get all AssetRecord properties
//        final List<String> propertiesToIgnore = Arrays.asList(new String[]{"class"});
//        try {
//            Map propertyMap = PropertyUtils.describe(node.getAssetRecord());
//            for (Object eachPropertyKey : propertyMap.keySet()) {
//                String eachStringPropertyKey = (String) eachPropertyKey;
//                if (propertiesToIgnore.contains(eachStringPropertyKey)) continue;
//                assetProperties.put("AssetRecord." + eachStringPropertyKey, propertyMap.get(eachPropertyKey));
//            }
//        } catch (IllegalAccessException ex) {
//            // TODO MVR logging and Exception handling ...
//            Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InvocationTargetException ex) {
//            // TODO MVR logging and Exception handling ...
//            Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NoSuchMethodException ex) {
//            // TODO MVR logging and Exception handling ...
//            Logger.getLogger(PageSequenceMonitorEnhanced.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return assetProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameterMap) {
        DefaultHttpClient client = null;
        PollStatus serviceStatus = PollStatus.unavailable("Poll not completed yet");

        Map<String, Number> responseTimes = new LinkedHashMap<String, Number>();

        substituteValuesInParameterMap(svc, parameterMap);

        SequenceTracker tracker = new SequenceTracker(parameterMap, DEFAULT_SEQUENCE_RETRY, DEFAULT_TIMEOUT);
        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
            try {
                PageSequenceMonitorParameters parms = PageSequenceMonitorParameters.get(svc, parameterMap);
                client = parms.createHttpClient();

                tracker.startAttempt();
                responseTimes.put("response-time", Double.NaN);
                parms.getPageSequence().execute(client, svc, responseTimes);

                double responseTime = tracker.elapsedTimeInMillis();
                serviceStatus = PollStatus.available();
                responseTimes.put("response-time", responseTime);
                serviceStatus.setProperties(responseTimes);

            } catch (PageSequenceMonitorException e) {
                serviceStatus = PollStatus.unavailable(e.getMessage());
                serviceStatus.setProperties(responseTimes);
            } catch (IllegalArgumentException e) {
                log().error("Invalid parameters to monitor: " + e.getMessage(), e);
                serviceStatus = PollStatus.unavailable("Invalid parameter to monitor: " + e.getMessage() + ".  See log for details.");
                serviceStatus.setProperties(responseTimes);
            } finally {
                // Do we need to do any cleanup?
                //if (client != null) {
                //    client.getHttpConnectionManager().closeIdleConnections(0);
                //}
            }
        }

        return serviceStatus;
    }
}
