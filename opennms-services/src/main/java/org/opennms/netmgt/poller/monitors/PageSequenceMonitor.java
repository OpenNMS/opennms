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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
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
 */
@Distributable
public class PageSequenceMonitor extends IPv4Monitor {
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
        PageSequence m_sequence;
        List<HttpPage> m_pages;
        Properties m_sequenceProperties;

        HttpPageSequence(PageSequence sequence) {
            m_sequence = sequence;

            m_pages = new ArrayList<HttpPage>(m_sequence.getPageCount());
            for (Page page : m_sequence.getPage()) {
                m_pages.add(new HttpPage(this, page));
            }

            m_sequenceProperties = new Properties();
        }

        List<HttpPage> getPages() {
            return m_pages;
        }

        private void execute(HttpClient client, MonitoredService svc, Map<String,Number> responseTimes) {
            // Clear the sequence properties before each run
            clearSequenceProperties();
            
            // Initialize the response time on each page that saves it
            for (HttpPage page : getPages()) {
                if (page.getDsName() != null) {
                    responseTimes.put(page.getDsName(), Double.NaN);
                }
            }
            
            for (HttpPage page : getPages()) {
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

    public interface PageSequenceHttpMethod extends HttpMethod {
        public void setParameters(NameValuePair[] parms);
    }

    public static class PageSequenceHttpPostMethod extends PostMethod implements PageSequenceHttpMethod {

        public void setParameters(NameValuePair[] parms) {
            setRequestBody(parms);
        }

    }

    public static class PageSequenceHttpGetMethod extends GetMethod implements PageSequenceHttpMethod {

        public void setParameters(NameValuePair[] parms) {
            setQueryString(parms);
        }

    }

    public static class HttpPage {
        private Page m_page;
        private HttpResponseRange m_range;
        private Pattern m_successPattern;
        private Pattern m_failurePattern;
        private HttpPageSequence m_parentSequence;
        private double m_responseTime;

        private NameValuePair[] m_parms;

        HttpPage(HttpPageSequence parent, Page page) {
            m_page = page;
            m_range = new HttpResponseRange(page.getResponseRange());
            m_successPattern = (page.getSuccessMatch() == null ? null : Pattern.compile(page.getSuccessMatch()));
            m_failurePattern = (page.getFailureMatch() == null ? null : Pattern.compile(page.getFailureMatch()));
            m_parentSequence = parent;

            List<NameValuePair> parms = new ArrayList<NameValuePair>();
            for (Parameter parm : m_page.getParameter()) {
                parms.add(new NameValuePair(parm.getKey(), parm.getValue()));
            }

            m_parms = parms.toArray(new NameValuePair[parms.size()]);
        }

        void execute(HttpClient client, MonitoredService svc, Properties sequenceProperties) {
            try {
                URI uri = getURI(svc);
                PageSequenceHttpMethod method = getMethod();
                method.setURI(uri);

                if (getVirtualHost(svc) != null) {
                    method.getParams().setVirtualHost(getVirtualHost(svc));
                }

                if (getUserAgent() != null) {
                    method.addRequestHeader("User-Agent", getUserAgent());
                } else {
                    method.addRequestHeader("User-Agent", "OpenNMS PageSequenceMonitor (Service name: " + svc.getSvcName() + ")");
                }

                if (m_parms.length > 0) {
                    method.setParameters(expandParms(svc));
                }

                if (m_page.getUserInfo() != null) {
                    String userInfo = m_page.getUserInfo();
                    String[] streetCred = userInfo.split(":", 2);
                    if (streetCred.length == 2) {
                        client.getState().setCredentials(new AuthScope(AuthScope.ANY), new UsernamePasswordCredentials(streetCred[0], streetCred[1]));
                        method.setDoAuthentication(true);
                    }
                }
                
                long startTime = System.nanoTime();
                int code = client.executeMethod(method);
                long endTime = System.nanoTime();
                m_responseTime = (endTime - startTime)/1000000.0;

                if (!getRange().contains(code)) {
                    throw new PageSequenceMonitorException("response code out of range for uri:" + uri + ".  Expected " + getRange() + " but received " + code);
                }

                /*
                 * We do the work below so we don't get this message logged
                 * by the HTTP client at the WARN level:
                 * 
                 *      org.apache.commons.httpclient.HttpMethodBase: Going to
                 *      buffer response body of large or unknown size. Using
                 *      getResponseBodyAsStream instead is recommended.
                 *      
                 * Note: that warning message doesn't get presented if the
                 * server reports the size of the document, but oftentimes
                 * during an error (or in other cases) it will not report the
                 * size of the result. Using the code below we ensure that
                 * no matter what the size is, a warning won't be generated.
                 */
                InputStream inputStream = method.getResponseBodyAsStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    IOUtils.copy(inputStream, outputStream);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
                String responseString = outputStream.toString();

                if (getFailurePattern() != null) {
                    Matcher matcher = getFailurePattern().matcher(responseString);
                    if (matcher.find()) {
                        throw new PageSequenceMonitorException(getResolvedFailureMessage(matcher));
                    }
                }

                if (getSuccessPattern() != null) {
                    Matcher matcher = getSuccessPattern().matcher(responseString);
                    if (!matcher.find()) {
                        throw new PageSequenceMonitorException("failed to find '" + getSuccessPattern() + "' in page content at " + uri);
                    }
                    updateSequenceProperties(sequenceProperties, matcher);
                }

            } catch (URIException e) {
                throw new IllegalArgumentException("unable to construct URL for page: " + e, e);
            } catch (HttpException e) {
                throw new PageSequenceMonitorException("HTTP Error " + e, e);
            } catch (IOException e) {
                throw new PageSequenceMonitorException("I/O Error " + e, e);
            }
        }

        private NameValuePair[] expandParms(MonitoredService svc) {
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
                NameValuePair expanded = new NameValuePair();
                expanded.setName(nvp.getName());
                expanded.setValue(PropertiesUtils.substitute(nvp.getValue(), getServiceProperties(svc), getSequenceProperties()));
                expandedParms.add(expanded);
                if (log().isDebugEnabled() && !nvp.getValue().equals(expanded.getValue()) ) {
                    log().debug("Expanded parm with name '" + nvp.getName() + "' from '" + nvp.getValue() + "' to '" + expanded.getValue() + "'");
                }
            }
            return expandedParms.toArray(new NameValuePair[expandedParms.size()]);
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

        private URI getURI(MonitoredService svc) throws URIException {
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

        private PageSequenceHttpMethod getMethod() {
            String method = m_page.getMethod();
            return ("GET".equalsIgnoreCase(method) ? new PageSequenceHttpGetMethod() : new PageSequenceHttpPostMethod());
        }

        private HttpResponseRange getRange() {
            return m_range;
        }

        private Pattern getSuccessPattern() {
            return m_successPattern;
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
        private HttpClientParams m_clientParams;
        private HttpPageSequence m_pageSequence;

        PageSequenceMonitorParameters(Map<String, String> parameterMap) {
            m_parameterMap = parameterMap;
            String pageSequence = getStringParm("page-sequence", null);
            if (pageSequence == null) {
                throw new IllegalArgumentException("page-sequence must be set in monitor parameters");
            }
            PageSequence sequence = parsePageSequence(pageSequence);
            m_pageSequence = new HttpPageSequence(sequence);

            createClientParams();
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

        private void createClientParams() {
            m_clientParams = new HttpClientParams();
            m_clientParams.setConnectionManagerTimeout(getTimeout());
            m_clientParams.setSoTimeout(getTimeout());
            m_clientParams.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(getRetries(), false));
            m_clientParams.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        }

        public int getRetries() {
            return getIntParm("retry", PageSequenceMonitor.DEFAULT_RETRY);
        }

        public int getTimeout() {
            return getIntParm("timeout", PageSequenceMonitor.DEFAULT_TIMEOUT);
        }

        public HttpClientParams getClientParams() {
            return m_clientParams;
        }

        HttpClient createHttpClient() {
            HttpClient client = new HttpClient(getClientParams());
            client.getHttpConnectionManager().getParams().setConnectionTimeout(getTimeout());
            return client;
        }
    }

    public PollStatus poll(MonitoredService svc, Map<String, Object> parameterMap) {
        HttpClient client = null;
        PollStatus serviceStatus = PollStatus.unavailable("");
        
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
            log().error("Invalid parameters to monitor: " + e, e);
            serviceStatus = PollStatus.unavailable("Invalid parameter to monitor: " + e.getMessage() + ".  See log for details.");
            serviceStatus.setProperties(responseTimes);
            return serviceStatus;
        } finally {
            if (client != null) {
                client.getHttpConnectionManager().closeIdleConnections(0);
            }
        }
    }

}
