/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ServiceParameters.ParameterName;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.httpdatacollection.Attrib;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.Parameter;
import org.opennms.netmgt.config.httpdatacollection.Uri;
import org.opennms.netmgt.config.httpdatacollection.Url;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collect data via URI
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class HttpCollector extends AbstractRemoteServiceCollector {

    private static final Logger LOG = LoggerFactory.getLogger(HttpCollector.class);

    private static final String HTTP_COLLECTION_KEY = "httpCollection";

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(HTTP_COLLECTION_KEY, HttpCollection.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final int DEFAULT_SO_TIMEOUT = 3000;

    private static final NumberFormat PARSER;

    static {
        PARSER = NumberFormat.getNumberInstance();
        ((DecimalFormat)PARSER).setParseBigDecimal(true);

        // Make sure that the {@link EmptyKeyRelaxedTrustSSLContext} algorithm
        // is available to JSSE
        java.security.Security.addProvider(new EmptyKeyRelaxedTrustProvider());
    }

    public HttpCollector() {
        super(TYPE_MAP);
    }

    /** {@inheritDoc}
     * @throws CollectionInitializationException */
    @Override
    public void initialize() throws CollectionInitializationException {
        LOG.debug("initialize: Initializing HttpCollector.");
        initHttpCollectionConfig();
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();
        final String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "http-collection", null));
        final HttpCollection collection = HttpCollectionConfigFactory.getInstance().getHttpCollection(collectionName);
        if (collection == null) {
            throw new IllegalArgumentException(String.format("HttpCollector: No collection found with name '%s'.",  collectionName));
        }
        runtimeAttributes.put(HTTP_COLLECTION_KEY, collection);
        return runtimeAttributes;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) {
        final HttpCollection collection = (HttpCollection)parameters.get(HTTP_COLLECTION_KEY);
        final CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(agent);
        final HttpCollectorAgent httpCollectorAgent = new HttpCollectorAgent(agent, parameters, collection, collectionSetBuilder);
        httpCollectorAgent.collect();
        return collectionSetBuilder.build();
    }

    private static class HttpCollectorAgent {
        private final CollectionAgent m_agent;
        private final Map<String, Object> m_parameters;
        private final HttpCollection m_httpCollection;
        private final CollectionSetBuilder m_collectionSetBuilder;
        private Uri m_uriDef;

        public Uri getUriDef() {
            return m_uriDef;
        }

        public HttpCollectorAgent(CollectionAgent agent, Map<String, Object> parameters, HttpCollection httpCollection, CollectionSetBuilder collectionSetBuilder) {
            m_agent = Objects.requireNonNull(agent);
            m_parameters = Objects.requireNonNull(parameters);
            m_httpCollection = Objects.requireNonNull(httpCollection);
            m_collectionSetBuilder = Objects.requireNonNull(collectionSetBuilder);
        }

        public void collect() {
            List<Uri> uriDefs = m_httpCollection.getUris();
            for (Uri uriDef : uriDefs) {
                m_uriDef = uriDef;
                try {
                    doCollection(this, m_collectionSetBuilder);
                } catch (HttpCollectorException e) {
                    LOG.warn("collect: http collection failed", e);
                    m_collectionSetBuilder.withStatus(CollectionStatus.FAILED);
                }
            }
        }

        public CollectionAgent getAgent() {
            return m_agent;
        }

        public Map<String, Object> getParameters() {
            return m_parameters;
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
     * @param collectorAgent
     * @throws HttpCollectorException
     */
    private static void doCollection(final HttpCollectorAgent collectorAgent, final CollectionSetBuilder collectionSetBuilder) throws HttpCollectorException {
        HttpRequestBase method = null;
        HttpClientWrapper clientWrapper = null;
        try {
            final HttpVersion httpVersion = computeVersion(collectorAgent.getUriDef());

            clientWrapper = HttpClientWrapper.create()
                    .setConnectionTimeout(ParameterMap.getKeyedInteger(collectorAgent.getParameters(), ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT))
                    .setSocketTimeout(ParameterMap.getKeyedInteger(collectorAgent.getParameters(), ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT))
                    .useBrowserCompatibleCookies();

            if ("https".equals(collectorAgent.getUriDef().getUrl().getScheme())) {
                clientWrapper.useRelaxedSSL("https");
            }

            String key = ParameterName.RETRY.toString();
            if (collectorAgent.getParameters().containsKey(ParameterName.RETRIES.toString())) {
                key = ParameterName.RETRIES.toString();
            }
            Integer retryCount = ParameterMap.getKeyedInteger(collectorAgent.getParameters(), key, DEFAULT_RETRY_COUNT);
            clientWrapper.setRetries(retryCount);

            method = buildHttpMethod(collectorAgent);
            method.setProtocolVersion(httpVersion);
            final String userAgent = determineUserAgent(collectorAgent);
            if (userAgent != null && !userAgent.trim().isEmpty()) {
                clientWrapper.setUserAgent(userAgent);
            }
            final HttpClientWrapper wrapper = clientWrapper;

            if (collectorAgent.getUriDef().getUrl().getUserInfo().isPresent()) {
                final String userInfo = collectorAgent.getUriDef().getUrl().getUserInfo().get();
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
            persistResponse(collectorAgent, collectionSetBuilder, response);
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

    private static void processResponse(final Locale responseLocale, final String responseBodyAsString, final HttpCollectorAgent collectorAgent, final CollectionSetBuilder collectionSetBuilder) {
        LOG.debug("processResponse:");
        LOG.debug("responseBody = {}", responseBodyAsString);
        LOG.debug("getmatches = {}", collectorAgent.getUriDef().getUrl().getMatches());
        int numberOfButes = 0;
        int flags = 0;
        if (collectorAgent.getUriDef().getUrl().isCanonicalEquivalence()) {
            flags |= Pattern.CANON_EQ;
        }
        if (collectorAgent.getUriDef().getUrl().isCaseInsensitive()) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (collectorAgent.getUriDef().getUrl().isComments()) {
            flags |= Pattern.COMMENTS;
        }
        if (collectorAgent.getUriDef().getUrl().isDotall()) {
            flags |= Pattern.DOTALL;
        }
        if (collectorAgent.getUriDef().getUrl().isLiteral()) {
            flags |= Pattern.LITERAL;
        }
        if (collectorAgent.getUriDef().getUrl().isMultiline()) {
            flags |= Pattern.MULTILINE;
        }
        if (collectorAgent.getUriDef().getUrl().isUnicodeCase()) {
            flags |= Pattern.UNICODE_CASE;
        }
        if (collectorAgent.getUriDef().getUrl().isUnixLines()) {
            flags |= Pattern.UNIX_LINES;
        }
        LOG.debug("flags = {}", flags);
        Pattern p = Pattern.compile(collectorAgent.getUriDef().getUrl().getMatches(), flags);
        Matcher m = p.matcher(responseBodyAsString);

        final boolean matches = m.matches();
        if (matches) {
            LOG.debug("processResponse: found matching attributes: {}", matches);
            final List<Attrib> attribDefs = collectorAgent.getUriDef().getAttributes();

            final List<Locale> locales = new ArrayList<>();
            if (responseLocale != null) {
                locales.add(responseLocale);
            }
            locales.add(Locale.getDefault());
            if (Locale.getDefault() != Locale.ENGLISH) {
                locales.add(Locale.ENGLISH);
            }

            // All node resources for HTTP; nothing of interface or "indexed resource" type
            final NodeLevelResource resource = new NodeLevelResource(collectorAgent.getAgent().getNodeId());
            for (final Attrib attribDef : attribDefs) {
                final AttributeType type = attribDef.getType();

                String value = null;
                try {
                    value = m.group(attribDef.getMatchGroup());
                } catch (final IndexOutOfBoundsException e) {
                    LOG.error("IndexOutOfBoundsException thrown while trying to find regex group, your regex does not contain the following group index: {}", attribDef.getMatchGroup());
                    LOG.error("Regex statement: {}", collectorAgent.getUriDef().getUrl().getMatches());
                    continue;
                }

                if (type.isNumeric()) {
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

                    LOG.debug("processResponse: adding numeric attribute {}", num);
                    collectionSetBuilder.withNumericAttribute(resource, collectorAgent.getUriDef().getName(), attribDef.getAlias(), num, type);
                    numberOfButes++;
                } else {
                    LOG.debug("processResponse: adding string attribute {}", value);
                    collectionSetBuilder.withStringAttribute(resource, collectorAgent.getUriDef().getName(), attribDef.getAlias(), value);
                    numberOfButes++;
                }
            }
        } else {
            LOG.debug("processResponse: found matching attributes: {}", matches);
        }

        if (numberOfButes < 1) {
            LOG.warn("doCollection: no attributes defined by the response: {}", responseBodyAsString.trim());
            throw new HttpCollectorException("No attributes specified were found.");
        }
    }

    public static class HttpCollectorException extends RuntimeException {

        private static final long serialVersionUID = 4413332529546573490L;

        HttpCollectorException(String message) {
            super(message);
        }

        HttpCollectorException(String message, Throwable e) {
            super(message, e);
        }

        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(super.toString());
            buffer.append(": client URL: ");
            return buffer.toString();
        }
    }

    private static void persistResponse(final HttpCollectorAgent collectorAgent, final CollectionSetBuilder collectionSetBuilder, final HttpResponse response) throws IOException {
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

            processResponse(responseLocale, responseString, collectorAgent, collectionSetBuilder);
        }
    }

    private static String determineUserAgent(final HttpCollectorAgent collectorAgent) {
        final Url url = collectorAgent.getUriDef().getUrl();
        if (url.getUserAgent().isPresent()) {
            return url.getUserAgent().get();
        }
        return null;
    }

    private static HttpVersion computeVersion(final Uri uri) {
        return new HttpVersion(Integer.parseInt(uri.getUrl().getHttpVersion().substring(0, 1)),
                               Integer.parseInt(uri.getUrl().getHttpVersion().substring(2)));
    }

    private static HttpRequestBase buildHttpMethod(final HttpCollectorAgent collectorAgent) throws URISyntaxException {
        HttpRequestBase method;
        final URI uri = buildUri(collectorAgent);
        final Url url = collectorAgent.getUriDef().getUrl();
        if ("GET".equals(url.getMethod())) {
            method = buildGetMethod(uri, collectorAgent);
        } else {
            method = buildPostMethod(uri, collectorAgent);
        }

        if (url.getVirtualHost().isPresent()) {
            final String virtualHost = url.getVirtualHost().get();
            if (!virtualHost.trim().isEmpty()) {
                method.setHeader(HTTP.TARGET_HOST, virtualHost);
            }
        }
        return method;
    }

    private static HttpPost buildPostMethod(final URI uri, final HttpCollectorAgent collectorAgent) {
        HttpPost method = new HttpPost(uri);
        List<NameValuePair> postParams = buildRequestParameters(collectorAgent);
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams, StandardCharsets.UTF_8);
        method.setEntity(entity);
        return method;
    }

    private static HttpGet buildGetMethod(final URI uri, final HttpCollectorAgent collectorAgent) {
        URI uriWithQueryString = null;
        List<NameValuePair> queryParams = buildRequestParameters(collectorAgent);
        try {
            final StringBuilder query = new StringBuilder();
            query.append(URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8));
            if (uri.getQuery() != null && !uri.getQuery().trim().isEmpty()) {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(uri.getQuery());
            }
            final URIBuilder ub = new URIBuilder(uri);
            if (query.length() > 0) {
                final List<NameValuePair> params = URLEncodedUtils.parse(query.toString(), StandardCharsets.UTF_8);
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

    private static List<NameValuePair> buildRequestParameters(final HttpCollectorAgent collectorAgent) {
        List<NameValuePair> retval = new ArrayList<>();
        if (collectorAgent.getUriDef().getUrl().getParameters() == null) {
            return retval;
        }
        for (final Parameter p : collectorAgent.getUriDef().getUrl().getParameters()) {
            retval.add(new BasicNameValuePair(p.getKey(), p.getValue()));
        }
        return retval;
    }

    private static URI buildUri(final HttpCollectorAgent collectorAgent) throws URISyntaxException {
        HashMap<String,String> substitutions = new HashMap<String,String>();
        substitutions.put("ipaddr", InetAddressUtils.str(collectorAgent.getAgent().getAddress()));
        substitutions.put("nodeid", Integer.toString(collectorAgent.getAgent().getNodeId()));

        final URIBuilder ub = new URIBuilder();
        ub.setScheme(collectorAgent.getUriDef().getUrl().getScheme());
        ub.setHost(substituteKeywords(substitutions, collectorAgent.getUriDef().getUrl().getHost(), "getHost"));
        ub.setPort(collectorAgent.getPort());
        ub.setPath(substituteKeywords(substitutions, collectorAgent.getUriDef().getUrl().getPath(), "getURL"));

        final String query = substituteKeywords(substitutions, collectorAgent.getUriDef().getUrl().getQuery().orElse(null), "getQuery");
        if (query != null) {
            final List<NameValuePair> params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
            ub.setParameters(params);
        }

        ub.setFragment(substituteKeywords(substitutions, collectorAgent.getUriDef().getUrl().getFragment().orElse(null), "getFragment"));
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


    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return HttpCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

    private static void initHttpCollectionConfig() {
        try {
            LOG.debug("initialize: Initializing collector: {}", HttpCollector.class.getSimpleName());
            HttpCollectionConfigFactory.init();
        } catch (FileNotFoundException e) {
            LOG.error("initialize: Error locating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            LOG.error("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }



}
