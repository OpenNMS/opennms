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
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.opennms.netmgt.provision.persist.ProvisionPrefixContextResolver;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.springframework.util.StringUtils;
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
 *
 */
public class DnsRequisitionUrlConnection extends URLConnection {

    public static final String URL_SCHEME = "dns://";
    
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
        
    protected DnsRequisitionUrlConnection(URL url) throws MalformedURLException {
        super(url);
        
        m_url = url;
        
        m_port = url.getPort() == -1 ? 53 : url.getPort();
        
        m_zone = parseZone(url);
        
        if (m_zone == null) {
            throw new IllegalArgumentException("Specified Zone is null");
        }
        
        m_serial = Long.valueOf(0L);
        m_fallback = Boolean.valueOf(false);

        m_key = null;

    }

    private String parseZone(URL url) {
        return url.getPath().replaceAll("/", "");
    }

    
    /**
     * This is a no op.
     */
    @Override
    public void connect() throws IOException {
    }

    /**
     * Creates a ByteArrayInputStream implementation of InputStream of the XML marshaled version
     * of the Requisition class.  Calling close on this stream is safe.
     * 
     */
    @Override
    public InputStream getInputStream() {
        
        InputStream stream = null;
        
        try {
            Requisition r = buildRequisitionFromZoneTransfer();
            stream = new ByteArrayInputStream(jaxBMarshal(r).getBytes());
            
        } catch (Exception e) {
            System.out.println(e);
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
        //ZoneTransferIn xfer = ZoneTransferIn.newAXFR(new Name(m_zone), m_host, m_key);
        ZoneTransferIn xfer = ZoneTransferIn.newIXFR(new Name(m_zone), 
                                      m_serial.longValue(), 
                                      m_fallback.booleanValue(), 
                                      m_url.getHost(), 
                                      m_port,
                                      m_key);

        List<Record> records = xfer.run();
        
        Requisition r = null;
        
        if (records.size() > 0) {
            
            //for now, set the foreign source to the specified dns zone
            r = new Requisition(m_zone);
            
            for (Record rec : records) {
                if ("A".equals( Type.string(rec.getType()))) {
                    r.insertNode(createRequisitionNode(rec));
                }
            }
        }
        
        return r;
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
        String addr = StringUtils.trimLeadingCharacter(arec.getAddress().toString(), '/');

        RequisitionNode n = new RequisitionNode();
        String host = rec.getName().toString();
        n.setBuilding(getZone());
        String nodeLabel = StringUtils.trimTrailingCharacter(host, '.');
        n.setForeignId(computeHashCode(nodeLabel));
        n.setNodeLabel(nodeLabel);
        
        RequisitionInterface i = new RequisitionInterface();
        i.setDescr("DNS");
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
     * Created this in the case that we decide to every do something different with the hashing
     * to have a lesser likely hood of duplicate foreign ids
     * @param nodeLabel
     * @return
     */
    private String computeHashCode(String nodeLabel) {
        String hash = String.valueOf(nodeLabel.hashCode());
        return hash;
    }

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
    
    public String getZone() {
        return m_zone;
    }
    
    public Long getSerial() {
        return m_serial;
    }
    
    public void setSerial(Long serial) {
        m_serial = serial;
    }
    
    public Boolean getFallback() {
        return m_fallback;
    }
    
    public void setFallback(Boolean fallback) {
        m_fallback = fallback;
    }
    
    public TSIG getKey() {
        return m_key;
    }
    
    public void setKey(TSIG key) {
        m_key = key;
    }
    
    public String getDescription() {
        return m_url.toString();
    }
    
    public String toString() {
        return getDescription();
    }
    
    public URL getUrl() {
        return m_url;
    }
    

}
