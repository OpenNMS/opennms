/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class TimeTetraLldpLocPortGetter extends SnmpGetter {

    private final static Logger LOG = LoggerFactory.getLogger(TimeTetraLldpLocPortGetter.class);

    public final static SnmpObjId TIMETETRA_LLDP_LOC_PORTID_SUBTYPE = SnmpObjId.get(".1.3.6.1.4.1.6527.3.1.2.59.3.1.1.2");
    public final static SnmpObjId TIMETETRA_LLDP_LOC_PORTID         = SnmpObjId.get(".1.3.6.1.4.1.6527.3.1.2.59.3.1.1.3");
    public final static SnmpObjId TIMETETRA_LLDP_LOC_DESCR          = SnmpObjId.get(".1.3.6.1.4.1.6527.3.1.2.59.3.1.1.4");

	public TimeTetraLldpLocPortGetter(SnmpAgentConfig peer, LocationAwareSnmpClient client, String location) {
	    super(peer, client, location);
	}

    public List<SnmpValue> get(Integer ifindex,Integer tmnxLldpRemLocalDestMACAddress) {
        return get(Arrays.asList(SnmpObjId.get(TIMETETRA_LLDP_LOC_PORTID_SUBTYPE).append(ifindex.toString()), SnmpObjId.get(TIMETETRA_LLDP_LOC_PORTID).append(ifindex.toString()), SnmpObjId.get(TIMETETRA_LLDP_LOC_DESCR).append(ifindex.toString())), tmnxLldpRemLocalDestMACAddress);
    }

    // In case port sub type is local the portid is the ifindex and then we need to convert the exa decimal to int.
    public LldpLink getLldpLink(TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow timeTetraLldpRemRow) {

	    LldpLink lldpLink= timeTetraLldpRemRow.getLldpLink();
        List<SnmpValue> val = get(lldpLink.getLldpPortIfindex(),timeTetraLldpRemRow.getTmnxLldpRemLocalDestMACAddress());

        if (val == null ) {
            LOG.debug("getLldpLink: cannot find local instance for lldp ifindex {} and local port number {}",
                    lldpLink.getLldpPortIfindex(),
                    lldpLink.getLldpLocalPortNum());
            LOG.debug("getLldpLink: setting default not found Values: portidtype \"InterfaceAlias\", portid=\"Not Found On lldpLocPortTable\"");
            lldpLink.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
            lldpLink.setLldpPortId("\"Not Found On lldpLocPortTable\"");
            lldpLink.setLldpPortDescr("");
            return lldpLink;
        }

        if (val.get(0) == null || val.get(0).isError() || !val.get(0).isNumeric()) {
            LOG.debug("getLldpLink: port id subtype is null or invalid for lldp ifindex {} and local port number {}",
                    lldpLink.getLldpPortIfindex(),
                    lldpLink.getLldpLocalPortNum());
            LOG.debug("get: setting default not found Values: portidtype \"InterfaceAlias\"");
            lldpLink.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
        } else {
            lldpLink.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.get(val.get(0).toInt()));
        }
        if (val.get(1) == null || val.get(1).isError()) {
            LOG.debug("getLldpLink: port id is null for lldp ifindex {} and local port number {}",
                    lldpLink.getLldpPortIfindex(),
                    lldpLink.getLldpLocalPortNum());
            LOG.debug("getLldpLink: setting default not found Values: portid=\"Not Found On lldpLocPortTable\"");
            lldpLink.setLldpPortId("\"Not Found On lldpLocPortTable\"");
        } else {
            lldpLink.setLldpPortId(decodeLldpPortId(lldpLink.getLldpPortIdSubType(),
                    val.get(1)));
        }
        if (val.get(2) != null && !val.get(2).isError())
            lldpLink.setLldpPortDescr((val.get(2).toDisplayString()));
        else
            lldpLink.setLldpPortDescr("");
        return lldpLink;
    }

    public static String decodeLldpPortId(LldpUtils.LldpPortIdSubType portSubType, SnmpValue snmpValue) {
	    if (portSubType == LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
	        return String.valueOf(Integer.parseInt(LldpRemTableTracker.decodeLldpPortId( LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL.getValue(),snmpValue),16));
        }
	    return LldpRemTableTracker.decodeLldpPortId(portSubType.getValue(),snmpValue);
    }

}
