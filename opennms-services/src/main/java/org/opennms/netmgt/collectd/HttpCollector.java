//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Jul 23: Actually use URL parameters (bug 3266) - jeffg@opennms.org
// 2008 Dec 25: Make HttpCollectionSet have many HttpCollectionResources
//              so that all resources get properly persisted when a collection
//              has many URIs, without re-breaking storeByGroup for this
//              collector (bug 2940)
// 2007 Aug 07: Move HTTP datacollection config package from
//              org.opennms.netmgt.config.datacollection to
//              org.opennms.netmgt.config.httpdatacollection. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
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
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.httpdatacollection.Attrib;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.Parameter;
import org.opennms.netmgt.config.httpdatacollection.Uri;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;

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
    
    private NumberFormat parser = null;
    
    private NumberFormat rrdFormatter =  null;
    
    
    public HttpCollector() {
        parser = NumberFormat.getNumberInstance();
        ((DecimalFormat)parser).setParseBigDecimal(true);

        rrdFormatter = NumberFormat.getNumberInstance();
        rrdFormatter.setMinimumFractionDigits(0);
        rrdFormatter.setMaximumFractionDigits(Integer.MAX_VALUE);
        rrdFormatter.setMinimumIntegerDigits(1);
        rrdFormatter.setMaximumIntegerDigits(Integer.MAX_VALUE);
        rrdFormatter.setGroupingUsed(false);
        
        
    }

    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) {
        HttpCollectionSet collectionSet = new HttpCollectionSet(agent, parameters);
        collectionSet.collect();
        return collectionSet;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    protected class HttpCollectionSet implements CollectionSet {
        private CollectionAgent m_agent;
        private Map<String, String> m_parameters;
        private Uri m_uriDef;
        private int m_status;
        private List<HttpCollectionResource> m_collectionResourceList;
        public Uri getUriDef() {
            return m_uriDef;
        }

        public void setUriDef(Uri uriDef) {
            m_uriDef = uriDef;
        }

        HttpCollectionSet(CollectionAgent agent, Map<String, String> parameters) {
            m_agent = agent;
            m_parameters = parameters;
            m_status=ServiceCollector.COLLECTION_FAILED;
        }
        
        public void collect() {
            String collectionName=m_parameters.get("collection");
            if(collectionName==null) {
                //Look for the old configuration style:
                 collectionName=m_parameters.get("http-collection");               
            }
            HttpCollection collection = HttpCollectionConfigFactory.getInstance().getHttpCollection(collectionName);
            m_collectionResourceList = new ArrayList<HttpCollectionResource>();
            List<Uri> uriDefs = collection.getUris().getUriCollection();
            for (Uri uriDef : uriDefs) {
                m_uriDef = uriDef;
                HttpCollectionResource collectionResource = new HttpCollectionResource(m_agent, uriDef);
                try {
                    doCollection(this, collectionResource);
                    m_collectionResourceList.add(collectionResource);
                } catch (HttpCollectorException e) {
                    log().error("collect: http collection failed: " + e, e);

                    /*
                     * FIXME: This doesn't make sense since everything is SNMP
                     * collection-centric.  Should probably let the exception
                     * pass through.
                     */
                    m_status=ServiceCollector.COLLECTION_FAILED;
                }
            }
            m_status=ServiceCollector.COLLECTION_SUCCEEDED;
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

        public int getStatus() {
            return m_status;
        }
        
        public void storeResults(List<HttpCollectionAttribute> results, HttpCollectionResource collectionResource) {
            collectionResource.storeResults(results);
        }

        public void visit(CollectionSetVisitor visitor) {
            visitor.visitCollectionSet(this);
            for (HttpCollectionResource collectionResource : m_collectionResourceList) {
                collectionResource.visit(visitor);
            }
            visitor.completeCollectionSet(this);
        }
        
		public boolean ignorePersist() {
			return false;
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
    private void doCollection(final HttpCollectionSet collectionSet, final HttpCollectionResource collectionResource) throws HttpCollectorException {

        HttpClient client = null;
        HttpMethod method = null;
        
        try {
            client = new HttpClient(buildParams(collectionSet));
            method = buildHttpMethod(collectionSet);
            
            buildCredentials(collectionSet, client, method);
            
            log().info("doCollection: collecting for client: "+client+" using method: "+method);
            client.executeMethod(method);
            //Not really a persist as such; it just stores data in collectionSet for later retrieval
            persistResponse(collectionSet, collectionResource, client, method);
        } catch (URIException e) {
            throw new HttpCollectorException("Error building HttpClient URI", client);
        } catch (HttpException e) {
            throw new HttpCollectorException("Error building HttpMethod", client);
        } catch (IOException e) {
            throw new HttpCollectorException("IO Error retrieving page", client);
        } finally {
            if (method != null) method.releaseConnection();
        }
    }

    class HttpCollectionAttribute extends AbstractCollectionAttribute implements AttributeDefinition {
        String m_alias;
        String m_type;
        Object m_value;
        HttpCollectionResource m_resource;
        HttpCollectionAttributeType m_attribType;
        
        HttpCollectionAttribute(HttpCollectionResource resource, HttpCollectionAttributeType attribType, String alias, String type, Number value) {
            super();
            m_resource=resource;
            m_attribType=attribType;
            m_alias = alias;
            m_type= type;
            m_value = value;
        }

        HttpCollectionAttribute(HttpCollectionResource resource, HttpCollectionAttributeType attribType, String alias, String type, String value) { 
            super();
            m_resource=resource;
            m_attribType=attribType;
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
        
        public Object getValue() {
            return m_value;
        }
        
        public String getNumericValue() {
            Object val = getValue();
            if (val instanceof Number) {
                return val.toString();
            } else {
                try {
                    return Double.valueOf(val.toString()).toString();
                } catch (NumberFormatException nfe) { /* Fall through */ }
            }
            if (log().isDebugEnabled()) {
                log().debug("Value for attribute " + this + " does not appear to be a number, skipping");
            }
            return null;
        }
             
        public String getStringValue() {
            return getValue().toString();
        }
        
        public String getValueAsString() {
            if (m_value instanceof Number) {
                return rrdFormatter.format(m_value);
            } else {
                return m_value.toString();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HttpCollectionAttribute) {
                HttpCollectionAttribute other = (HttpCollectionAttribute)obj;
                return getName().equals(other.getName());
            }
            return false;
        }
        public CollectionAttributeType getAttributeType() {
            return m_attribType;
        }
        
        public CollectionResource getResource() {
            return m_resource;
        }
        
        public boolean shouldPersist(ServiceParameters params) {
            return true;
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
            buffer.append(getValueAsString());
            return buffer.toString();
        }
        
    }
    
    private List<HttpCollectionAttribute> processResponse(final String responseBodyAsString, final HttpCollectionSet collectionSet, HttpCollectionResource collectionResource) {
        log().debug("processResponse:");
        log().debug("responseBody = " + responseBodyAsString);
        log().debug("getmatches = " + collectionSet.getUriDef().getUrl().getMatches());
        List<HttpCollectionAttribute> butes = new LinkedList<HttpCollectionAttribute>();
        int flags = 0;
        if (collectionSet.getUriDef().getUrl().getCanonicalEquivalence())
            flags |= Pattern.CANON_EQ;
        if (collectionSet.getUriDef().getUrl().getCaseInsensitive())
            flags |= Pattern.CASE_INSENSITIVE;
        if (collectionSet.getUriDef().getUrl().getComments())
            flags |= Pattern.COMMENTS;
        if (collectionSet.getUriDef().getUrl().getDotall())
            flags |= Pattern.DOTALL;
        if (collectionSet.getUriDef().getUrl().getLiteral())
            flags |= Pattern.LITERAL;
        if (collectionSet.getUriDef().getUrl().getMultiline())
            flags |= Pattern.MULTILINE;
        if (collectionSet.getUriDef().getUrl().getUnicodeCase())
            flags |= Pattern.UNICODE_CASE;
        if (collectionSet.getUriDef().getUrl().getUnixLines())
            flags |= Pattern.UNIX_LINES;
        log().debug("flags = " + flags);
        Pattern p = Pattern.compile(collectionSet.getUriDef().getUrl().getMatches(), flags);
        Matcher m = p.matcher(responseBodyAsString);
        
        final boolean matches = m.matches();
        if (matches) {
            log().debug("processResponse: found matching attributes: "+matches);
            List<Attrib> attribDefs = collectionSet.getUriDef().getAttributes().getAttribCollection();
            AttributeGroupType groupType = new AttributeGroupType(collectionSet.getUriDef().getName(),"all");
            
            for (Attrib attribDef : attribDefs) {
                if (! attribDef.getType().matches("^([Oo](ctet|CTET)[Ss](tring|TRING))|([Ss](tring|TRING))$")) {
                    try {
                        Number num = NumberFormat.getNumberInstance().parse(m.group(attribDef.getMatchGroup()));
                        HttpCollectionAttribute bute = 
                            new HttpCollectionAttribute(
                                                        collectionResource,
                                                        new HttpCollectionAttributeType(attribDef, groupType), 
                                                        attribDef.getAlias(),
                                                        attribDef.getType(), 
                                                        num);
                        log().debug("processResponse: adding found numeric attribute: "+bute);
                        butes.add(bute);
                    } catch (IndexOutOfBoundsException e) {
                        log().error("IndexOutOfBoundsException thrown while trying to find regex group, your regex does not contain the following group index: " + attribDef.getMatchGroup());
                        log().error("Regex statement: " + collectionSet.getUriDef().getUrl().getMatches());
                    } catch (ParseException e) {
                        log().error("attribute "+attribDef.getAlias()+" failed to match a parsable number! Matched \""+m.group(attribDef.getMatchGroup())+"\" instead.");
                    }
                } else {
                    HttpCollectionAttribute bute =
                        new HttpCollectionAttribute(
                                                    collectionResource,
                                                    new HttpCollectionAttributeType(attribDef, groupType),
                                                    attribDef.getAlias(),
                                                    attribDef.getType(),
                                                    m.group(attribDef.getMatchGroup()));
                    log().debug("processResponse: adding found string attribute: " + bute);
                    butes.add(bute);
                }
            }
        } else {
            log().debug("processResponse: found matching attributes: "+matches);
        }
        return butes;
    }

    public class HttpCollectorException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        HttpClient m_client;
        
        HttpCollectorException(String message, HttpClient client) {
            super(message);
            m_client = client;
        }
        
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(super.toString());
            buffer.append(": client URL: ");
            final HostConfiguration hostConfiguration = m_client.getHostConfiguration();
            
            if (hostConfiguration != null) {
                buffer.append(hostConfiguration.getHostURL() == null ? "null" : hostConfiguration.getHostURL());
            }
            
            return buffer.toString();
        }
    }

    private void persistResponse(final HttpCollectionSet collectionSet, HttpCollectionResource collectionResource, final HttpClient client, final HttpMethod method) throws IOException {
        List<HttpCollectionAttribute> butes = processResponse(method.getResponseBodyAsString(), collectionSet, collectionResource);
        
        if (butes.isEmpty()) {
            String url = client.getHostConfiguration() == null ? "null" : client.getHostConfiguration().getHostURL();
            log().warn("doCollection: no attributes defined for the response from: "+url+" were found that match the expression: '" + collectionSet.getUriDef().getUrl().getMatches() + "'");
            throw new HttpCollectorException("No attributes specified were found: ", client);
        }
        
        //put the results into the collectionset for later
        collectionSet.storeResults(butes, collectionResource);
    }

    private void buildCredentials(final HttpCollectionSet collectionSet, final HttpClient client, final HttpMethod method) {
        if (collectionSet.getUriDef().getUrl().getUserInfo() != null) {
            String userInfo = collectionSet.getUriDef().getUrl().getUserInfo();
            String[] streetCred = userInfo.split(":", 2);
            if (streetCred.length == 2) {
                client.getState().setCredentials(new AuthScope(AuthScope.ANY), new UsernamePasswordCredentials(streetCred[0], streetCred[1]));
                method.setDoAuthentication(true);
            }
        }
    }
    
    private HttpClientParams buildParams(final HttpCollectionSet collectionSet) {
        HttpClientParams params = new HttpClientParams(DefaultHttpParams.getDefaultParams());
        params.setVersion(computeVersion(collectionSet.getUriDef()));
        params.setSoTimeout(Integer.parseInt(ParameterMap.getKeyedString(collectionSet.getParameters(), "timeout", DEFAULT_SO_TIMEOUT)));
        
        //review the httpclient code, looks like virtual host is checked for null
        //and if true, sets Host to the connection's host property
        params.setVirtualHost(collectionSet.getUriDef().getUrl().getVirtualHost());
        
        String key = "retry";
        if (collectionSet.getParameters().containsKey("retries")) {
            key = "retries";
        }
        Integer retryCount = ParameterMap.getKeyedInteger(collectionSet.getParameters(), key, DEFAULT_RETRY_COUNT);
        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryCount, false));
        
        params.setParameter(HttpMethodParams.USER_AGENT, determineUserAgent(collectionSet, params));


        return params;
    }

    private String determineUserAgent(final HttpCollectionSet collectionSet, final HttpClientParams params) {
        String userAgent = collectionSet.getUriDef().getUrl().getUserAgent();
        return (String) (userAgent == null ? params.getParameter(HttpMethodParams.USER_AGENT) : userAgent);
    }

    private HttpVersion computeVersion(final Uri uri) {
        return new HttpVersion(Integer.parseInt(uri.getUrl().getHttpVersion().substring(0, 1)),
                Integer.parseInt(uri.getUrl().getHttpVersion().substring(2)));
    }

    private HttpMethod buildHttpMethod(final HttpCollectionSet collectionSet) throws URIException {
        HttpMethod method;
        if ("GET".equals(collectionSet.getUriDef().getUrl().getMethod())) {
            method = buildGetMethod(collectionSet);
        } else {
            method = buildPostMethod(collectionSet);
        }
        method.setURI(buildUri(collectionSet));

        return method;
    }
    
    private PostMethod buildPostMethod(final HttpCollectionSet collectionSet) {
        PostMethod method = new PostMethod();
        NameValuePair[] postParams = buildRequestParameters(collectionSet);
        if (postParams.length > 0) {
            method.setRequestBody(postParams);
        }
        return method;
    }
    
    private GetMethod buildGetMethod(final HttpCollectionSet collectionSet) {
        GetMethod method = new GetMethod();
        NameValuePair[] queryParams = buildRequestParameters(collectionSet);
        if (queryParams.length > 0) {
            method.setQueryString(queryParams);
        }
        return method;
    }
    
    private NameValuePair[] buildRequestParameters(final HttpCollectionSet collectionSet) {
        NameValuePair[] nvpArray = {};
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (collectionSet.getUriDef().getUrl().getParameters() == null)
            return nvpArray;
        List<Parameter> parameters = collectionSet.getUriDef().getUrl().getParameters().getParameterCollection();
        if (parameters.size() > 0) {
            nvps = new ArrayList<NameValuePair>();
            for (Parameter p : parameters) {
                nvps.add(new NameValuePair(p.getKey(), p.getValue()));
            }
        }
        return nvps.toArray(nvpArray);
    }

    private URI buildUri(final HttpCollectionSet collectionSet) throws URIException {
        HashMap<String,String> substitutions = new HashMap<String,String>();
        substitutions.put("ipaddr", collectionSet.getAgent().getInetAddress().getHostAddress());
        substitutions.put("nodeid", Integer.toString(collectionSet.getAgent().getNodeId()));
        
        return new URI(collectionSet.getUriDef().getUrl().getScheme(),
                collectionSet.getUriDef().getUrl().getUserInfo(),
                substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getHost(), "getHost"),
                collectionSet.getUriDef().getUrl().getPort(),
                substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getPath(), "getURL"),
                substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getQuery(), "getQuery"),
                substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getFragment(), "getFragment"));
    }
    
    private String substituteKeywords(final HashMap<String,String> substitutions, final String urlFragment, final String desc) {
        String newFragment = urlFragment;
        if (newFragment != null)
        {
            for (String key : substitutions.keySet()) {
                newFragment = newFragment.replaceAll("\\$\\{" + key + "\\}", substitutions.get(key));
            }
            if (log().isDebugEnabled() && newFragment.compareTo(urlFragment) != 0) {
                log().debug("doSubs: "+desc+" substituted as \""+newFragment+"\"");
            }
        }
        return newFragment;
    }

    
    public void initialize(Map<String, String> parameters) {
        
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
    
    public void initialize(CollectionAgent agent, Map<String, String> parameters) {
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

    private String determineServiceName(final Map<String, String> parameters) {
        return ParameterMap.getKeyedString(parameters, "service-name", "HTTP");
    }

    public void release() {
        // TODO Auto-generated method stub
    }

    public void release(CollectionAgent agent) {
        // TODO Auto-generated method stub
    }

    
    class HttpCollectionResource implements CollectionResource {

        CollectionAgent m_agent;
        AttributeGroup m_attribGroup;
        
        HttpCollectionResource(CollectionAgent agent, Uri uriDef) {
            m_agent=agent;
            m_attribGroup=new AttributeGroup(this, new AttributeGroupType(uriDef.getName(), "all"));
        }
        
        public void storeResults(List<HttpCollectionAttribute> results) {
            for(HttpCollectionAttribute attrib: results) {
                m_attribGroup.addAttribute(attrib);
            }
        }
        
        //A rescan is never needed for the HttpCollector
        public boolean rescanNeeded() {
            return false;
        }

        public boolean shouldPersist(ServiceParameters params) {
            return true;
        }

        public String getOwnerName() {
            return m_agent.getHostAddress();
        }

        public File getResourceDir(RrdRepository repository) {
            return new File(repository.getRrdBaseDir(), Integer.toString(m_agent.getNodeId()));
        }
        
        public void visit(CollectionSetVisitor visitor) {
            visitor.visitResource(this);
            m_attribGroup.visit(visitor);
            visitor.completeResource(this);
        }
        
        public int getType() {
            return -1; //Is this right?
        }
        
        public String getResourceTypeName() {
            return "node"; //All node resources for HTTP; nothing of interface or "indexed resource" type
        }

        public String getInstance() {
            return null;
        }

        public String getLabel() {
            return null;
        }
    }
    
    class HttpCollectionAttributeType implements CollectionAttributeType {
        Attrib m_attribute;
        AttributeGroupType m_groupType;

        protected HttpCollectionAttributeType(Attrib attribute, AttributeGroupType groupType) {
            m_groupType=groupType;
            m_attribute=attribute;
        }

        public AttributeGroupType getGroupType() {
            return m_groupType;
        }

        public void storeAttribute(CollectionAttribute attribute, Persister persister) {
            if(m_attribute.getType().equals("string")) {
                persister.persistStringAttribute(attribute);
            } else {
                persister.persistNumericAttribute(attribute);
            }
        }

        public String getName() {
            return m_attribute.getAlias();
        }

        public String getType() {
            return m_attribute.getType();
        }
        
    }
    
    public RrdRepository getRrdRepository(String collectionName) {
        return HttpCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

}
