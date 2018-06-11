/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.http.HttpResponseRange;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.MatchTable;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.pagesequence.Page;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.config.pagesequence.Parameter;
import org.opennms.netmgt.config.pagesequence.SessionVariable;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.opennms.netmgt.utils.DnsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the HTTP service on remote interfaces. The class implements the ServiceMonitor interface
 * that allows it to be used along with other plug-ins by the service poller framework.
 *
 * @author <a mailto:brozow@opennms.org>Mathew Brozowski</a>
 */
@Distributable
public class PageSequenceMonitor extends AbstractServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(PageSequenceMonitor.class);

    protected static class SequenceTracker{

        TimeoutTracker m_tracker;
        public SequenceTracker(Map<String, Object> parameterMap, int defaultSequenceRetry, int defaultTimeout) {
            final Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("retry", getKeyedInteger(parameterMap, "sequence-retry", defaultSequenceRetry));
            parameters.put("timeout", getKeyedInteger(parameterMap, "timeout", defaultTimeout));
            parameters.put("strict-timeout", getKeyedBoolean(parameterMap, "strict-timeout", false));
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
        Map<String,Object> m_parameters = new HashMap<String,Object>();

        HttpPageSequence(final PageSequence sequence) {
            m_sequence = sequence;

            m_pages = new ArrayList<HttpPage>(m_sequence.getPages().size());
            for (Page page : m_sequence.getPages().toArray(new Page[0])) {
                m_pages.add(new HttpPage(this, page));
            }

            m_sequenceProperties = new Properties();
        }

        public Map<String,Object> getParameters() {
            return m_parameters;
        }

        public void setParameters(final Map<String,Object> parameters) {
            m_parameters = parameters;
        }

        List<HttpPage> getPages() {
            return m_pages;
        }

        private void execute(HttpClientWrapper clientWrapper, MonitoredService svc, Map<String,Number> responseTimes) {
            // Clear the sequence properties before each run
            clearSequenceProperties();

            // Initialize the response time on each page that saves it
            for (HttpPage page : getPages()) {
                if (page.getDsName() != null) {
                    responseTimes.put(page.getDsName(), Double.NaN);
                }
            }

            for (HttpPage page : getPages()) {
                LOG.debug("Executing HttpPage: {}", page.toString());
                page.execute(clientWrapper, svc, m_sequenceProperties);
                if (page.getDsName() != null) {
                    LOG.debug("Recording response time {} for ds {}", page.getResponseTime(), page.getDsName());
                    responseTimes.put(page.getDsName(), page.getResponseTime());
                }
            }

        }

        protected Properties getSequenceProperties() {
            return m_sequenceProperties;
        }

        protected void setSequenceProperties(final Properties newProps) {
            m_sequenceProperties = newProps;
        }

        protected void clearSequenceProperties() {
            m_sequenceProperties.clear();
        }
    }

    public interface PageSequenceHttpUriRequest extends HttpUriRequest {
        public void setQueryParameters(List<NameValuePair> parms);
    }

    public static class PageSequenceHttpPost extends HttpPost implements PageSequenceHttpUriRequest {
        public PageSequenceHttpPost(final URI uri) {
            super(uri);
        }

        @Override
        public void setQueryParameters(final List<NameValuePair> parms) {
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parms, StandardCharsets.UTF_8);
            this.setEntity(entity);
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
                String query = URLEncodedUtils.format(parms, StandardCharsets.UTF_8);
                URIBuilder ub = new URIBuilder(uri);
                final List<NameValuePair> params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
                if (!params.isEmpty()) {
                    ub.setParameters(params);
                }
                uriWithQueryString = ub.build();
                this.setURI(uriWithQueryString);
            } catch (URISyntaxException e) {
                LOG.warn(e.getMessage(), e);
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

        private final List<NameValuePair> m_parms = new ArrayList<>();

        HttpPage(HttpPageSequence parent, Page page) {
            m_page = page;
            m_range = new HttpResponseRange(page.getResponseRange());
            m_successPattern = (page.getSuccessMatch() == null ? null : Pattern.compile(page.getSuccessMatch()));
            m_failurePattern = (page.getFailureMatch() == null ? null : Pattern.compile(page.getFailureMatch()));
            m_locationPattern = (page.getLocationMatch() == null ? null : Pattern.compile(page.getLocationMatch()));
            m_parentSequence = parent;

            for (Parameter parm : m_page.getParameters().toArray(new Parameter[0])) {
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

        void execute(final HttpClientWrapper parentClientWrapper, final MonitoredService svc, final Properties sequenceProperties) {
            CloseableHttpResponse response = null;
            try (final HttpClientWrapper clientWrapper = parentClientWrapper.duplicate()) {
                URI uri = getURI(svc);
                PageSequenceHttpUriRequest method = getMethod(uri);

                if (getVirtualHost(svc) == null) {
                    LOG.debug("Adding request interceptor to remove the host header");
                    clientWrapper.addRequestInterceptor(new HttpRequestInterceptor() {
                        @Override
                        public void process(HttpRequest request, HttpContext ctx) throws HttpException, IOException {
                            Header host = request.getFirstHeader(HTTP.TARGET_HOST);
                            if (host != null) {
                                request.removeHeader(host);
                                LOG.debug("httpRequestInterceptor: virtual-host is not set, removing host header");
                            }
                        }
                    });
                } else {
                    HttpHost host = new HttpHost(getVirtualHost(svc), uri.getPort());
                    clientWrapper.setVirtualHost(host.toHostString());
                }

                switch(m_page.getHttpVersion()) {
                    case "0.9":
                        clientWrapper.setVersion(HttpVersion.HTTP_0_9); break;
                    case "1.0":
                        clientWrapper.setVersion(HttpVersion.HTTP_1_0); break;
                    default:
                        clientWrapper.setVersion(HttpVersion.HTTP_1_1); break;
                }

                if (getUserAgent() != null && !getUserAgent().trim().isEmpty()) {
                    clientWrapper.setUserAgent(getUserAgent());
                } else {
                    clientWrapper.setUserAgent("OpenNMS PageSequenceMonitor (Service name: " + svc.getSvcName() + ")");
                }

                if ("https".equals(uri.getScheme())) {
                    if (Boolean.parseBoolean(m_page.getDisableSslVerification())) {
                        try {
                            clientWrapper.useRelaxedSSL("https");
                        } catch (final GeneralSecurityException e) {
                            LOG.warn("Failed configure relaxed SSL for PageSequence {}", svc.getSvcName(), e);
                        }
                    }
                }

                if (m_parms.size() > 0) {
                    method.setQueryParameters(expandParms(svc));
                }

                if (getUserInfo() != null) {
                    String userInfo = getUserInfo();
                    String[] streetCred = userInfo.split(":", 2);
                    if (streetCred.length == 2) {
                        clientWrapper.addBasicCredentials(streetCred[0], streetCred[1]);
                    } else { 
                        LOG.warn("Illegal value found for username/password HTTP credentials: {}", userInfo);
                    }
                }

                long startTime = System.nanoTime();
                response = clientWrapper.execute(method);
                long endTime = System.nanoTime();
                m_responseTime = (endTime - startTime)/1000000.0;

                int code = response.getStatusLine().getStatusCode();
                if (!getRange().contains(code)) {
                    LOG.debug("Response code out of range for URI:" + uri + ".  Expected " + getRange() + " but received " + code);
                    throw new PageSequenceMonitorException("Response code out of range for URI:" + uri + ".  Expected " + getRange() + " but received " + code);
                }

                String responseString = EntityUtils.toString(response.getEntity());

                if (getLocationPattern() != null) {
                    Header locationHeader = response.getFirstHeader("location");
                    if (locationHeader == null) {
                        LOG.debug("locationMatch was set, but no Location: header was returned at {}", uri, new Exception());
                        throw new PageSequenceMonitorException("locationMatch was set, but no Location: header was returned at " + uri);
                    }
                    Matcher matcher = getLocationPattern().matcher(locationHeader.getValue());
                    if (!matcher.find()) {
                        LOG.debug("Failed to find '{}' in Location: header at {}:\n{}", getLocationPattern(), uri, locationHeader.getValue(), new Exception());
                        throw new PageSequenceMonitorException("Failed to find '" + getLocationPattern() + "' in Location: header at " + uri);
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
                        LOG.debug("Failed to find '{}' in page content at {}:\n{}", getSuccessPattern(), uri, responseString.trim(), new Exception());
                        throw new PageSequenceMonitorException("Failed to find '" + getSuccessPattern() + "' in page content at " + uri);
                    }
                    updateSequenceProperties(sequenceProperties, matcher);
                }

            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to construct URL for page", e);
            } catch (ConnectTimeoutException e) {
                // NMS-8098: Don't unwrap these exceptions, they have a better message
                // than the root cause exceptions do
                LOG.debug(e.getMessage(), e);
                throw new PageSequenceMonitorException(e.getMessage(), e);
            } catch (IOException e) {
                // NMS-8098: Unwrap the exception so we can get the most accurate
                // root cause message
                Throwable cause = e;
                while (cause.getCause() != null) {
                     cause = cause.getCause();
                }
                LOG.debug(cause.getMessage(), cause);
                throw new PageSequenceMonitorException(cause.getMessage(), cause);
            }
        }

        private List<NameValuePair> expandParms(MonitoredService svc) {
            List<NameValuePair> expandedParms = new ArrayList<>();
            Properties svcProps = getServiceProperties(svc);
            if (svcProps != null) {
                LOG.debug("I have {} service properties.", svcProps.size());
            }
            Properties seqProps = getSequenceProperties();
            if (seqProps != null) {
                LOG.debug("I have {} sequence properties.", seqProps.size());
            }
            for (NameValuePair nvp : m_parms) {
                String value = PropertiesUtils.substitute((String)nvp.getValue(), getServiceProperties(svc), getSequenceProperties());
                expandedParms.add(new BasicNameValuePair(nvp.getName(), value));
                if (!nvp.getValue().equals(value) ) {
                    LOG.debug("Expanded parm with name '{}' from '{}' to '{}'", nvp.getName(), nvp.getValue(), value);
                }
            }
            return expandedParms;
        }

        private void updateSequenceProperties(Properties props, Matcher matcher) {
            for (SessionVariable varBinding : m_page.getSessionVariables()) {
                String vbName = varBinding.getName();
                String vbValue = matcher.group(varBinding.getMatchGroup());
                if (vbValue == null)
                    vbValue = "";
                props.put(vbName, vbValue);
                LOG.debug("Just set session variable '{}' to '{}'", vbName, vbValue);
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
            String host = getHost(seqProps, svcProps);
            if (m_page.getRequireIPv4()) {
                try {
                    InetAddress address = DnsUtils.resolveHostname(host, false);
                    if (!(address instanceof Inet4Address)) throw new UnknownHostException();
                    host = InetAddressUtils.str(address);
                } catch (UnknownHostException e) {
                    throw new PageSequenceMonitorException("Failed to find IPv4 address for hostname: " + host);
                }
            } else if (m_page.getRequireIPv6()) {
                try {
                    InetAddress address = DnsUtils.resolveHostname(host, true);
                    host = "[" + InetAddressUtils.str(address) + "]";
                } catch (UnknownHostException e) {
                    throw new PageSequenceMonitorException("Failed to find IPv6 address for hostname: " + host);
                }
            } else {
                // Just leave the hostname as-is, let httpclient resolve it using the platform preferences
            }
            URIBuilder ub = new URIBuilder();
            ub.setScheme(getScheme());
            ub.setHost(host);
            ub.setPort(getPort());
            ub.setPath(getPath(seqProps, svcProps));
            final String query = getQuery(seqProps, svcProps);
            if (query != null) {
                final List<NameValuePair> params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
                ub.setParameters(params);
            }
            ub.setFragment(getFragment(seqProps, svcProps));
            return ub.build();
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
    }

    public static class PageSequenceMonitorParameters {
        public static final String KEY = PageSequenceMonitorParameters.class.getName();

        static synchronized PageSequenceMonitorParameters get(final Map<String,Object> parameterMap) {
            PageSequenceMonitorParameters parms = (PageSequenceMonitorParameters) parameterMap.get(KEY);
            if (parms == null) {
                parms = new PageSequenceMonitorParameters(parameterMap);
                parameterMap.put(KEY, parms);
            }
            return parms;
        }

        private final Map<String, Object> m_parameterMap;
        private final HttpPageSequence m_pageSequence;

        PageSequenceMonitorParameters(final Map<String, Object> parameterMap) {
            m_parameterMap = parameterMap;

            Object pageSequence = getKeyedObject(parameterMap, "page-sequence", null);

            if (pageSequence == null) {
                throw new IllegalArgumentException("page-sequence must be set in monitor parameters");
            }

            /* if we get an actual PageSequence object, we need
             * to do substitution on it first, so turn it back into
             * a string temporarily.
             */
            if (pageSequence instanceof PageSequence) {
                pageSequence = JaxbUtils.marshal(pageSequence);
            } else if (pageSequence instanceof String) {
                // don't need to do anything
            } else {
                throw new IllegalArgumentException("Unsure how to deal with Page Sequence of type " + pageSequence.getClass());
            }

            // Perform parameter expansion on the page-sequence string
            pageSequence = PropertiesUtils.substitute((String)pageSequence, m_parameterMap);
            PageSequence sequence = parsePageSequence((String)pageSequence);
            m_pageSequence = new HttpPageSequence(sequence);
            m_pageSequence.setParameters(m_parameterMap);
        }

        Map<String, Object> getParameterMap() {
            return m_parameterMap;
        }

        HttpPageSequence getPageSequence() {
            return m_pageSequence;
        }

        PageSequence parsePageSequence(String sequenceString) {
            return JaxbUtils.unmarshal(PageSequence.class, sequenceString);

        }

        public int getRetries() {
            return getKeyedInteger(m_parameterMap, "retry", DEFAULT_RETRY);
        }

        public int getTimeout() {
            return getKeyedInteger(m_parameterMap, "timeout", DEFAULT_TIMEOUT);
        }

        HttpClientWrapper createHttpClient() {
            HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                    .setConnectionTimeout(getTimeout())
                    .setSocketTimeout(getTimeout())
                    .setRetries(getRetries())
                    .useBrowserCompatibleCookies();
            return clientWrapper;
        }
    }

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameterMap) {
        PollStatus serviceStatus = PollStatus.unavailable("Poll not completed yet");

        final Map<String,Number> responseTimes = new LinkedHashMap<String,Number>();

        SequenceTracker tracker = new SequenceTracker(parameterMap, DEFAULT_SEQUENCE_RETRY, DEFAULT_TIMEOUT);
        for(tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt() ) {
            HttpClientWrapper clientWrapper = null;
            try {
                PageSequenceMonitorParameters parms = PageSequenceMonitorParameters.get(parameterMap);

                clientWrapper = parms.createHttpClient();

                // TODO: Is it normal for monitors to set 'response-time' to NaN
                // before the poll is executed?
                responseTimes.put(PollStatus.PROPERTY_RESPONSE_TIME, Double.NaN);

                tracker.startAttempt();
                parms.getPageSequence().execute(clientWrapper, svc, responseTimes);
                double responseTime = tracker.elapsedTimeInMillis();

                serviceStatus = PollStatus.available();
                // Update response time with the actual execution time
                responseTimes.put(PollStatus.PROPERTY_RESPONSE_TIME, responseTime);

            } catch (PageSequenceMonitorException e) {
                serviceStatus = PollStatus.unavailable(e.getMessage());
            } catch (IllegalArgumentException e) {
                LOG.error("Invalid parameters to monitor", e);
                serviceStatus = PollStatus.unavailable("Invalid parameter to monitor: " + e.getMessage() + ".  See log for details.");
            } catch (Throwable e) {
                LOG.error("Unexpected exception: " + e.getMessage(), e);
                serviceStatus = PollStatus.unavailable("Unexpected exception: " + e.getMessage());
            } finally {
                serviceStatus.setProperties(responseTimes);
                IOUtils.closeQuietly(clientWrapper);
            }
        }

        return serviceStatus;
    }
}
