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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.ServiceParameters.ParameterName;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.collection.support.AbstractCollectionSet;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.httpdatacollection.Attrib;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.Parameter;
import org.opennms.netmgt.config.httpdatacollection.Uri;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.rrd.RrdRepository;
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
    private static final int DEFAULT_SO_TIMEOUT = 3000;

    private static final NumberFormat PARSER;

    private static final NumberFormat RRD_FORMATTER;

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

    private static class HttpCollectionSet extends AbstractCollectionSet {
        private final CollectionAgent m_agent;
        private final Map<String, Object> m_parameters;
        private Uri m_uriDef;
        private int m_status;
        private List<HttpCollectionResource> m_collectionResourceList;
        private Date m_timestamp;

        public Uri getUriDef() {
            return m_uriDef;
        }

        public HttpCollectionSet(CollectionAgent agent, Map<String, Object> parameters) {
            m_agent = agent;
            m_parameters = parameters;
            m_status=ServiceCollector.COLLECTION_SUCCEEDED;
        }

        public void collect() {
            String collectionName=ParameterMap.getKeyedString(m_parameters, ParameterName.COLLECTION.toString(), null);
            if(collectionName==null) {
                //Look for the old configuration style:
                collectionName=ParameterMap.getKeyedString(m_parameters, ParameterName.HTTP_COLLECTION.toString(), null);
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
                    LOG.warn("collect: http collection failed", e);

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

        public Map<String, Object> getParameters() {
            return m_parameters;
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
        public Date getCollectionTimestamp() {
            return m_timestamp;
        }
        public void setCollectionTimestamp(Date timestamp) {
            this.m_timestamp = timestamp;
        }

        public int getPort() { // This method has been created to deal with NMS-4886
            int port = getUriDef().getUrl().getPort();
            // Check for service assigned port if UriDef port is not supplied (i.e., is equal to the default port 80)
            if (port == 80 && m_parameters.containsKey(ParameterName.PORT.toString())) {
                try {
                    port = Integer.parseInt(m_parameters.get(ParameterName.PORT.toString()).toString());
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
    private static void doCollection(final HttpCollectionSet collectionSet, final HttpCollectionResource collectionResource) throws HttpCollectorException {
        HttpRequestBase method = null;
        HttpClientWrapper clientWrapper = null;
        try {
            final HttpVersion httpVersion = computeVersion(collectionSet.getUriDef());

            clientWrapper = HttpClientWrapper.create()
                    .setConnectionTimeout(ParameterMap.getKeyedInteger(collectionSet.getParameters(), ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT))
                    .setSocketTimeout(ParameterMap.getKeyedInteger(collectionSet.getParameters(), ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT))
                    .useBrowserCompatibleCookies();

            if ("https".equals(collectionSet.getUriDef().getUrl().getScheme())) {
                clientWrapper.useRelaxedSSL("https");
            }

            String key = ParameterName.RETRY.toString();
            if (collectionSet.getParameters().containsKey(ParameterName.RETRIES.toString())) {
                key = ParameterName.RETRIES.toString();
            }
            Integer retryCount = ParameterMap.getKeyedInteger(collectionSet.getParameters(), key, DEFAULT_RETRY_COUNT);
            clientWrapper.setRetries(retryCount);

            method = buildHttpMethod(collectionSet);
            method.setProtocolVersion(httpVersion);
            final String userAgent = determineUserAgent(collectionSet);
            if (userAgent != null && !userAgent.trim().isEmpty()) {
                clientWrapper.setUserAgent(userAgent);
            }
            final HttpClientWrapper wrapper = clientWrapper;

            if (collectionSet.getUriDef().getUrl().getUserInfo() != null) {
                final String userInfo = collectionSet.getUriDef().getUrl().getUserInfo();
                final String[] streetCred = userInfo.split(":", 2);
                if (streetCred.length == 2) {
                    wrapper.addBasicCredentials(streetCred[0], streetCred[1]);
                } else { 
                    LOG.warn("Illegal value found for username/password HTTP credentials: {}", userInfo);
                }
            }

            LOG.info("doCollection: collecting using method: {}", method);
            final CloseableHttpResponse response = clientWrapper.execute(method);
            //Not really a persist as such; it just stores data in collectionSet for later retrieval
            persistResponse(collectionSet, collectionResource, response);
        } catch (URISyntaxException e) {
            throw new HttpCollectorException("Error building HttpClient URI", e);
        } catch (IOException e) {
            throw new HttpCollectorException("IO Error retrieving page", e);
        } catch (PatternSyntaxException e) {
            throw new HttpCollectorException("Invalid regex specified in HTTP collection configuration", e);
        } catch (Throwable e) {
            throw new HttpCollectorException("Unexpected exception caught during HTTP collection", e);
        } finally {
            IOUtils.closeQuietly(clientWrapper);
        }
    }

    private static class HttpCollectionAttribute extends AbstractCollectionAttribute {
        private final Object m_value;

        public HttpCollectionAttribute(HttpCollectionResource resource, HttpCollectionAttributeType attribType, Number value) {
            super(attribType, resource);
            m_value = value;
        }

        public HttpCollectionAttribute(HttpCollectionResource resource, HttpCollectionAttributeType attribType, String value) { 
            super(attribType, resource);
            m_value = value;
        }

        @Override
        public String getNumericValue() {
            if (m_value instanceof Number) {
                return m_value.toString();
            } else {
                try {
                    return Double.valueOf(m_value.toString()).toString();
                } catch (NumberFormatException nfe) { /* Fall through */ }
            }
            LOG.debug("Value for attribute {} does not appear to be a number, skipping", this);
            return null;
        }

        @Override
        public String getStringValue() {
            return m_value.toString();
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

    private static List<HttpCollectionAttribute> processResponse(final Locale responseLocale, final String responseBodyAsString, final HttpCollectionSet collectionSet, HttpCollectionResource collectionResource) {
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
            final AttributeGroupType groupType = new AttributeGroupType(collectionSet.getUriDef().getName(), AttributeGroupType.IF_TYPE_ALL);

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
                            LOG.warn("attribute {} failed to match a parsable number with locale \"{}\"! Matched \"{}\" instead.", attribDef.getAlias(), locale, value);
                        }
                    }

                    if (num == null) {
                        LOG.warn("processResponse: gave up attempting to parse numeric value, skipping group {}", attribDef.getMatchGroup());
                        continue;
                    }

                    final HttpCollectionAttribute bute = new HttpCollectionAttribute(
                                                                                     collectionResource,
                                                                                     new HttpCollectionAttributeType(attribDef, groupType), 
                                                                                     num
                            );
                    LOG.debug("processResponse: adding found numeric attribute: {}", bute);
                    butes.add(bute);
                } else {
                    HttpCollectionAttribute bute =
                            new HttpCollectionAttribute(
                                                        collectionResource,
                                                        new HttpCollectionAttributeType(attribDef, groupType),
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

    public static class HttpCollectorException extends RuntimeException {

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

    private static void persistResponse(final HttpCollectionSet collectionSet, final HttpCollectionResource collectionResource, final HttpResponse response) throws IOException {
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

    private static String determineUserAgent(final HttpCollectionSet collectionSet) {
        String userAgent = collectionSet.getUriDef().getUrl().getUserAgent();
        return (String) (userAgent == null ? null : userAgent);
    }

    private static HttpVersion computeVersion(final Uri uri) {
        return new HttpVersion(Integer.parseInt(uri.getUrl().getHttpVersion().substring(0, 1)),
                               Integer.parseInt(uri.getUrl().getHttpVersion().substring(2)));
    }

    private static HttpRequestBase buildHttpMethod(final HttpCollectionSet collectionSet) throws URISyntaxException {
        HttpRequestBase method;
        URI uri = buildUri(collectionSet);
        if ("GET".equals(collectionSet.getUriDef().getUrl().getMethod())) {
            method = buildGetMethod(uri, collectionSet);
        } else {
            method = buildPostMethod(uri, collectionSet);
        }

        final String virtualHost = collectionSet.getUriDef().getUrl().getVirtualHost();
        if (virtualHost != null && !virtualHost.trim().isEmpty()) {
            method.setHeader(HTTP.TARGET_HOST, virtualHost);
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
            if (uri.getQuery() != null && !uri.getQuery().trim().isEmpty()) {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(uri.getQuery());
            }
            final URIBuilder ub = new URIBuilder(uri);
            if (query.length() > 0) {
                final List<NameValuePair> params = URLEncodedUtils.parse(query.toString(), Charset.forName("UTF-8"));
                if (!params.isEmpty()) {
                    ub.setParameters(params);
                }
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
        substitutions.put("ipaddr", InetAddressUtils.str(collectionSet.getAgent().getAddress()));
        substitutions.put("nodeid", Integer.toString(collectionSet.getAgent().getNodeId()));

        final URIBuilder ub = new URIBuilder();
        ub.setScheme(collectionSet.getUriDef().getUrl().getScheme());
        ub.setHost(substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getHost(), "getHost"));
        ub.setPort(collectionSet.getPort());
        ub.setPath(substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getPath(), "getURL"));

        final String query = substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getQuery(), "getQuery");
        final List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
        ub.setParameters(params);

        ub.setFragment(substituteKeywords(substitutions, collectionSet.getUriDef().getUrl().getFragment(), "getFragment"));
        return ub.build();
    }

    private static String substituteKeywords(final Map<String,String> substitutions, final String urlFragment, final String desc) {
        String newFragment = urlFragment;
        if (newFragment != null)
        {
            for (final Entry<String,String> entry : substitutions.entrySet()) {
                final String key = entry.getKey();
                newFragment = newFragment.replaceAll("\\$\\{" + key + "\\}", entry.getValue());
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


    private static class HttpCollectionResource implements CollectionResource {

        private final CollectionAgent m_agent;
        private final AttributeGroup m_attribGroup;

        public HttpCollectionResource(CollectionAgent agent, Uri uriDef) {
            m_agent=agent;
            m_attribGroup=new AttributeGroup(this, new AttributeGroupType(uriDef.getName(), AttributeGroupType.IF_TYPE_ALL));
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
        public String getResourceTypeName() {
            return CollectionResource.RESOURCE_TYPE_NODE; //All node resources for HTTP; nothing of interface or "indexed resource" type
        }

        @Override
        public String getInstance() {
            return null;
        }

        @Override
        public String getInterfaceLabel() {
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

    private static class HttpCollectionAttributeType extends AbstractCollectionAttributeType {
        private final Attrib m_attribute;

        public HttpCollectionAttributeType(Attrib attribute, AttributeGroupType groupType) {
            super(groupType);
            m_attribute=attribute;
        }

        @Override
        public void storeAttribute(CollectionAttribute attribute, Persister persister) {
            if("string".equalsIgnoreCase(m_attribute.getType())) {
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
