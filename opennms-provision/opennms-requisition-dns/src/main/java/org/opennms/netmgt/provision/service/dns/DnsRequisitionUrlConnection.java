/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.dns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of <code>java.net.URLConnection</code> for handling
 * URLs specified in the Provisiond configuration requesting an import
 * requisition based on the A records of a zone transfer for a DNS server.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class DnsRequisitionUrlConnection extends URLConnection {
    private static final Logger LOG = LoggerFactory.getLogger(DnsRequisitionUrlConnection.class);

    private static final String EXPRESSION_ARG = "expression";
    
    private static final String SERVICES_ARG = "services";
    
    private static final String FID_HASH_SRC_ARG = "foreignidhashsource";
    
    private static final String[] HASH_IP_KEYWORDS = { "ip", "addr" };
    
    private static final String[] HASH_LABEL_KEYWORDS = { "name", "label" };

    private static final String QUERY_ARG_SEPARATOR = "&";

    /** Constant <code>URL_SCHEME="dns://"</code> */
    public static final String URL_SCHEME = "dns://";

    /** Constant <code>PROTOCOL="dns"</code> */
    public static final String PROTOCOL = "dns";

    private static final DnsRequisitionProvider s_provider = new DnsRequisitionProvider();
    private final DnsRequisitionRequest m_request;
    private final Map<String, String> m_args;

    /**
     * <p>Constructor for DnsRequisitionUrlConnection.</p>
     *
     * @param url a {@link java.net.URL} object.
     * @throws java.net.MalformedURLException if any.
     */
    public DnsRequisitionUrlConnection(URL url) throws MalformedURLException {
        super(url);
        m_args = getUrlArgs(url);
        validateDnsUrl(url);

        m_request = new DnsRequisitionRequest();
        m_request.setHost(url.getHost());
        m_request.setPort(url.getPort() == -1 ? 53 : url.getPort());
        m_request.setZone(parseZone(url));
        m_request.setForeignSource(parseForeignSource(url));
        m_request.setExpression(determineExpressionFromUrl(url));
        m_request.setForeignIdHashSource(getForeignIdHashSource());
        m_request.setServices(Arrays.asList(getServices()));

        if (m_request.getZone() == null) {
            throw new IllegalArgumentException("Specified Zone is null");
        }
    }

    private Map<String, String> getArgs() {
        return m_args;
    }

    public DnsRequisitionRequest getRequest() {
        return m_request;
    }

    /**
     * Determine services to be provisioned from URL
     * 
     * @return a String[] of opennms service names
     */
    private String[] getServices() {
        // TODO validate services against service table of database
        if (getArgs() != null && getArgs().get(SERVICES_ARG) != null) {
            return getArgs().get(SERVICES_ARG).split(",");
        }
        return new String[] { "ICMP", "SNMP" };
    }

    /**
     * Determine source for computing hash for foreignId from URL
     * 
     * @return a String of "ipAddress" or "nodeLabel"
     */
    private ForeignIdHashSource getForeignIdHashSource() {
        boolean hasIpKeywords = false;
        boolean hasLabelKeywords = false;
        if (getArgs() != null && getArgs().get(FID_HASH_SRC_ARG) != null) {
            String hashSourceArg = getArgs().get(FID_HASH_SRC_ARG).toLowerCase();
            for (String keyword : HASH_IP_KEYWORDS) {
                if (hashSourceArg.contains(keyword)) {
                    hasIpKeywords = true;
                    break;
                }
            }
            for (String keyword : HASH_LABEL_KEYWORDS) {
                if (hashSourceArg.contains(keyword)) {
                    hasLabelKeywords = true;
                    break;
                }
            }
        }

        if (hasIpKeywords && !hasLabelKeywords) {
            return ForeignIdHashSource.IP_ADDRESS;
        } else if (hasIpKeywords && hasLabelKeywords) {
            return ForeignIdHashSource.NODE_LABEL_AND_IP_ADDRESS;
        } else {
            return ForeignIdHashSource.NODE_LABEL;
        }
    }

    
    /**
     * {@inheritDoc}
     *
     * This is a no op.
     */
    @Override
    public void connect() throws IOException {
    }

    /**
     * {@inheritDoc}
     *
     * Creates a ByteArrayInputStream implementation of InputStream of the XML marshaled version
     * of the Requisition class.  Calling close on this stream is safe.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            final Requisition r = s_provider.getRequisition(m_request);
            return new ByteArrayInputStream(jaxBMarshal(r).getBytes());
        } catch (Exception e) {
            String message = "Problem getting input stream: "+e;
            LOG.warn(message, e);
            throw new IOExceptionWithCause(message,e );
        }
    }

    /**
     * Utility to marshal the Requisition class into XML.
     * 
     * @param r
     * @return a String of XML encoding the Requisition class
     * 
     * @throws JAXBException
     */
    private String jaxBMarshal(Requisition r) throws JAXBException {
    	return JaxbUtils.marshal(r);
    }

    /**
     * <p>determineExpressionFromUrl</p>
     *
     * @param url a {@link java.net.URL} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String determineExpressionFromUrl(URL url) {
        LOG.info("determineExpressionFromUrl: finding regex as parameter in query string of URL: {}", url);
        if(getUrlArgs(url) == null) {
            return null;
        } else {
            return getUrlArgs(url).get(EXPRESSION_ARG);
        }
    }

    private static List<String> tokenizeQueryArgs(String query) throws IllegalArgumentException {
        
        if (query == null) {
            throw new IllegalArgumentException("The URL query is null");
        }

        List<String> queryArgs = Arrays.asList(StringUtils.split(query, QUERY_ARG_SEPARATOR));

        return queryArgs;
    }

    /**
     * <p>decodeQueryString</p>
     *
     * @param url a {@link java.net.URL} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String decodeQueryString(URL url) {
        if (url == null || url.getQuery() == null) {
            throw new IllegalArgumentException("The URL or the URL query is null: "+url);
        }
        
        String query = null;
        try {
            query = URLDecoder.decode(url.getQuery(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOG.error("decodeQueryString", e);
        }
        
        return query;
    }

    /**
     * Validate the format is:
     *   dns://<host>/<zone>/?expression=<regex>
     *
     *   there should be only one arguement in the path
     *   there should only be one query parameter
     *
     * @param url a {@link java.net.URL} object.
     * @throws java.net.MalformedURLException if any.
     */
    protected void validateDnsUrl(URL url) throws MalformedURLException {
        String path = url.getPath();
        path = StringUtils.removeStart(path, "/");
        path = StringUtils.removeEnd(path, "/");
        if (path == null || StringUtils.countMatches(path, "/") > 1) {
            throw new MalformedURLException("The specified DNS URL contains invalid path: "+url);
        }

        final String query = url.getQuery();
        if ((query != null) && (determineExpressionFromUrl(url) == null) && (getArgs().get(SERVICES_ARG) == null) && (getArgs().get(FID_HASH_SRC_ARG) == null)) {
            throw new MalformedURLException("The specified DNS URL contains an invalid query string: "+url);
        }
    }

    
    /**
     * Zone should be the first path entity
     *
     *   dns://<host>/<zone>[/<foreign source>][/<?expression=<regex>>
     *
     * @param url a {@link java.net.URL} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String parseZone(URL url) {
        String path = url.getPath();
        path = StringUtils.removeStart(path, "/");
        path = StringUtils.removeEnd(path, "/");

        String zone = path;
        if (path != null && StringUtils.countMatches(path, "/") == 1) {
            String[] paths = path.split("/");
            zone = paths[0];
        }

        return zone;
    }

    /**
     * Foreign Source should be the second path entity, if it exists, otherwise it is
     * set to the value of the zone.
     *
     *   dns://<host>/<zone>[/<foreign source>][/<?expression=<regex>>
     *
     * @param url a {@link java.net.URL} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String parseForeignSource(URL url) {
        String path = url.getPath();
        path = StringUtils.removeStart(path, "/");
        path = StringUtils.removeEnd(path, "/");

        String foreignSource = path;
        if (path != null && StringUtils.countMatches(path, "/") == 1) {
            String[] paths = path.split("/");
            foreignSource = paths[1];
        }

        return foreignSource;
    }

    protected static Map<String, String> getUrlArgs(URL url) {
        if (url.getQuery() == null) {
            return null;
        }

        //TODO: need to throw exception if query is null
        String query = decodeQueryString(url);

        //TODO: need to handle exception
        List<String> queryArgs = tokenizeQueryArgs(query);
        Map<String, String> args = new HashMap<String, String>();
        for (String queryArg : queryArgs) {
            String[] argTokens = StringUtils.split(queryArg, '='); 

            if (argTokens.length < 2) {
                LOG.warn("getUrlArgs: syntax error in URL query string, missing '=' in query argument: {}", queryArg);
            } else {
                LOG.debug("adding arg tokens {}, {}", argTokens[1], argTokens[0].toLowerCase());
                args.put(argTokens[0].toLowerCase(), argTokens[1]);
            }
        }

        return args;
    }  

}
