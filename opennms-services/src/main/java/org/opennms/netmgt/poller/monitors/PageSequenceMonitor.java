//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2010 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2010 Feb 24: Incorporate ds-name per page as suggested by Jean-Marie
//              Kubek in bug 3142. - jeffg@opennms.org for bofh.jr@gmail.com
// 2010 Feb 23: Make it possible to reference the contents of matching groups
//              from a page's "match" regex in the params and regexes of later
//              pages in a sequence. - jeffg@opennms.org
// 2008 Jan 23: Perty things up a bit. - dj@opennms.org
// 2007 Apr 06: Make sure we close {Input,Output}Streams. - dj@opennms.org
// 2007 Apr 06: Use getResponseBodyAsStream to get the response from the HTTP
//              client to avoid a possible WARN message.  Also eliminate a
//              compile warning. - dj@opennms.org
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
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
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.MatchTable;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.pagesequence.Page;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.config.pagesequence.Parameter;
import org.opennms.netmgt.config.pagesequence.SessionVariable;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the HTTP service on remote interfaces. The class implements the ServiceMonitor interface
 * that allows it to be used along with other plug-ins by the service poller framework.
 *
 * @author ranger
 * @version $Id: $
 */
@Distributable
public class PageSequenceMonitor extends IPv4Monitor {
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

    private static final class EmptyKeyRelaxedTrustProvider extends Provider {
        protected EmptyKeyRelaxedTrustProvider() {
            super(EmptyKeyRelaxedTrustSSLContext.ALGORITHM + "Provider", 1.0, null);
            put(
                "SSLContext." + EmptyKeyRelaxedTrustSSLContext.ALGORITHM,
                EmptyKeyRelaxedTrustSSLContext.class.getName()
            );
        }
    }

    public static final class EmptyKeyRelaxedTrustSSLContext extends SSLContextSpi {
        public static final String ALGORITHM = "EmptyKeyRelaxedTrust";

        private final SSLContext m_delegate;

        public EmptyKeyRelaxedTrustSSLContext() {
            SSLContext customContext = null;

            try {
                // Use a blank list of key managers so no SSL keys will be available
                KeyManager[] keyManager = null;
                TrustManager[] trustManagers = { new X509TrustManager() {

                    public void checkClientTrusted(X509Certificate[] chain,
                            String authType) throws CertificateException {
                        // Perform no checks
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                            String authType) throws CertificateException {
                        // Perform no checks
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }}
                };
                customContext = SSLContext.getInstance("SSL");
                customContext.init(keyManager, trustManagers, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                // Should never happen
                ThreadCategory.getInstance(this.getClass()).error("Could not find SSL algorithm in JVM", e);
            } catch (KeyManagementException e) {
                // Should never happen
                ThreadCategory.getInstance(this.getClass()).error("Could not find SSL algorithm in JVM", e);
            }
            m_delegate = customContext;
        }

        @Override
        protected SSLEngine engineCreateSSLEngine() {
            return m_delegate.createSSLEngine();
        }

        @Override
        protected SSLEngine engineCreateSSLEngine(String arg0, int arg1) {
            return m_delegate.createSSLEngine(arg0, arg1);
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            return m_delegate.getClientSessionContext();
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            return m_delegate.getServerSessionContext();
        }

        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            return m_delegate.getServerSocketFactory();
        }

        @Override
        protected javax.net.ssl.SSLSocketFactory engineGetSocketFactory() {
            return m_delegate.getSocketFactory();
        }

