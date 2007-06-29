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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
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
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.datacollection.Attrib;
import org.opennms.netmgt.config.datacollection.HttpCollection;
import org.opennms.netmgt.config.datacollection.Uri;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
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
    
    //Don't make this static because each service will have its own
    //copy and the key won't require the service name as  part of the key.
    private final HashMap<Integer, String> m_scheduledNodes = new HashMap<Integer, String>();

    @SuppressWarnings("unchecked")
    public int collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) {
        HttpCollectionSet collectionSet = new HttpCollectionSet(agent, parameters);
        return collectionSet.collect();
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    protected class HttpCollectionSet {
        private CollectionAgent m_agent;
        private Map<String, String> m_parameters;
        private Uri m_uriDef;
        
        public Uri getUriDef() {
            return m_uriDef;
        }

        public void setUriDef(Uri uriDef) {
            m_uriDef = uriDef;
        }

        HttpCollectionSet(CollectionAgent agent, Map<String, String> parameters) {
            m_agent = agent;
            m_parameters = parameters;
        }
        
        @SuppressWarnings("unchecked")
        public int collect() {
            HttpCollection collection = HttpCollectionConfigFactory.getInstance().getHttpCollection(m_parameters.get("http-collection"));
            List<Uri> uriDefs = collection.getUris().getUriCollection();
            for (Uri uriDef : uriDefs) {
                m_uriDef = uriDef;
                try {
                    doCollection(this);
                } catch (HttpCollectorException e) {
                    log().error("collect: http collection failed: " + e, e);

                    /*
                     * FIXME: This doesn't make sense since everything is SNMP
                     * collection-centric.  Should probably let the exception
                     * pass through.
                     */
                    return ServiceCollector.COLLECTION_FAILED;
                }
            }
            return ServiceCollector.COLLECTION_SUCCEEDED;

        }

        public CollectionAgent getAgent() {
            return m_agent;
        }

        public void setAgent(CollectionAgent agent) {
            m_agent = agent;
        }

        public Map<String, String> getParameters() {
            return m_parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            m_parameters = parameters;
        }
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
     * @param collectionSet
     * @throws HttpCollectorException
     */
    private void doCollection(final HttpCollectionSet collectionSet) throws HttpCollectorException {

        HttpClient client = null;
        HttpMethod method = null;
        
        try {
            client = new HttpClient(buildParams(collectionSet));
            method = buildHttpMethod(collectionSet);
            log().info("doCollection: collecting for client: "+client+" using method: "+method);
            client.executeMethod(method);
            List<HttpCollectionAttribute> butes = processResponse(method.getResponseBodyAsString(), collectionSet);
            
            if (butes.isEmpty()) {
                log().warn("doCollection: no attributes defined for collection were found in response text matching regular expression '" + collectionSet.getUriDef().getUrl().getMatches() + "'");
                throw new HttpCollectorException("No attributes specified were found: ", client);
            }
            String collectionName = collectionSet.getParameters().get("http-collection");
            RrdRepository rrdRepository = HttpCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
            ResourceIdentifier resource = new ResourceIdentifier() {

                public String getOwnerName() {
                    return collectionSet.getAgent().getHostAddress();
                }

                public File getResourceDir(RrdRepository repository) {
                    return new File(repository.getRrdBaseDir(), Integer.toString(collectionSet.getAgent().getNodeId()));
                }
                
            };
            
            log().info("doCollection: persisting "+butes.size()+" attributes");
            
            for (HttpCollectionAttribute attribute : butes) {
                PersistOperationBuilder builder = new PersistOperationBuilder(rrdRepository, resource, attribute.getName());
                builder.declareAttribute(attribute);
                log().debug("doCollection: setting attribute: "+attribute);
                builder.setAttributeValue(attribute, attribute.getValue());
                builder.commit();
            }
        } catch (URIException e) {
            throw new HttpCollectorException("Error building HttpClient URI", client);
        } catch (HttpException e) {
            throw new HttpCollectorException("Error building HttpMethod", client);
        } catch (IOException e) {
            throw new HttpCollectorException("IO Error retrieving page", client);
        } catch (RrdException e) {
            throw new HttpCollectorException("Error writing RRD", client);
        } finally {
            if (method != null) method.releaseConnection();
        }
    }
    
    class HttpCollectionAttribute implements AttributeDefinition {
        String m_alias;
        String m_type;
        String m_value;
        
        HttpCollectionAttribute(String alias, String type, String value) {
            m_alias = alias;
            m_type= type;
            m_value = value;
        }

        public String getName() {
            return m_alias;
        }

        public String getType() {
            return m_type;
        }
        
        public String getValue() {
            return m_value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HttpCollectionAttribute) {
                HttpCollectionAttribute other = (HttpCollectionAttribute)obj;
                return getName().equals(other.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("HttpAttribute: ");
            buffer.append(getName());
            buffer.append(":");
            buffer.append(getType());
            buffer.append(":");
            buffer.append(getValue());
            return buffer.toString();
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private List<HttpCollectionAttribute> processResponse(String responseBodyAsString, HttpCollectionSet collectionSet) {
        log().debug("processResponse: ");
        List<HttpCollectionAttribute> butes = new LinkedList<HttpCollectionAttribute>();
        Pattern p = Pattern.compile(collectionSet.getUriDef().getUrl().getMatches());
        Matcher m = p.matcher(responseBodyAsString);
        
        final boolean matches = m.matches();
        if (matches) {
            log().debug("processResponse: found matching attributes: "+matches);
            List<Attrib> attribDefs = collectionSet.getUriDef().getAttributes().getAttribCollection();
            
            for (Attrib attribDef : attribDefs) {
                HttpCollectionAttribute bute = new HttpCollectionAttribute(attribDef.getAlias(),
                        attribDef.getType(), m.group(attribDef.getMatchGroup()));
                log().debug("processResponse: adding found attribute: "+bute);
                butes.add(bute);
            }
        } else {
            log().debug("processResponse: found matching attributes: "+matches);
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
            final HostConfiguration hostConfiguration = m_client.getHostConfiguration();
            buffer.append((hostConfiguration == null ? "null" : hostConfiguration.toString()));
            return buffer.toString();
        }
    }

    private HttpClientParams buildParams(HttpCollectionSet collectionSet) {
        HttpClientParams params = new HttpClientParams(DefaultHttpParams.getDefaultParams());
        params.setVersion(computeVersion(collectionSet.getUriDef()));
        params.setSoTimeout(Integer.parseInt(ParameterMap.getKeyedString(collectionSet.getParameters(), "timeout", DEFAULT_SO_TIMEOUT)));
        
        //review the httpclient code, looks like virtual host is checked for null
        //and if true, sets Host to the connection's host property
        params.setVirtualHost(collectionSet.getUriDef().getUrl().getVirtualHost());
        Integer retryCount = ParameterMap.getKeyedInteger(collectionSet.getParameters(), "retries", DEFAULT_RETRY_COUNT);
        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryCount, false));

        return params;
    }

    private HttpVersion computeVersion(Uri uri) {
        return new HttpVersion(Integer.parseInt(uri.getUrl().getHttpVersion().substring(0, 1)),
                Integer.parseInt(uri.getUrl().getHttpVersion().substring(2)));
    }

    private HttpMethod buildHttpMethod(HttpCollectionSet collectionSet) throws URIException {
        HttpMethod method;
        if ("GET".equals(collectionSet.getUriDef().getUrl().getMethod())) {
            method = new GetMethod();
        } else {
            method = new PostMethod();
        }
        method.setURI(buildUri(collectionSet));

        return method;
    }

    private URI buildUri(HttpCollectionSet collectionSet) throws URIException {
        return new URI(collectionSet.getUriDef().getUrl().getScheme(),
                collectionSet.getUriDef().getUrl().getUserInfo(),
                determineHost(collectionSet.getAgent().getInetAddress(), collectionSet.getUriDef()),
                collectionSet.getUriDef().getUrl().getPort(),
                collectionSet.getUriDef().getUrl().getPath(),
                collectionSet.getUriDef().getUrl().getQuery(),
                collectionSet.getUriDef().getUrl().getFragment());
    }
    
    //note: trouble deciding here on getHost() vs. getIpAddress() or 
    //getCanonicalHost() even.
    private String determineHost(InetAddress address, Uri uriDef) {
        String host;
        if ("${ipaddr}".equals(uriDef.getUrl().getHost())) {
            host = address.getHostName();
        } else {
            host = uriDef.getUrl().getHost();
        }
        log().debug("determineHost: host for URI is set to: "+host);
        return host;
    }

    public void initialize(Map parameters) {
        log().debug("initialize: Initializing HttpCollector.");
        m_scheduledNodes.clear();
        initHttpCollecionConfig();
        initDatabaseConnectionFactory();
        initializeRrdRepository();
    }

    private void initHttpCollecionConfig() {
        try {
            log().debug("initialize: Initializing collector: "+getClass());
            HttpCollectionConfigFactory.init();
        } catch (MarshalException e) {
            log().fatal("initialize: Error marshalling configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initialize: Error validating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (FileNotFoundException e) {
            log().fatal("initialize: Error locating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log().fatal("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initializeRrdRepository() {
        log().debug("initializeRrdRepository: Initializing RRD repo from HttpCollector...");
        initializeRrdDirs();
        initializeRrdInterface();
    }

    private void initializeRrdDirs() {
        /*
         * If the RRD file repository directory does NOT already exist, create
         * it.
         */
        StringBuffer sb;
        File f = new File(HttpCollectionConfigFactory.getInstance().getRrdPath());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                sb = new StringBuffer();
                sb.append("initializeRrdDirs: Unable to create RRD file repository.  Path doesn't already exist and could not make directory: ");
                sb.append(HttpCollectionConfigFactory.getInstance().getRrdPath());
                log().error(sb.toString());
                throw new RuntimeException(sb.toString());
            }
        }
    }

    private void initializeRrdInterface() {
        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            log().error("initializeRrdInterface: Unable to initialize RrdUtils", e);
            throw new RuntimeException("Unable to initialize RrdUtils", e);
        }
    }

    private void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (IOException e) {
            log().fatal("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            log().fatal("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            log().fatal("initDatabaseConnectionFactory: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        }
    }
    
    public void initialize(CollectionAgent agent, Map parameters) {
        log().debug("initialize: Initializing HTTP collection for agent: "+agent);
        final Integer scheduledNodeKey = new Integer(agent.getNodeId());
        final String scheduledAddress = m_scheduledNodes.get(scheduledNodeKey);
        
        if (scheduledAddress != null) {
            log().info("initialize: Not scheduling interface for collection: "+scheduledAddress);
            final StringBuffer sb = new StringBuffer();
            sb.append("initialize service: ");
            
            //If they include this parameter, use it for debug logging.
            sb.append(determineServiceName(parameters));
            
            sb.append(" for address: ");
            sb.append(scheduledAddress);
            sb.append(" already scheduled for collection on node: ");
            sb.append(agent);
            log().debug(sb.toString());
            throw new IllegalStateException(sb.toString());
        } else {
            log().info("initialize: Scheduling interface for collection: "+scheduledAddress);
            m_scheduledNodes.put(scheduledNodeKey, scheduledAddress);
        }
    }

    private String determineServiceName(Map parameters) {
        return ParameterMap.getKeyedString(parameters, "service-name", "HTTP");
    }

    public void release() {
        // TODO Auto-generated method stub
    }

    public void release(CollectionAgent agent) {
        // TODO Auto-generated method stub
    }

}
