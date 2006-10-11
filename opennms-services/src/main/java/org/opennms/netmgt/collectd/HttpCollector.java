//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//

package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.datacollection.Attrib;
import org.opennms.netmgt.config.datacollection.HttpCollection;
import org.opennms.netmgt.config.datacollection.Uri;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * Collect data via URI
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class HttpCollector implements ServiceCollector {

    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final String DEFAULT_SO_TIMEOUT = "3000";

    @SuppressWarnings("unchecked")
    public int collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) {
        HttpCollection collection = HttpCollectionConfigFactory.getInstance().getHttpCollection(parameters.get("http-collection"));
        List<Uri> uris = collection.getUris().getUriCollection();
        for (Uri uri : uris) {
            try {
                doCollection((InetAddress)agent.getAddress(), uri, parameters);
            } catch (HttpCollectorException e) {
                Category log = log();
                log.error("collect: http collection problem: ", e);

                //this doesn't make sense since everything is SNMP collection centric
                //should probably let the exception pass through
                return ServiceCollector.COLLECTION_FAILED;
            }
        }
        return ServiceCollector.COLLECTION_SUCCEEDED;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * Performs HTTP collection.
     * 
     * Couple of notes to make the implementation of this client library
     * less obtuse:
     * 
     *   - HostConfiguration class is not created here because the library
     *     builds it when a URI is defined.
     *     
     * @param uriDef
     * @param parameters
     * @throws HttpException
     * @throws IOException
     */
    private void doCollection(InetAddress address, Uri uriDef, Map<String, String> parameters) throws HttpCollectorException {

        HttpClient client = null;
        HttpMethod method = null;
        
        try {
            client = new HttpClient(buildParams(uriDef, parameters));
            method = buildHttpMethod(address, uriDef);
            client.executeMethod(method);
            List<HttpCollectionAttribute> butes = processResponse(method.getResponseBodyAsString(), uriDef);
            
            if (butes.isEmpty()) {
                throw new HttpCollectorException("No attributes specified were found: ",client);
            }
        } catch (URIException e) {
            throw new HttpCollectorException("Error building HttpClient URI", client);
        } catch (HttpException e) {
            throw new HttpCollectorException("Error building HttpMethod", client);
        } catch (IOException e) {
            throw new HttpCollectorException("IO Error retrieving page", client);
        } finally {
            method.releaseConnection();
        }
        
    }
    
    class HttpCollectionAttribute {
        String m_alias;
        String m_type;
        String m_value;
        
        HttpCollectionAttribute(String alias, String type, String value) {
            m_alias = alias;
            m_type= type;
            m_value = value;
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<HttpCollectionAttribute> processResponse(String responseBodyAsString, Uri uriDef) {
        List<HttpCollectionAttribute> butes = new LinkedList<HttpCollectionAttribute>();
        Pattern p = Pattern.compile(uriDef.getUrl().getMatches());
        Matcher m = p.matcher(responseBodyAsString);
        
        if (m.matches()) {
            List<Attrib> attribs = uriDef.getAttributes().getAttribCollection();
            
            for (Attrib attribDef : attribs) {
                HttpCollectionAttribute bute = new HttpCollectionAttribute(attribDef.getAlias(),
                        attribDef.getType(), m.group(attribDef.getMatchGroup()));
                butes.add(bute);
            }
        }
        return butes;
    }

    public class HttpCollectorException extends RuntimeException {
        private static final long serialVersionUID = 7244720855059205687L;
        HttpClient m_client;
        HttpCollectorException(String message, HttpClient client){
            super(message);
            m_client = client;
        }
        
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(super.toString());
            buffer.append(": client URL: ");
            buffer.append(m_client.getHostConfiguration().getHostURL());
            return buffer.toString();
        }
    }

    private HttpClientParams buildParams(Uri uri, Map<String, String> parameters) {
        HttpClientParams params = new HttpClientParams(DefaultHttpParams.getDefaultParams());
        params.setVersion(computeVersion(uri));
        params.setSoTimeout(Integer.parseInt(ParameterMap.getKeyedString(parameters, "timeout", DEFAULT_SO_TIMEOUT)));
        
        //review the httpclient code, looks like virtual host is checked for null
        //and if true, sets Host to the connection's host property
        params.setVirtualHost(uri.getUrl().getVirtualHost());
        Integer retryCount = ParameterMap.getKeyedInteger(parameters, "retries", DEFAULT_RETRY_COUNT);
        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryCount, false));

        return params;
    }

    private HttpVersion computeVersion(Uri uri) {
        return new HttpVersion(Integer.parseInt(uri.getUrl().getHttpVersion().substring(0, 1)),
                Integer.parseInt(uri.getUrl().getHttpVersion().substring(2)));
    }

    private HttpMethod buildHttpMethod(InetAddress address, Uri uriDef) throws URIException {
        HttpMethod method;
        
        if ("GET".equals(uriDef.getUrl().getMethod())) {
            method = new GetMethod();
        } else {
            method = new PostMethod();
        }
        method.setURI(buildUri(address, uriDef));

        return method;
    }

    private URI buildUri(InetAddress address, Uri uri) throws URIException {
        //FIXME: need to convert to agent address if = "${ipaddr}"
        return new URI(uri.getUrl().getScheme(),
                uri.getUrl().getUserInfo(),
                determineHost(address, uri),
                uri.getUrl().getPort(),
                uri.getUrl().getPath(),
                uri.getUrl().getQuery(),
                uri.getUrl().getFragment());
    }
    
    //note: trouble deciding here on getHost() vs. getIpAddress() or 
    //getCanonicalHost() even.
    private String determineHost(InetAddress address, Uri uri) {
        if ("${ipaddr}".equals(uri.getUrl().getHost())) {
            return address.getHostName();
        } else {
            return uri.getUrl().getHost();
        }
    }

    public void initialize(Map parameters) {
        // TODO Auto-generated method stub
    }

    public void initialize(CollectionAgent agent, Map parameters) {
        // TODO Auto-generated method stub
    }

    public void release() {
        // TODO Auto-generated method stub
    }

    public void release(CollectionAgent agent) {
        // TODO Auto-generated method stub
    }

}
