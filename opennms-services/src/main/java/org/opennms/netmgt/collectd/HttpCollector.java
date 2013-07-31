/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIBuilder;
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
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.EmptyKeyRelaxedTrustSSLContext;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.collector.AttributeDefinition;
import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.config.httpdatacollection.Attrib;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.Parameter;
import org.opennms.netmgt.config.httpdatacollection.Uri;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collect data via URI
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class HttpCollector implements ServiceCollector {
    
    private static final Logger LOG = LoggerFactory.getLogger(HttpCollector.class);

    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final String DEFAULT_SO_TIMEOUT = "3000";

    private static final NumberFormat PARSER;

    private static NumberFormat RRD_FORMATTER;

    static {
        PARSER = NumberFormat.getNumberInstance();
        ((DecimalFormat)PARSER).setParseBigDecimal(true);

        RRD_FORMATTER = NumberFormat.getNumberInstance();
        RRD_FORMATTER.setMinimumFractionDigits(0);
        RRD_FORMATTER.setMaximumFractionDigits(Integer.MAX_VALUE);
        RRD_FORMATTER.setMinimumIntegerDigits(1);
        RRD_FORMATTER.setMaximumIntegerDigits(Integer.MAX_VALUE);
        RRD_FORMATTER.setGroupingUsed(false);

        // Make sure that the {@link EmptyKeyRelaxedTrustSSLContext} algorithm
        // is available to JSSE
        java.security.Security.addProvider(new EmptyKeyRelaxedTrustProvider());
    }

    /** {@inheritDoc} */
    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) {
        HttpCollectionSet collectionSet = new HttpCollectionSet(agent, parameters);
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.collect();
        return collectionSet;
    }

    protected class HttpCollectionSet implements CollectionSet {
        private CollectionAgent m_agent;
        private Map<String, Object> m_parameters;
        private Uri m_uriDef;
        private int m_status;
        private List<HttpCollectionResource> m_collectionResourceList;
		private Date m_timestamp;
        public Uri getUriDef() {
            return m_uriDef;
        }

		public void setUriDef(Uri uriDef) {
            m_uriDef = uriDef;
        }

        HttpCollectionSet(CollectionAgent agent, Map<String, Object> parameters) {
            m_agent = agent;
            m_parameters = parameters;
            m_status=ServiceCollector.COLLECTION_SUCCEEDED;
        }

        public void collect() {
            String collectionName=ParameterMap.getKeyedString(m_parameters, "collection", null);
            if(collectionName==null) {
                //Look for the old configuration style:
            	collectionName=ParameterMap.getKeyedString(m_parameters, "http-collection", null);
            }
            if (collectionName==null) {
                LOG.debug("no collection name found in parameters");
            	m_status=ServiceCollector.COLLECTION_FAILED;
            	return;
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
                    LOG.error("collect: http collection failed", e);

                    /*
                     * FIXME: This doesn't make sense since everything is SNMP
                     * collection-centric.  Should probably let the exception
                     * pass through.
                     */
                    m_status=ServiceCollector.COLLECTION_FAILED;
                }
            }
        }

        public CollectionAgent getAgent() {
            return m_agent;
        }

        public void setAgent(CollectionAgent agent) {
            m_agent = agent;
        }

        public Map<String, Object> getParameters() {
            return m_parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            m_parameters = parameters;
        }

        @Override
        public int getStatus() {
            return m_status;
        }

        public void storeResults(List<HttpCollectionAttribute> results, HttpCollectionResource collectionResource) {
            collectionResource.storeResults(results);
        }

        @Override
        public void visit(CollectionSetVisitor visitor) {
            visitor.visitCollectionSet(this);
            for (HttpCollectionResource collectionResource : m_collectionResourceList) {
                collectionResource.visit(visitor);
            }
            visitor.completeCollectionSet(this);
        }

        @Override
		public boolean ignorePersist() {
			return false;
		}       

		@Override
		public Date getCollectionTimestamp() {
			return m_timestamp;
		}
		public void setCollectionTimestamp(Date timestamp) {
			this.m_timestamp = timestamp;
		}

	public int getPort() { // This method has been created to deal with NMS-4886
	    int port = getUriDef().getUrl().getPort();
	    // Check for service assigned port if UriDef port is not supplied (i.e., is equal to the default port 80)
	    if (port == 80 && m_parameters.containsKey("port")) {
	        try {
	            port = Integer.parseInt(m_parameters.get("port").toString());
	            LOG.debug("getPort: using service provided HTTP port {}", port);
	        } catch (Exception e) {
	            LOG.warn("Malformed HTTP port on service definition.");
	        }
	    }
	    return port;
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

        DefaultHttpClient client = null;
        HttpUriRequest method = null;

        try {
            HttpParams params = buildParams(collectionSet);
            client = new DefaultHttpClient(params);
            if ("https".equals(collectionSet.getUriDef().getUrl().getScheme())) {
                final SchemeRegistry registry = client.getConnectionManager().getSchemeRegistry();
                final Scheme https = registry.getScheme("https");

                // Override the trust validation with a lenient implementation
                final SSLSocketFactory factory = new SSLSocketFactory(SSLContext.getInstance(EmptyKeyRelaxedTrustSSLContext.ALGORITHM), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                final Scheme lenient = new Scheme(https.getName(), https.getDefaultPort(), factory);
                // This will replace the existing "https" schema
                registry.register(lenient);
            }

            String key = "retry";
            if (collectionSet.getParameters().containsKey("retries")) {
                key = "retries";
            }
            Integer retryCount = ParameterMap.getKeyedInteger(collectionSet.getParameters(), key, DEFAULT_RETRY_COUNT);
            client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false));
            method = buildHttpMethod(collectionSet);
            method.getParams().setParameter(CoreProtocolPNames.USER_AGENT, determineUserAgent(collectionSet, params));

            buildCredentials(collectionSet, client, method);

            LOG.info("doCollection: collecting for client: {} using method: {}", client, method);
            HttpResponse response = client.execute(method);
            //Not really a persist as such; it just stores data in collectionSet for later retrieval
            persistResponse(collectionSet, collectionResource, client, response);
        } catch (URISyntaxException e) {
            throw new HttpCollectorException("Error building HttpClient URI", e);
        } catch (IOException e) {
            throw new HttpCollectorException("IO Error retrieving page", e);
        } catch (NoSuchAlgorithmException e) {
            throw new HttpCollectorException("Could not find EmptyKeyRelaxedTrustSSLContext to allow connection to untrusted HTTPS hosts", e);
        } catch (PatternSyntaxException e) {
            throw new HttpCollectorException("Invalid regex specified in HTTP collection configuration", e);
        } catch (Throwable e) {
            throw new HttpCollectorException("Unexpected exception caught during HTTP collection", e);
        } finally {
            if (client != null) {
                client.getConnectionManager().shutdown();
            }
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

        @Override
        public String getName() {
            return m_alias;
        }

        @Override
        public String getType() {
            return m_type;
        }

        public Object getValue() {
            return m_value;
        }

        @Override
        public String getNumericValue() {
            Object val = getValue();
            if (val instanceof Number) {
                return val.toString();
            } else {
                try {
                    return Double.valueOf(val.toString()).toString();
                } catch (NumberFormatException nfe) { /* Fall through */ }
            }
            LOG.debug("Value for attribute {} does not appear to be a number, skipping", this);
            return null;
        }

        @Override
        public String getStringValue() {
            return getValue().toString();
        }

        public String getValueAsString() {
            if (m_value instanceof Number) {
                return RRD_FORMATTER.format(m_value);
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
        @Override
        public CollectionAttributeType getAttributeType() {
            return m_attribType;
        }

        @Override
        public CollectionResource getResource() {
            return m_resource;
        }

        @Override
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

        @Override
        public String getMetricIdentifier() {
            return "Not_Supported_Yet_HTTP_"+getAttributeType().getName();
        }

    }

    private List<HttpCollectionAttribute> processResponse(final Locale responseLocale, final String responseBodyAsString, final HttpCollectionSet collectionSet, HttpCollectionResource collectionResource) {
        LOG.debug("processResponse:");
        LOG.debug("responseBody = {}", responseBodyAsString);
        LOG.debug("getmatches = {}", collectionSet.getUriDef().getUrl().getMatches());
        List<HttpCollectionAttribute> butes = new LinkedList<HttpCollectionAttribute>();
        int flags = 0;
        if (collectionSet.getUriDef().getUrl().getCanonicalEquivalence()) {
            flags |= Pattern.CANON_EQ;
        }
        if (collectionSet.getUriDef().getUrl().getCaseInsensitive()) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (collectionSet.getUriDef().getUrl().getComments()) {
            flags |= Pattern.COMMENTS;
        }
        if (collectionSet.getUriDef().getUrl().getDotall()) {
            flags |= Pattern.DOTALL;
        }
        if (collectionSet.getUriDef().getUrl().getLiteral()) {
            flags |= Pattern.LITERAL;
        }
        if (collectionSet.getUriDef().getUrl().getMultiline()) {
            flags |= Pattern.MULTILINE;
        }
        if (collectionSet.getUriDef().getUrl().getUnicodeCase()) {
            flags |= Pattern.UNICODE_CASE;
        }
        if (collectionSet.getUriDef().getUrl().getUnixLines()) {
            flags |= Pattern.UNIX_LINES;
        }
        LOG.debug("flags = {}", flags);
        Pattern p = Pattern.compile(collectionSet.getUriDef().getUrl().getMatches(), flags);
        Matcher m = p.matcher(responseBodyAsString);

        final boolean matches = m.matches();
        if (matches) {
            LOG.debug("processResponse: found matching attributes: {}", matches);
            final List<Attrib> attribDefs = collectionSet.getUriDef().getAttributes().getAttribCollection();
            final AttributeGroupType groupType = new AttributeGroupType(collectionSet.getUriDef().getName(),"all");

            final List<Locale> locales = new ArrayList<Locale>();
            if (responseLocale != null) {
                locales.add(responseLocale);
            }
            locales.add(Locale.getDefault());
            if (Locale.getDefault() != Locale.ENGLISH) {
                locales.add(Locale.ENGLISH);
            }

            for (final Attrib attribDef : attribDefs) {
                final String type = attribDef.getType();
                String value = null;
                try {
                    value = m.group(attribDef.getMatchGroup());
                } catch (final IndexOutOfBoundsException e) {
                    LOG.error("IndexOutOfBoundsException thrown while trying to find regex group, your regex does not contain the following group index: {}", attribDef.getMatchGroup());
                    LOG.error("Regex statement: {}", collectionSet.getUriDef().getUrl().getMatches());
                    continue;
                }

                if (! type.matches("^([Oo](ctet|CTET)[Ss](tring|TRING))|([Ss](tring|TRING))$")) {
                    Number num = null;
                    for (final Locale locale : locales) {
                        try {
                            num = NumberFormat.getNumberInstance(locale).parse(value);
                            LOG.debug("processResponse: found a parsable number with locale \"{}\".", locale);
                            break;
                        } catch (final ParseException e) {
                            LOG.error("attribute {} failed to match a parsable number with locale \"{}\"! Matched \"{}\" instead.", attribDef.getAlias(), locale, value);
                        }
                    }

                    if (num == null) {
                        LOG.error("processResponse: gave up attempting to parse numeric value, skipping group {}", attribDef.getMatchGroup());
                        continue;
                    }
                    
                    final HttpCollectionAttribute bute = new HttpCollectionAttribute(
                         collectionResource,
                         new HttpCollectionAttributeType(attribDef, groupType), 
                         attribDef.getAlias(),
                         type, 
                         num
                     );
                     LOG.debug("processResponse: adding found numeric attribute: {}", bute);
                     butes.add(bute);
                } else {
                    HttpCollectionAttribute bute =
                        new HttpCollectionAttribute(
                                                    collectionResource,
                                                    new HttpCollectionAttributeType(attribDef, groupType),
                                                    attribDef.getAlias(),
                                                    type,
                                                    value);
                    LOG.debug("processResponse: adding found string attribute: {}", bute);
                    butes.add(bute);
                }
            }
        } else {
            LOG.debug("processResponse: found matching attributes: {}", matches);
        }
        return butes;
    }

    public class HttpCollectorException extends RuntimeException {

        private static final long serialVersionUID = 4413332529546573490L;

        HttpCollectorException(String message) {
            super(message);
        }

        HttpCollectorException(String message, Throwable e) {
            super(message);
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(super.toString());
            buffer.append(": client URL: ");
            return buffer.toString();
        }
    }

    private void persistResponse(final HttpCollectionSet collectionSet, final HttpCollectionResource collectionResource, final HttpClient client, final HttpResponse response) throws IOException {
        final String responseString = EntityUtils.toString(response.getEntity());
        if (responseString != null && !"".equals(responseString)) {
            // Get response's locale from the Content-Language header if available
            Locale responseLocale = null;
            final Header[] headers = response.getHeaders("Content-Language");
            if (headers != null) {
                LOG.debug("doCollection: Trying to devise response's locale from Content-Language header.");
                if (headers.length == 1) {
                    if (headers[0].getValue().split(",").length == 1) {
                        final String[] values = headers[0].getValue().split("-");
                        LOG.debug("doCollection: Found one Content-Language header with value: {}", headers[0].getValue());
                        switch (values.length) {
                            case 1:
                                responseLocale = new Locale(values[0]);
                                break;
                            case 2:
                                responseLocale = new Locale(values[0], values[1]);
                                break;
                            default:
                                LOG.warn("doCollection: Ignoring Content-Language header with value {}. No support for more than 1 language subtag!", headers[0].getValue());
                        }
                    } else {
                        LOG.warn("doCollection: Multiple languages specified. That doesn't make sense. Ignoring...");
                    }
                } else {
                    LOG.warn("doCollection: More than 1 Content-Language headers received. Ignoring them!");
                }
            }

            List<HttpCollectionAttribute> attributes = processResponse(responseLocale, responseString, collectionSet, collectionResource);

            if (attributes.isEmpty()) {
                LOG.warn("doCollection: no attributes defined by the response: {}", responseString.trim());
                throw new HttpCollectorException("No attributes specified were found: ");
            }

            // put the results into the collectionset for later
            collectionSet.storeResults(attributes, collectionResource);
        }
    }

    private static void buildCredentials(final HttpCollectionSet collectionSet, final DefaultHttpClient client, final HttpUriRequest method) {
        if (collectionSet.getUriDef().getUrl().getUserInfo() != null) {
            String userInfo = collectionSet.getUriDef().getUrl().getUserInfo();
            String[] streetCred = userInfo.split(":", 2);
            if (streetCred.length == 2) {
                client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(streetCred[0], streetCred[1]));
            } else { 
                LOG.warn("Illegal value found for username/password HTTP credentials: {}", userInfo);
            }
        }
    }

    private static HttpParams buildParams(final HttpCollectionSet collectionSet) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, computeVersion(collectionSet.getUriDef()));
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.parseInt(ParameterMap.getKeyedString(collectionSet.getParameters(), "timeout", DEFAULT_SO_TIMEOUT)));
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.parseInt(ParameterMap.getKeyedString(collectionSet.getParameters(), "timeout", DEFAULT_SO_TIMEOUT)));
        params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        //review the httpclient code, looks like virtual host is checked for null
        //and if true, sets Host to the connection's host property
        String virtualHost = collectionSet.getUriDef().getUrl().getVirtualHost();
        if (virtualHost != null) {
            params.setParameter(
                                ClientPNames.VIRTUAL_HOST, 
                                new HttpHost(virtualHost, collectionSet.getPort())
            );
        }

        return params;
    }

    private static String determineUserAgent(final HttpCollectionSet collectionSet, final HttpParams params) {
        String userAgent = collectionSet.getUriDef().getUrl().getUserAgent();
        return (String) (userAgent == null ? params.getParameter(CoreProtocolPNames.USER_AGENT) : userAgent);
    }

    private static HttpVersion computeVersion(final Uri uri) {
        return new HttpVersion(Integer.parseInt(uri.getUrl().getHttpVersion().substring(0, 1)),
                               Integer.parseInt(uri.getUrl().getHttpVersion().substring(2)));
    }

    private static HttpUriRequest buildHttpMethod(final HttpCollectionSet collectionSet) throws URISyntaxException {
        HttpUriRequest method;
        URI uri = buildUri(collectionSet);
        if ("GET".equals(collectionSet.getUriDef().getUrl().getMethod())) {
            method = buildGetMethod(uri, collectionSet);
        } else {
            method = buildPostMethod(uri, collectionSet);
        }

        return method;
    }

    private static HttpPost buildPostMethod(final URI uri, final HttpCollectionSet collectionSet) {
        HttpPost method = new HttpPost(uri);
        List<NameValuePair> postParams = buildRequestParameters(collectionSet);
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams, "UTF-8");
            method.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            // Should never happen
        }
        return method;
    }

    private static HttpGet buildGetMethod(final URI uri, final HttpCollectionSet collectionSet) {
        URI uriWithQueryString = null;
        List<NameValuePair> queryParams = buildRequestParameters(collectionSet);
        try {
            StringBuffer query = new StringBuffer();
            query.append(URLEncodedUtils.format(queryParams, "UTF-8"));
            if (uri.getQuery() != null && uri.getQuery().length() > 0) {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(uri.getQuery());
            }
            URIBuilder ub = new URIBuilder(uri);
            if (query.length() > 0) {
                ub.setQuery(query.toString());
            }
            uriWithQueryString = ub.build();
            return new HttpGet(uriWithQueryString);
        } catch (URISyntaxException e) {
            LOG.warn(e.getMessage(), e);
            return new HttpGet(uri);
        }
    }

    private static List<NameValuePair> buildRequestParameters(final HttpCollectionSet collectionSet) {
        List<NameValuePair> retval = new ArrayList<NameValuePair>();
        if (collectionSet.getUriDef().getUrl().getParameters() == null) {
            return retval;
        }
        List<Parameter> parameters = collectionSet.getUriDef().getUrl().getParameters().getParameterCollection();
        for (Parameter p : parameters) {
            retval.add(new BasicNameValuePair(p.getKey(), p.getValue()));
        }
        return retval;
    }

    private static URI buildUri(final HttpCollectionSet collectionSet) throws URISyntaxException {
        HashMap<String,String> substitutions = new HashMap<String,String>();
        substitutions.put("ipaddr", InetAddressUtils.str(collectionSet.getAgent().getInetAddress()));
        substitutions.put("nodeid", Integer.toString(collectionSet.getAgent().getNodeId()));

        URIBuilder ub = new URIBuilder();
        ub.setScheme(collectionSet.getUriDef().getUrl().getScheme());
        ub.setHost(substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getHost(), "getHost"));
        ub.setPort(collectionSet.getPort());
        ub.setPath(substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getPath(), "getURL"));
        ub.setQuery(substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getQuery(), "getQuery"));
        ub.setFragment(substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getFragment(), "getFragment"));
        return ub.build();
    }

    private static String substituteKeywords(final HashMap<String,String> substitutions, final String urlFragment, final String desc) {
        String newFragment = urlFragment;
        if (newFragment != null)
        {
            for (String key : substitutions.keySet()) {
                newFragment = newFragment.replaceAll("\\$\\{" + key + "\\}", substitutions.get(key));
            }
            if (LOG.isDebugEnabled() && newFragment.compareTo(urlFragment) != 0) {
                LOG.debug("doSubs: {} substituted as \"{}\"", desc, newFragment);
            }
        }
        return newFragment;
    }


    /** {@inheritDoc} 
     * @throws CollectionInitializationException */
    @Override
    public void initialize(Map<String, String> parameters) throws CollectionInitializationException {

        LOG.debug("initialize: Initializing HttpCollector.");

        initHttpCollectionConfig();
        initDatabaseConnectionFactory();
        initializeRrdRepository();
    }

    private static void initHttpCollectionConfig() {
        try {
            LOG.debug("initialize: Initializing collector: {}", HttpCollector.class.getSimpleName());
            HttpCollectionConfigFactory.init();
        } catch (MarshalException e) {
            LOG.error("initialize: Error marshalling configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("initialize: Error validating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (FileNotFoundException e) {
            LOG.error("initialize: Error locating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            LOG.error("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private static void initializeRrdRepository() throws CollectionInitializationException {
        LOG.debug("initializeRrdRepository: Initializing RRD repo from HttpCollector...");
        initializeRrdDirs();
    }

    private static void initializeRrdDirs() throws CollectionInitializationException {
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
                LOG.error(sb.toString());
                throw new CollectionInitializationException(sb.toString());
            }
        }
    }

    private static void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (IOException e) {
            LOG.error("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            LOG.error("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            LOG.error("initDatabaseConnectionFactory: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) {
        LOG.debug("initialize: Initializing HTTP collection for agent: {}", agent);

        // Add any initialization here
    }

    /**
     * <p>release</p>
     */
    @Override
    public void release() {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
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
        @Override
        public boolean rescanNeeded() {
            return false;
        }

        @Override
        public boolean shouldPersist(ServiceParameters params) {
            return true;
        }

        @Override
        public String getOwnerName() {
            return m_agent.getHostAddress();
        }

        @Override
        public File getResourceDir(RrdRepository repository) {
            return new File(repository.getRrdBaseDir(), getParent());
        }

        @Override
        public void visit(CollectionSetVisitor visitor) {
            visitor.visitResource(this);
            m_attribGroup.visit(visitor);
            visitor.completeResource(this);
        }

        @Override
        public int getType() {
            return -1; //Is this right?
        }

        @Override
        public String getResourceTypeName() {
            return "node"; //All node resources for HTTP; nothing of interface or "indexed resource" type
        }

        @Override
        public String getInstance() {
            return null;
        }

        @Override
        public String getLabel() {
            return null;
        }

        @Override
        public String getParent() {
            return m_agent.getStorageDir().toString();
        }

        @Override
        public TimeKeeper getTimeKeeper() {
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

        @Override
        public AttributeGroupType getGroupType() {
            return m_groupType;
        }

        @Override
        public void storeAttribute(CollectionAttribute attribute, Persister persister) {
            if(m_attribute.getType().equals("string")) {
                persister.persistStringAttribute(attribute);
            } else {
                persister.persistNumericAttribute(attribute);
            }
        }

        @Override
        public String getName() {
            return m_attribute.getAlias();
        }

        @Override
        public String getType() {
            return m_attribute.getType();
        }

    }

    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return HttpCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

}
