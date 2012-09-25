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

package org.opennms.netmgt.snmpinterfacepoller;

import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface.SnmpMinimalPollInterface;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class SnmpPollInterfaceMonitor {

    /**
     * ifAdminStatus table from MIB-2.
     */
    private static final String IF_ADMIN_STATUS_OID = ".1.3.6.1.2.1.2.2.1.7.";
    
    /**
     * ifOperStatus table from MIB-2.
     */
    private static final String IF_OPER_STATUS_OID = ".1.3.6.1.2.1.2.2.1.8.";

    /**
     * <p>poll</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param mifaces a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
	public List<SnmpMinimalPollInterface> poll(SnmpAgentConfig agentConfig,
			List<SnmpMinimalPollInterface> mifaces) {

		if (mifaces == null) {
			log().error("Null Interfaces passed to Monitor, exiting");
			return null;
		}

		log().debug("Got " + mifaces.size() + " interfaces to poll");

		// Retrieve this interface's SNMP peer object
		//
		if (agentConfig == null)
			throw new RuntimeException("SnmpAgentConfig object not available");

		SnmpObjId[] adminoids = new SnmpObjId[mifaces.size()];
		SnmpObjId[] operooids = new SnmpObjId[mifaces.size()];

		for (int i = 0; i < mifaces.size(); i++) {
			SnmpMinimalPollInterface miface = mifaces.get(i);
			miface.setStatus(PollStatus.unavailable());
			adminoids[i] = SnmpObjId.get(IF_ADMIN_STATUS_OID
					+ miface.getIfindex());
			operooids[i] = SnmpObjId.get(IF_OPER_STATUS_OID
					+ miface.getIfindex());
			log().debug(
					"Adding Admin/Oper oids: " + adminoids[i] + "/"
							+ operooids[i]);
		}

		SnmpValue[] adminresults = new SnmpValue[mifaces.size()];
		SnmpValue[] operoresults = new SnmpValue[mifaces.size()];

		log().debug("try to get admin statuses");
		adminresults = SnmpUtils.get(agentConfig, adminoids);
		log().debug("got admin status " + adminresults.length + " SnmpValues");
		if (adminresults.length != mifaces.size()) {
			log().warn("Snmp Interface Admin statuses collection failed");
			return mifaces;
		}

		log().debug("try to get operational statuses");
		operoresults = SnmpUtils.get(agentConfig, operooids);
		log().debug(
				"got operational status " + operoresults.length + " SnmpValues");
		if (operoresults.length != mifaces.size()) {
			log().warn("Snmp Interface Operational statuses collection failed");
			return mifaces;
		}

		for (int i = 0; i < mifaces.size(); i++) {
			SnmpMinimalPollInterface miface = mifaces.get(i);

			if (adminresults[i] != null && operoresults[i] != null) {
				try {
					miface.setAdminstatus(adminresults[i].toInt());
					miface.setOperstatus(operoresults[i].toInt());
					miface.setStatus(PollStatus.up());
					log().debug(
							"SNMP Value is " + adminresults[i].toInt()
									+ " for oid: " + adminoids[i]);
					log().debug(
							"SNMP Value is " + operoresults[i].toInt()
									+ " for oid: " + operooids[i]);
				} catch (Exception e) {
					log().warn(
							"SNMP Value is "
									+ adminresults[i].toDisplayString()
									+ " for oid: " + adminoids[i]);
					log().warn(
							"SNMP Value is "
									+ operoresults[i].toDisplayString()
									+ " for oid: " + operooids[i]);
				}
			} else {
				log().info(
						"SNMP Value is null for oid: " + adminoids[i] + "/"
								+ operooids[i]);
			}
		}

		return mifaces;
	}

	/**
	 * <p>
	 * log
	 * </p>
	 * 
	 * @return a {@link org.opennms.core.utils.ThreadCategory} object.
	 */
	protected ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

}
