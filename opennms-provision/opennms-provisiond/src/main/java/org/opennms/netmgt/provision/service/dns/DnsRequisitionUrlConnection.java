/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 10, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service.dns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.ProvisionPrefixContextResolver;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.xbill.DNS.ARecord;
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

    private static final String EXPRESSION_ARG = "expression";

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
    
    
    /**
     * <p>Constructor for DnsRequisitionUrlConnection.</p>
     *
     * @param url a {@link java.net.URL} object.
     * @throws java.net.MalformedURLException if any.
     */
    protected DnsRequisitionUrlConnection(URL url) throws MalformedURLException {
        super(url);
        
        validateDnsUrl(url);
        
        m_url = url;
        m_port = url.getPort() == -1 ? 53 : url.getPort();
        m_zone = parseZone(url);
        m_foreignSource = parseForeignSource(url);
        
        if (m_zone == null) {
            throw new IllegalArgumentException("Specified Zone is null");
        }
        
        m_serial = Long.valueOf(0L);
        m_fallback = Boolean.valueOf(false);

        m_key = null;
        
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
            log().warn("getInputStream: Problem getting input stream: "+e, e);
            throw e;
        } catch (Exception e) {
            String message = "Problem getting input stream: "+e;
            log().warn(message, e);
            throw new IOExceptionWithCause(message,e );
        }
        
        return stream;
    }

    /**
     * Builds a Requisition based on the A records returned in a zone transfer from the
     * specified zone.
     * 
     * @return an instance of the JaxB annotated Requistion class than can be marshaled
     *   into the XML and streamed to the Provisioner
     *   
     * @throws IOException
     * @throws ZoneTransferException
     */
    private Requisition buildRequisitionFromZoneTransfer() throws IOException, ZoneTransferException {
        
	ZoneTransferIn xfer = null;
        List<Record> records = null;
        
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
             log().warn(message, e);
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
        ARecord arec = (ARecord)rec;
        String addr = StringUtils.stripStart(arec.getAddress().toString(), "/");

        RequisitionNode n = new RequisitionNode();
        
        String host = rec.getName().toString();
        String nodeLabel = StringUtils.stripStart(host, ".");
        
        n.setBuilding(getForeignSource());
        
        n.setForeignId(computeHashCode(nodeLabel));
        n.setNodeLabel(nodeLabel);
        
        RequisitionInterface i = new RequisitionInterface();
        i.setDescr("DNS-A");
        i.setIpAddr(addr);
        i.setSnmpPrimary("P");
        i.setManaged(Boolean.valueOf(true));
        i.setStatus(Integer.valueOf(1));
        
        i.insertMonitoredService(new RequisitionMonitoredService("ICMP"));
        i.insertMonitoredService(new RequisitionMonitoredService("SNMP"));
        n.insertInterface(i );
        
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
        
        log().info("matchingRecord: checking rec: "+rec+" to see if it should be imported...");

        boolean matches = false;
        
        if ("A".equals(Type.string(rec.getType()))) {
            
            log().debug("matchingRecord: record is a an A record, continuing...");
            
            String expression = determineExpressionFromUrl(getUrl());
            
            if (expression != null) {

                Pattern p = Pattern.compile(expression);
                Matcher m = p.matcher(rec.getName().toString());

                log().debug("matchingRecord: attempting to match record: ["+rec.getName().toString()+"] with expression: ["+expression+"]");
                if (m.matches()) {
                    matches = true;
                }
                
                log().debug("matchingRecord: record matches expression: "+matches);
                
            } else {
                
                log().debug("matchingRecord: on expression for this zone, returning valid match for this A record...");
                
                matches = true;
            }

        }
        
        log().info("matchingRecord: record: "+rec+" matches: "+matches);
        
        return matches;
    }
    

    /**
     * Created this in the case that we decide to every do something different with the hashing
     * to have a lesser likely hood of duplicate foreign ids
     * @param nodeLabel
     * @return
     */
    private String computeHashCode(String nodeLabel) {
        String hash = String.valueOf(nodeLabel.hashCode());
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public URL getURL() {
        // TODO Auto-generated method stub
        return super.getURL();
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
        ProvisionPrefixContextResolver cr = new ProvisionPrefixContextResolver();
        JAXBContext context = cr.getContext(r.getClass());
        Marshaller m = context.createMarshaller();
        Writer w = new StringWriter();
        m.marshal(r, w);
        
        String xml = w.toString();
        return xml;
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

    /**
     * <p>determineExpressionFromUrl</p>
     *
     * @param url a {@link java.net.URL} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String determineExpressionFromUrl(URL url) {
        log().info("determineExpressionFromUrl: finding regex as parameter in query string of URL: "+url);
        
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
                log().warn("determineExpressionFromUrl: syntax error in URL query string, missing '=' in query argument: "+queryArg);
            } else {
                args.put(argTokens[0].toLowerCase(), argTokens[1]);
            }
        }

        return args.get(EXPRESSION_ARG);
    }

    private static List<String> tokenizeQueryArgs(String query) throws IllegalArgumentException {
        
        if (query == null) {
            throw new IllegalArgumentException("The URL query is null");
        }

        List<String> queryArgs = new ArrayList<String>();
        queryArgs = Arrays.asList(StringUtils.split(query, QUERY_ARG_SEPARATOR));

        return queryArgs;
    }

    /**
     * <p>decodeQueryString</p>
     *
     * @param url a {@link java.net.URL} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String decodeQueryString(URL url) {
        String query = null;
        
        if (url == null || url.getQuery() == null) {
            throw new IllegalArgumentException("The URL or the URL query is null: "+url);
        }
        
        try {
            query = URLDecoder.decode(url.getQuery(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log().error("decodeQueryString: "+e, e);
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
        
        if (query != null && determineExpressionFromUrl(url) == null) {
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
    
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(DnsRequisitionUrlConnection.class);
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
