/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.dns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Type;
import org.xbill.DNS.ZoneTransferException;
import org.xbill.DNS.ZoneTransferIn;

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

    private String m_zone;
    
    //TODO implement this
    private Long m_serial;
    
    //TODO implement this
    private Boolean m_fallback;
    
    //TODO implement this
    private TSIG m_key;

    private URL m_url;

    private int m_port;

    private String m_foreignSource;
    
    private int m_foreignIdHashSource;
    
    private String[] m_services;
    
    private static Map<String, String> m_args;
    
    
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
        
        m_url = url;
        m_port = url.getPort() == -1 ? 53 : url.getPort();
        m_zone = parseZone(url);
        m_foreignSource = parseForeignSource(url);
        m_foreignIdHashSource = getForeignIdHashSource();
        m_services = getServices();
        
        if (m_zone == null) {
            throw new IllegalArgumentException("Specified Zone is null");
        }
        
        m_serial = Long.valueOf(0L);
        m_fallback = Boolean.FALSE;

        m_key = null;
        
    }

    /**
     * Determine services to be provisioned from URL
     * 
     * @return a String[] of opennms service names
     */
    private String[] getServices() {
        // TODO validate services against service table of database
        String[] services = new String[] { "ICMP", "SNMP" };
        if (getArgs() != null && getArgs().get(SERVICES_ARG) != null) {
            services = getArgs().get(SERVICES_ARG).split(",");
        }
        return services;
    }

    /**
     * Determine source for computing hash for foreignId from URL
     * 
     * @return a String of "ipAddress" or "nodeLabel"
     */
    private int getForeignIdHashSource() {
        int result = 0;
        if (getArgs() != null && getArgs().get(FID_HASH_SRC_ARG) != null) {
            String hashSourceArg = getArgs().get(FID_HASH_SRC_ARG).toLowerCase();
            for (String keyword : HASH_IP_KEYWORDS) {
                if (hashSourceArg.contains(keyword)) {
                    result = 2;
                    break;
                }
            }
            for (String keyword : HASH_LABEL_KEYWORDS) {
                if (hashSourceArg.contains(keyword)) {
                    result++;
                    break;
                }
            }
        }
        return result;
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
        
        InputStream stream = null;
        
        try {
            Requisition r = buildRequisitionFromZoneTransfer();
            stream = new ByteArrayInputStream(jaxBMarshal(r).getBytes());
        } catch (IOException e) {
            LOG.warn("getInputStream: Problem getting input stream", e);
            throw e;
        } catch (Throwable e) {
            String message = "Problem getting input stream: "+e;
            LOG.warn(message, e);
            throw new IOExceptionWithCause(message,e );
        }
        
        return stream;
    }

    /**
     * Builds a Requisition based on the A records returned in a zone transfer from the
     * specified zone.
     * 
     * @return an instance of the JaxB annotated Requisition class than can be marshaled
     *   into the XML and streamed to the Provisioner
     *   
     * @throws IOException
     * @throws ZoneTransferException
     */
    private Requisition buildRequisitionFromZoneTransfer() throws IOException, ZoneTransferException {
        
	ZoneTransferIn xfer = null;
        List<Record> records = null;
        
        LOG.debug("connecting to host {}:{}", m_url.getHost(), m_port);
        try { 
            xfer = ZoneTransferIn.newIXFR(new Name(m_zone), 
                                        m_serial.longValue(), 
                                        m_fallback.booleanValue(), 
                                        m_url.getHost(), 
                                        m_port,
                                        m_key);
               records = getRecords(xfer);
       } catch (ZoneTransferException e) // Fallbacking to AXFR
       {
             String message = "IXFR not supported trying AXFR: "+e;
             LOG.warn(message, e);
             xfer = ZoneTransferIn.newAXFR(new Name(m_zone), m_url.getHost(), m_key);
             records = getRecords(xfer);
       }

  
        Requisition r = null;
        
        if (records.size() > 0) {
            
            //for now, set the foreign source to the specified dns zone
            r = new Requisition(getForeignSource());
            
            for (Record rec : records) {
                if (matchingRecord(rec)) {
                    r.insertNode(createRequisitionNode(rec));
                }
            }
        }
        
        return r;
    }


    @SuppressWarnings("unchecked")
    private List<Record> getRecords(ZoneTransferIn xfer) throws IOException, ZoneTransferException {
        return (List<Record>) xfer.run();
    }

    /**
     * Creates an instance of the JaxB annotated RequisionNode class.
     * 
     * @param rec
     * @return a populated RequisitionNode based on defaults and data from the
     *   A record returned from a DNS zone transfer query.
     */
    private RequisitionNode createRequisitionNode(Record rec) {
        String addr = null;
        if ("A".equals(Type.string(rec.getType()))) {
            ARecord arec = (ARecord)rec;
            addr = StringUtils.stripStart(arec.getAddress().toString(), "/");
        } else if ("AAAA".equals(Type.string(rec.getType()))) {
            AAAARecord aaaarec = (AAAARecord)rec;
            addr = aaaarec.rdataToString();
        } else {
            throw new IllegalArgumentException("Invalid record type " + Type.string(rec.getType()) + ". A or AAAA expected.");
        }

        RequisitionNode n = new RequisitionNode();
        
        String host = rec.getName().toString();
        String nodeLabel = StringUtils.stripEnd(StringUtils.stripStart(host, "."), ".");

        n.setBuilding(getForeignSource());
        
        switch(m_foreignIdHashSource) {
            case 1:
                n.setForeignId(computeHashCode(nodeLabel));
                LOG.debug("Generating foreignId from hash of nodelabel {}", nodeLabel);
                break;
            case 2:
                n.setForeignId(computeHashCode(addr));
                LOG.debug("Generating foreignId from hash of ipAddress {}", addr);
                break;
            case 3:
                n.setForeignId(computeHashCode(nodeLabel+addr));
                LOG.debug("Generating foreignId from hash of nodelabel+ipAddress {}{}", nodeLabel, addr);
                break;
            default:
                n.setForeignId(computeHashCode(nodeLabel));
                LOG.debug("Default case: Generating foreignId from hash of nodelabel {}", nodeLabel);
                break;
        }
        n.setNodeLabel(nodeLabel);
        
        RequisitionInterface i = new RequisitionInterface();
        i.setDescr("DNS-" + Type.string(rec.getType()));
        i.setIpAddr(addr);
        i.setSnmpPrimary(PrimaryType.PRIMARY);
        i.setManaged(Boolean.TRUE);
        i.setStatus(Integer.valueOf(1));
        
        for (String service : m_services) {
            service = service.trim();
            i.insertMonitoredService(new RequisitionMonitoredService(service));
            LOG.debug("Adding provisioned service {}", service);
            }
        
        n.putInterface(i);
        
        return n;
    }

    /**
     * Determines if the record is an A record and if the canonical name 
     * matches the expression supplied in the URL, if one was supplied.
     * 
     * @param rec
     * @return boolean if rec should be included in the import requisition
     */
    private boolean matchingRecord(Record rec) {
        
        LOG.info("matchingRecord: checking rec: {} to see if it should be imported...", rec);

        boolean matches = false;
        if ("A".equals(Type.string(rec.getType())) || "AAAA".equals(Type.string(rec.getType()))) {
            LOG.debug("matchingRecord: record is an {} record, continuing...", Type.string(rec.getType()));
            
            String expression = determineExpressionFromUrl(getUrl());
            
            if (expression != null) {

                Pattern p = Pattern.compile(expression);
                Matcher m = p.matcher(rec.getName().toString());

                // Try matching on host name only for backwards compatibility
                LOG.debug("matchingRecord: attempting to match hostname: [{}] with expression: [ {} ]", rec.getName(), expression);
                if (m.matches()) {
                    matches = true;
                } else {
                    // include the IP address and try again
                    LOG.debug("matchingRecord: attempting to match record: [{} {}] with expression: [{}]", rec.getName(), rec.rdataToString(), expression);
                    m = p.matcher(rec.getName().toString() + " " + rec.rdataToString());
                    if (m.matches()) {
                        matches = true;
                    }
                }
                
                LOG.debug("matchingRecord: record matches expression: {}", matches);
                
            } else {
                
                LOG.debug("matchingRecord: no expression for this zone, returning valid match for this {} record...", Type.string(rec.getType()));
                
                matches = true;
            }

        }
        
        LOG.info("matchingRecord: record: {} matches: {}", matches, rec);
        
        return matches;
    }
    

    /**
     * Created this in the case that we decide to every do something different with the hashing
     * to have a lesser likely hood of duplicate foreign ids
     * @param hashSource
     * @return
     */
    private String computeHashCode(String hashSource) {
        String hash = String.valueOf(hashSource.hashCode());
        return hash;
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
     * <p>getZone</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getZone() {
        return m_zone;
    }
    
    /**
     * <p>getSerial</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getSerial() {
        return m_serial;
    }
    
    /**
     * <p>setSerial</p>
     *
     * @param serial a {@link java.lang.Long} object.
     */
    public void setSerial(Long serial) {
        m_serial = serial;
    }
    
    /**
     * <p>getFallback</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getFallback() {
        return m_fallback;
    }
    
    /**
     * <p>setFallback</p>
     *
     * @param fallback a {@link java.lang.Boolean} object.
     */
    public void setFallback(Boolean fallback) {
        m_fallback = fallback;
    }
    
    /**
     * <p>getKey</p>
     *
     * @return a {@link org.xbill.DNS.TSIG} object.
     */
    public TSIG getKey() {
        return m_key;
    }
    
    /**
     * <p>setKey</p>
     *
     * @param key a {@link org.xbill.DNS.TSIG} object.
     */
    public void setKey(TSIG key) {
        m_key = key;
    }
    
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_url.toString();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getDescription();
    }
    
    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.net.URL} object.
     */
    public URL getUrl() {
        return m_url;
    }
    
    public static Map<String, String> getArgs() {
        return m_args;
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
            query = URLDecoder.decode(url.getQuery(), "UTF-8");
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
    protected static void validateDnsUrl(URL url) throws MalformedURLException {
        
        String path = url.getPath();
        path = StringUtils.removeStart(path, "/");
        path = StringUtils.removeEnd(path, "/");
        
        if (path == null || StringUtils.countMatches(path, "/") > 1) {
            throw new MalformedURLException("The specified DNS URL contains invalid path: "+url);
        }
        
        String query = url.getQuery();
        
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
    
    /**
     * <p>setForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     */
    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }


    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

}
