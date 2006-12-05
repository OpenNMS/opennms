//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 Blast Internet Services, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
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
//      http://www.blast.com/
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.pagesequence.Page;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the HTTP service on remote interfaces. The class implements the ServiceMonitor interface
 * that allows it to be used along with other plug-ins by the service poller framework.
 * 
 */
@Distributable
public class HttpMonitor2 extends IPv4Monitor {


    public static class HttpMonitorException extends RuntimeException {

        private static final long serialVersionUID = 1346757238604080088L;

        public HttpMonitorException(String message) {
            super(message);
        }

        public HttpMonitorException(Throwable cause) {
            super(cause);
        }

        public HttpMonitorException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RETRY = 0;
    
    public static class HttpPageSequence {
        PageSequence m_sequence;
        List<HttpPage> m_pages;
        
        HttpPageSequence(PageSequence sequence) {
            m_sequence = sequence;
            
            m_pages = new ArrayList<HttpPage>(m_sequence.getPageCount());
            for(Page page : m_sequence.getPage()) {
                m_pages.add(new HttpPage(this, page));
            }
        }
        
        List<HttpPage> getPages() {
            return m_pages;
        }

        private void execute(HttpClient client) {
            for (HttpPage page : getPages()) {
                page.execute(client);
            }
        }
    }
    
    public static class HttpResponseRange {
        static Pattern rangePattern = Pattern.compile("([1-5][0-9][0-9])(?:-([1-5][0-9][0-9]))?");
        int m_begin;
        int m_end;
        
        HttpResponseRange(String rangeSpec) {
            Matcher matcher = rangePattern.matcher(rangeSpec);
            matcher.matches();
            
            String beginSpec = matcher.group(1);
            String endSpec = matcher.group(2);

            if (beginSpec == null) {
                throw new IllegalArgumentException("Invalid range spec: "+rangeSpec);
            }
            
            m_begin = Integer.parseInt(beginSpec);
            
            if (endSpec == null) {
                m_end = m_begin;
            } else {
                m_end = Integer.parseInt(endSpec);
            }
            
        }
        
        public boolean isIn(int responseCode) {
            return (m_begin <= responseCode && responseCode <= m_end);
        }
        
        public String toString() {
            if (m_begin == m_end) {
                return Integer.toString(m_begin);
            } else {
                return Integer.toString(m_begin)+'-'+Integer.toString(m_end);
            }
        }
        
        
    }
    
    public static class HttpPage {
        HttpPageSequence m_parent;
        Page m_page;
        HttpResponseRange m_range;
        
        HttpPage(HttpPageSequence parent, Page page) {
            m_parent = parent;
            m_page = page;
            m_range = new HttpResponseRange(page.getResponseRange());
        }

        void execute(HttpClient client) {
            try {
                URI uri = getURI();
                HttpMethod method = getMethod();
                method.setURI(uri);
                
                int code = client.executeMethod(method);
                
                if (!m_range.isIn(code)) {
                    throw new HttpMonitorException("response code out of range. Expected "+m_range+" but received "+code);
                }
                

            } catch (URIException e) {
                throw new IllegalArgumentException("unable to construct URL for page: "+e, e);
            } catch (HttpException e) {
                throw new HttpMonitorException("HTTP Error "+e, e);
            } catch (IOException e) {
                throw new HttpMonitorException("I/O Error "+e, e);
            }
        }

        private URI getURI() throws URIException {
            return new URI(getScheme(), getUserInfo(), getHost(), getPort(), getPath(), getQuery(), getFragment());
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
            String host = m_page.getHost();
            // FIXME: handle ipAddr
            return host;
        }

        private String getUserInfo() {
            return m_page.getUserInfo();
        }

        private String getScheme() {
            return m_page.getScheme();
        }

        private HttpMethod getMethod() {
            String method = m_page.getMethod();
            return ("GET".equalsIgnoreCase(method) ? new GetMethod() : new PostMethod());
        }
    }

    public static class HttpMonitor2Parameters {
        
        public static final String KEY = HttpMonitor2Parameters.class.getName();
        
        static synchronized HttpMonitor2Parameters get(Map paramterMap) {
            HttpMonitor2Parameters parms = (HttpMonitor2Parameters)paramterMap.get(KEY);
            if (parms == null) {
                parms = new HttpMonitor2Parameters(paramterMap);
                paramterMap.put(KEY, parms);
            }
            return parms;
        }
        
        private Map m_parameterMap;
        private HttpPageSequence m_pageSequence;

        HttpMonitor2Parameters(Map parameterMap) {
            m_parameterMap = parameterMap;
            String pageSequence = getStringParm("page-sequence", null);
            if (pageSequence == null) {
                throw new IllegalArgumentException("page-sequence must be set in monitor parameters");
            }
            PageSequence sequence = parsePageSequence(pageSequence);
            m_pageSequence = new HttpPageSequence(sequence);
        }
        
        Map getParameterMap() {
            return m_parameterMap;
        }

        HttpPageSequence getPageSequence() {
            return m_pageSequence;
        }

        PageSequence parsePageSequence(String sequenceString) {
            try {
                return (PageSequence) Unmarshaller.unmarshal(PageSequence.class, new StringReader(sequenceString));
            } catch (MarshalException e) {
                throw new IllegalArgumentException("Unable to parse page-sequence for HttpMonitor: "+sequenceString, e);
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Unable to parse page-sequence for HttpMonitor: "+sequenceString, e);
            }
        
        }

        private String getStringParm(String key, String deflt) {
            return ParameterMap.getKeyedString(this.getParameterMap(), key, deflt);
        }
        
        private int getIntParm(String key, int defValue) {
            return ParameterMap.getKeyedInteger(getParameterMap()  , key, defValue);
        }

        HttpClientParams buildParams(MonitoredService svc) {
            HttpClientParams params = new HttpClientParams();
            params.setSoTimeout(getIntParm("timeout", HttpMonitor2.DEFAULT_TIMEOUT));
            int retryCount = getIntParm("retry", HttpMonitor2.DEFAULT_RETRY);
            params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryCount, false));
            params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            return params;
        }
    }
    

    public PollStatus poll(MonitoredService svc, Map parameterMap) {
        
        try {
            HttpMonitor2Parameters parms = HttpMonitor2Parameters.get(parameterMap);
            
            HttpClient client = new HttpClient(parms.buildParams(svc));

            long startTime = System.currentTimeMillis();
            
            parms.getPageSequence().execute(client);

            long endTime = System.currentTimeMillis();
            return PollStatus.available(endTime - startTime);

        } catch (HttpMonitorException e) {
            return PollStatus.unavailable(e.getMessage());
        } catch (IllegalArgumentException e) {
            log().error("Invalid parameters to monitor.", e);
            return PollStatus.unavailable("Invalid parameter to monitor: "+e.getMessage()+". See log for details.");
        }
    }

}

