/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpGetter;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class LldpLocPortGetter extends SnmpGetter {

    private final static Logger LOG = LoggerFactory.getLogger(LldpLocPortGetter.class);

    public final static String LLDP_LOC_PORTID_SUBTYPE = "lldpLocPortIdSubtype";
    public final static String LLDP_LOC_PORTID = "lldpLocPortId";
    public final static String LLDP_LOC_DESCR = "lldpLocPortDesc";

    public final static SnmpObjId LLDP_LOC_PORTID_SUBTYPE_OID = SnmpObjId.get(".1.0.8802.1.1.2.1.3.7.1.2");
    public final static SnmpObjId LLDP_LOC_PORTID_OID = SnmpObjId.get(".1.0.8802.1.1.2.1.3.7.1.3");
    public final static SnmpObjId LLDP_LOC_DESCR_OID = SnmpObjId.get(".1.0.8802.1.1.2.1.3.7.1.4");

	public LldpLocPortGetter(SnmpAgentConfig peer, LocationAwareSnmpClient client, String location) {
	    super(peer, client, location);
	}

	
    public List<SnmpValue> get(Integer lldpRemLocalPortNum) {
        return get(Arrays.asList(SnmpObjId.get(LLDP_LOC_PORTID_SUBTYPE_OID), SnmpObjId.get(LLDP_LOC_PORTID_OID), SnmpObjId.get(LLDP_LOC_DESCR_OID)), lldpRemLocalPortNum);
    }

    public LldpLink getLldpLink(LldpRemTableTracker.LldpRemRow row) {

        List<SnmpValue> val = get(row.getLldpRemLocalPortNum());

        LldpLink lldplink = row.getLldpLink();
        if (val == null) {
            LOG.debug("getLldpLink: cannot find local instance for lldp local port number {}",
                     lldplink.getLldpRemLocalPortNum());
            LOG.debug("getLldpLink: setting default not found Values: portidtype \"InterfaceAlias\", portid=\"Not Found On lldpLocPortTable\"");
            lldplink.setLldpPortIdSubType(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
            lldplink.setLldpPortId("\"Not Found On lldpLocPortTable\"");
            lldplink.setLldpPortDescr("");
            return lldplink;
        }

        if (val.get(0) == null || val.get(0).isError() || !val.get(0).isNumeric()) {
            LOG.debug("getLldpLink: port id subtype is null or invalid for lldp local port number {}",
                     lldplink.getLldpRemLocalPortNum());
            LOG.debug("getLldpLink: setting default not found Values: portidtype \"InterfaceAlias\"");
            lldplink.setLldpPortIdSubType(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
        } else {
            lldplink.setLldpPortIdSubType(LldpPortIdSubType.get(val.get(0).toInt()));
        }
        if (val.get(1) == null || val.get(1).isError()) {
            LOG.debug("getLldpLink: port id is null for lldp local port number {}",
                     lldplink.getLldpRemLocalPortNum());
            LOG.debug("get: setting default not found Values: portid=\"Not Found On lldpLocPortTable\"");
            lldplink.setLldpPortId("\"Not Found On lldpLocPortTable\"");
        } else {
            lldplink.setLldpPortId(LldpRemTableTracker.decodeLldpPortId(lldplink.getLldpPortIdSubType().getValue(),
                                                                        val.get(1)));
        }
        if (val.get(2) != null && !val.get(2).isError())
            lldplink.setLldpPortDescr((val.get(2).toDisplayString()));
        else
            lldplink.setLldpPortDescr("");
        if (val.get(0).isNumeric()
                && val.get(0).toInt() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL.getValue()) {
            try {
                lldplink.setLldpPortIfindex((val.get(1).toInt()));
            } catch (Exception e) {
                LOG.warn("getLldpLink: failed to convert to ifindex local port id {}",
                          val.get(1));
            }
        }
        return lldplink;
    }

}