        @Override
        protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom arg2) throws KeyManagementException {
            // Don't do anything, we've already initialized everything in the constructor
        }
    }

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RETRY = 0;

    public static class HttpPageSequence {
        PageSequence m_sequence;
        List<HttpPage> m_pages;
        Properties m_sequenceProperties;
        Map<String,String> m_parameters = new HashMap<String,String>();

        HttpPageSequence(PageSequence sequence) {
            m_sequence = sequence;

            m_pages = new ArrayList<HttpPage>(m_sequence.getPageCount());
            for (Page page : m_sequence.getPage()) {
                m_pages.add(new HttpPage(this, page));
            }

            m_sequenceProperties = new Properties();
        }

        public Map<String,String> getParameters() {
            return m_parameters;
        }

        public void setParameters(Map<String,String> parameters) {
            m_parameters = parameters;
        }

        List<HttpPage> getPages() {
            return m_pages;
        }

        private void execute(DefaultHttpClient client, MonitoredService svc, Map<String,Number> responseTimes) {
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
                page.execute(client, svc, m_sequenceProperties);
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

    public static class HttpResponseRange {
        private static final Pattern RANGE_PATTERN = Pattern.compile("([1-5][0-9][0-9])(?:-([1-5][0-9][0-9]))?");
        private int m_begin;
        private int m_end;

        HttpResponseRange(String rangeSpec) {
            Matcher matcher = RANGE_PATTERN.matcher(rangeSpec);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid range spec: " + rangeSpec);
            }

            String beginSpec = matcher.group(1);
            String endSpec = matcher.group(2);

            m_begin = Integer.parseInt(beginSpec);

            if (endSpec == null) {
                m_end = m_begin;
            } else {
                m_end = Integer.parseInt(endSpec);
            }
        }

        public boolean contains(int responseCode) {
            return (m_begin <= responseCode && responseCode <= m_end);
        }

        public String toString() {
            if (m_begin == m_end) {
                return Integer.toString(m_begin);
            } else {
                return Integer.toString(m_begin) + '-' + Integer.toString(m_end);
            }
        }
    }

    public interface PageSequenceHttpUriRequest extends HttpUriRequest {
        public void setQueryParameters(List<NameValuePair> parms);
    }

    public static class PageSequenceHttpPost extends HttpPost implements PageSequenceHttpUriRequest {
        public PageSequenceHttpPost(URI uri) {
            super(uri);
        }

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

        public void setQueryParameters(List<NameValuePair> parms) {
            URI uri = this.getURI();
            URI uriWithQueryString = null;
            try {
                String query = URLEncodedUtils.format(parms, "UTF-8");
                uriWithQueryString = new URI(
                                             uri.getScheme(), 
                                             uri.getUserInfo(), 
                                             uri.getHost(), 
                                             uri.getPort(), 
                                             uri.getPath(),
                                             // Do we need to merge any existing query params?
                                             // Probably not... shouldn't be any.
                                             query, 
                                             uri.getFragment()
                );
            } catch (URISyntaxException e) {
                ThreadCategory.getInstance(this.getClass()).warn(e.getMessage(), e);
            }
            this.setURI(uriWithQueryString);
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
            StringBuffer retval = new StringBuffer();
            retval.append("HttpPage { ");
            retval.append("page.host => ").append(m_page.getHost()).append(", ");
            retval.append("page.port => ").append(m_page.getPort()).append(", ");
            retval.append("page.path => ").append(m_page.getPath()).append(", ");
            retval.append("page.successMatch => '").append(m_page.getSuccessMatch()).append("', ");
            retval.append("page.failureMatch => '").append(m_page.getFailureMatch()).append("', ");
            retval.append("page.locationMatch => '").append(m_page.getLocationMatch()).append("'");
            retval.append(" }");
            return retval.toString();
        }

        void execute(DefaultHttpClient client, MonitoredService svc, Properties sequenceProperties) {
            try {
                URI uri = getURI(svc);
                PageSequenceHttpUriRequest method = getMethod(uri);

                if (getVirtualHost(svc) != null) {
                    method.getParams().setParameter(ClientPNames.VIRTUAL_HOST, new HttpHost(getVirtualHost(svc), uri.getPort()));
                }

                if (getUserAgent() != null) {
                    method.getParams().setParameter(CoreProtocolPNames.USER_AGENT, getUserAgent());
                } else {
                    method.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "OpenNMS PageSequenceMonitor (Service name: " + svc.getSvcName() + ")");
                }

                if ("https".equals(uri.getScheme())) {
                    if (Boolean.parseBoolean(m_page.getDisableSslVerification())) {
                        SchemeRegistry registry = client.getConnectionManager().getSchemeRegistry();
                        Scheme https = registry.getScheme("https");
                        // Override the trust validation with a lenient implementation
                        SSLSocketFactory factory = new SSLSocketFactory(SSLContext.getInstance(EmptyKeyRelaxedTrustSSLContext.ALGORITHM));

                        // @see http://hc.apache.org/httpcomponents-client-4.0.1/tutorial/html/connmgmt.html
                        factory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                        Scheme lenient = new Scheme(https.getName(), factory, https.getDefaultPort());
                        // This will replace the existing "https" schema
                        registry.register(lenient);
                    }
                }

                if (m_parms.size() > 0) {
                    method.setQueryParameters(expandParms(svc));
                }

                if (m_page.getUserInfo() != null) {
                    String userInfo = m_page.getUserInfo();
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
                m_responseTime = (endTime - startTime)/1000000.0;

                int code = response.getStatusLine().getStatusCode();
                if (!getRange().contains(code)) {
                    throw new PageSequenceMonitorException("response code out of range for uri:" + uri + ".  Expected " + getRange() + " but received " + code);
                }

                String responseString = EntityUtils.toString(response.getEntity());

                if (getLocationPattern() != null) {
                    Header locationHeader = response.getFirstHeader("location");
                    if (locationHeader == null) {
                        if (log().isDebugEnabled()) {
                            log().debug("locationMatch was set, but no Location: header was returned at " + uri, new Exception());
                        }
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
                    updateSequenceProperties(sequenceProperties, matcher);
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
            Properties svcProps = getServiceProperties(svc);
            if (svcProps != null && log().isDebugEnabled()) {
                log().debug("I have " + svcProps.size() + " service properties.");
            }
            Properties seqProps = getSequenceProperties();
            if (seqProps != null && log().isDebugEnabled()) {
                log().debug("I have " + seqProps.size() + " sequence properties.");
            }
            for (NameValuePair nvp : m_parms) {
                String value = PropertiesUtils.substitute((String)nvp.getValue(), getServiceProperties(svc), getSequenceProperties());
                expandedParms.add(new BasicNameValuePair(nvp.getName(), value));
                if (log().isDebugEnabled() && !nvp.getValue().equals(value) ) {
                    log().debug("Expanded parm with name '" + nvp.getName() + "' from '" + nvp.getValue() + "' to '" + value + "'");
                }
            }
            return expandedParms;
        }

        private void updateSequenceProperties(Properties props, Matcher matcher) {
            for (SessionVariable varBinding : m_page.getSessionVariableCollection()) {
                String vbName = varBinding.getName();
                String vbValue = matcher.group(varBinding.getMatchGroup());
                if (vbValue == null)
                    vbValue = "";
                props.put(vbName, vbValue);
                if (log().isDebugEnabled()) {
                    log().debug("Just set session variable '" + vbName + "' to '" + vbValue + "'");
                }
            }

            setSequenceProperties(props);
        }

        private String getUserAgent() {
            return m_page.getUserAgent();
        }

        private String getVirtualHost(MonitoredService svc) {
            return PropertiesUtils.substitute(m_page.getVirtualHost(), getServiceProperties(svc), getSequenceProperties());
        }

        private URI getURI(MonitoredService svc) throws URISyntaxException {
            Properties svcProps = getServiceProperties(svc);
            Properties seqProps = getSequenceProperties();
            return new URI(getScheme(), getUserInfo(), getHost(seqProps, svcProps), getPort(), getPath(seqProps, svcProps), getQuery(seqProps, svcProps), getFragment(seqProps, svcProps));
        }

        private String getFragment(Properties... p) {
            return PropertiesUtils.substitute(m_page.getFragment(), p);
        }

        private String getQuery(Properties... p) {
            return PropertiesUtils.substitute(m_page.getQuery(), p);
        }

        private String getPath(Properties... p) {
            return PropertiesUtils.substitute(m_page.getPath(), p);
        }

        private int getPort(Properties... p) {
            return Integer.valueOf(PropertiesUtils.substitute(String.valueOf(m_page.getPort()), p));
        }

        private String getHost(Properties... p) {
            return PropertiesUtils.substitute(m_page.getHost(), p);
        }

        private Properties getServiceProperties(MonitoredService svc) {
            Properties properties = new Properties();
            properties.put("ipaddr", svc.getIpAddr());
            properties.put("nodeid", svc.getNodeId());
            properties.put("nodelabel", svc.getNodeLabel());
            properties.put("svcname", svc.getSvcName());
            return properties;
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
        static synchronized PageSequenceMonitorParameters get(Map paramterMap) {
            PageSequenceMonitorParameters parms = (PageSequenceMonitorParameters) paramterMap.get(KEY);
            if (parms == null) {
                parms = new PageSequenceMonitorParameters(paramterMap);
                paramterMap.put(KEY, parms);
            }
            return parms;
        }

        private Map<String, String> m_parameterMap;
        private HttpParams m_clientParams;
        private HttpPageSequence m_pageSequence;

        PageSequenceMonitorParameters(Map<String, String> parameterMap) {
            m_parameterMap = parameterMap;
            String pageSequence = getStringParm("page-sequence", null);
            if (pageSequence == null) {
                throw new IllegalArgumentException("page-sequence must be set in monitor parameters");
            }
            PageSequence sequence = parsePageSequence(pageSequence);
            m_pageSequence = new HttpPageSequence(sequence);
            m_pageSequence.setParameters(parameterMap);

            m_clientParams = createClientParams();
        }

        Map<String, String> getParameterMap() {
            return m_parameterMap;
        }

        HttpPageSequence getPageSequence() {
            return m_pageSequence;
        }

        @SuppressWarnings("deprecation")
        PageSequence parsePageSequence(String sequenceString) {
            try {
                return CastorUtils.unmarshal(PageSequence.class, new StringReader(sequenceString));
            } catch (MarshalException e) {
                throw new IllegalArgumentException("Unable to parse page-sequence for HttpMonitor: " + e + "\nConfig: " + sequenceString, e);
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Unable to validate page-sequence for HttpMonitor: " + e + "\nConfig: " + sequenceString, e);
            }

        }

        private String getStringParm(String key, String deflt) {
            return ParameterMap.getKeyedString(this.getParameterMap(), key, deflt);
        }

        private int getIntParm(String key, int defValue) {
            return ParameterMap.getKeyedInteger(getParameterMap(), key, defValue);
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
            return getIntParm("retry", PageSequenceMonitor.DEFAULT_RETRY);
        }

        public int getTimeout() {
            return getIntParm("timeout", PageSequenceMonitor.DEFAULT_TIMEOUT);
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

    /** {@inheritDoc} */
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameterMap) {
        DefaultHttpClient client = null;
        PollStatus serviceStatus = PollStatus.unavailable("Poll not completed yet");

        Map<String,Number> responseTimes = new LinkedHashMap<String,Number>();

        try {
            PageSequenceMonitorParameters parms = PageSequenceMonitorParameters.get(parameterMap);

            client = parms.createHttpClient();

            long startTime = System.nanoTime();
            responseTimes.put("response-time", Double.NaN);
            parms.getPageSequence().execute(client, svc, responseTimes);

            long endTime = System.nanoTime();
            double responseTime = (endTime - startTime) / 1000000.0;
            serviceStatus = PollStatus.available();
            responseTimes.put("response-time", responseTime);
            serviceStatus.setProperties(responseTimes);

            return serviceStatus;
        } catch (PageSequenceMonitorException e) {
            serviceStatus = PollStatus.unavailable(e.getMessage());
            serviceStatus.setProperties(responseTimes);
            return serviceStatus;
        } catch (IllegalArgumentException e) {
            log().error("Invalid parameters to monitor: " + e.getMessage(), e);
            serviceStatus = PollStatus.unavailable("Invalid parameter to monitor: " + e.getMessage() + ".  See log for details.");
            serviceStatus.setProperties(responseTimes);
            return serviceStatus;
        } finally {
            // Do we need to do any cleanup?
            //if (client != null) {
            //    client.getHttpConnectionManager().closeIdleConnections(0);
            //}
        }
    }
}
