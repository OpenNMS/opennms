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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.AbstractRequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionRequest;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;
import org.xbill.DNS.ZoneTransferException;
import org.xbill.DNS.ZoneTransferIn;

public class DnsRequisitionProvider extends AbstractRequisitionProvider<DnsRequisitionRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(DnsRequisitionProvider.class);

    public static final String TYPE_NAME = "dns";

    public DnsRequisitionProvider() {
        super(DnsRequisitionRequest.class);
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public RequisitionRequest getRequest(Map<String, String> parameters) {
        return new DnsRequisitionRequest(parameters);
    }

    @Override
    public Requisition getRequisitionFor(DnsRequisitionRequest request) {
        ZoneTransferIn xfer = null;
        List<Record> records = null;

        LOG.debug("connecting to host {}:{}", request.getHost(), request.getPort());
        try {
            try {
                xfer = ZoneTransferIn.newIXFR(new Name(request.getZone()), request.getSerial(),
                        request.getFallback(), request.getHost(), request.getPort(), null);
                records = getRecords(xfer);
            } catch (ZoneTransferException e) {
                // Fallback to AXFR
                String message = "IXFR not supported trying AXFR: " + e;
                LOG.warn(message, e);
                xfer = ZoneTransferIn.newAXFR(new Name(request.getZone()), request.getHost(), null);
                records = getRecords(xfer);
            }
        } catch (IOException | ZoneTransferException e) {
            throw new RuntimeException(e);
        }

        if (records.size() > 0) {
            // for now, set the foreign source to the specified dns zone
            final Requisition r = new Requisition(request.getForeignSource());
            for (Record rec : records) {
                if (matchingRecord(request, rec)) {
                    r.insertNode(createRequisitionNode(request, rec));
                }
            }
            return r;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Record> getRecords(ZoneTransferIn xfer) throws IOException, ZoneTransferException {
        return (List<Record>) xfer.run();
    }

    /**
     * Determines if the record is an A record and if the canonical name matches
     * the expression supplied in the URL, if one was supplied.
     * 
     * @param rec
     * @return boolean if rec should be included in the import requisition
     */
    private boolean matchingRecord(DnsRequisitionRequest request, Record rec) {
        LOG.info("matchingRecord: checking rec: {} to see if it should be imported...", rec);

        boolean matches = false;
        if ("A".equals(Type.string(rec.getType())) || "AAAA".equals(Type.string(rec.getType()))) {
            LOG.debug("matchingRecord: record is an {} record, continuing...", Type.string(rec.getType()));

            final String expression = request.getExpression();
            if (expression != null) {
                final Pattern p = Pattern.compile(expression);
                Matcher m = p.matcher(rec.getName().toString());

                // Try matching on host name only for backwards compatibility
                LOG.debug("matchingRecord: attempting to match hostname: [{}] with expression: [ {} ]", rec.getName(),
                        expression);
                if (m.matches()) {
                    matches = true;
                } else {
                    // include the IP address and try again
                    LOG.debug("matchingRecord: attempting to match record: [{} {}] with expression: [{}]",
                            rec.getName(), rec.rdataToString(), expression);
                    m = p.matcher(rec.getName().toString() + " " + rec.rdataToString());
                    if (m.matches()) {
                        matches = true;
                    }
                }
                LOG.debug("matchingRecord: record matches expression: {}", matches);
            } else {
                LOG.debug("matchingRecord: no expression for this zone, returning valid match for this {} record...",
                        Type.string(rec.getType()));
                matches = true;
            }
        }

        LOG.info("matchingRecord: record: {} matches: {}", matches, rec);
        return matches;
    }

    /**
     * Creates an instance of the JaxB annotated RequisionNode class.
     * 
     * @param rec
     * @return a populated RequisitionNode based on defaults and data from the A
     *         record returned from a DNS zone transfer query.
     */
    private RequisitionNode createRequisitionNode(DnsRequisitionRequest request, Record rec) {
        String addr = null;
        if ("A".equals(Type.string(rec.getType()))) {
            final ARecord arec = (ARecord) rec;
            addr = StringUtils.stripStart(arec.getAddress().toString(), "/");
        } else if ("AAAA".equals(Type.string(rec.getType()))) {
            final AAAARecord aaaarec = (AAAARecord) rec;
            addr = aaaarec.rdataToString();
        } else {
            throw new IllegalArgumentException(
                    "Invalid record type " + Type.string(rec.getType()) + ". A or AAAA expected.");
        }

        final RequisitionNode n = new RequisitionNode();

        final String host = rec.getName().toString();
        final String nodeLabel = StringUtils.stripEnd(StringUtils.stripStart(host, "."), ".");

        n.setBuilding(request.getForeignSource());

        switch (request.getForeignIdHashSource()) {
        case NODE_LABEL:
            n.setForeignId(computeHashCode(nodeLabel));
            LOG.debug("Generating foreignId from hash of nodelabel {}", nodeLabel);
            break;
        case IP_ADDRESS:
            n.setForeignId(computeHashCode(addr));
            LOG.debug("Generating foreignId from hash of ipAddress {}", addr);
            break;
        case NODE_LABEL_AND_IP_ADDRESS:
            n.setForeignId(computeHashCode(nodeLabel + addr));
            LOG.debug("Generating foreignId from hash of nodelabel+ipAddress {}{}", nodeLabel, addr);
            break;
        default:
            n.setForeignId(computeHashCode(nodeLabel));
            LOG.debug("Default case: Generating foreignId from hash of nodelabel {}", nodeLabel);
            break;
        }
        n.setNodeLabel(nodeLabel);

        final RequisitionInterface i = new RequisitionInterface();
        i.setDescr("DNS-" + Type.string(rec.getType()));
        i.setIpAddr(addr);
        i.setSnmpPrimary(PrimaryType.PRIMARY);
        i.setManaged(Boolean.TRUE);
        i.setStatus(Integer.valueOf(1));

        for (String service : request.getServices()) {
            service = service.trim();
            i.insertMonitoredService(new RequisitionMonitoredService(service));
            LOG.debug("Adding provisioned service {}", service);
        }

        n.putInterface(i);
        return n;
    }

    /**
     * Created this in the case that we decide to every do something different
     * with the hashing to have a lesser likely hood of duplicate foreign ids
     * 
     * @param hashSource
     * @return
     */
    private String computeHashCode(String hashSource) {
        return String.valueOf(hashSource.hashCode());
    }
}
