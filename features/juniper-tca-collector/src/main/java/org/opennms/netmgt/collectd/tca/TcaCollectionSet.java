/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.CollectionTimedOut;
import org.opennms.netmgt.collectd.CollectionWarning;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

/**
 * The Class TcaCollectionSet.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class TcaCollectionSet implements CollectionSet {

	/** The SNMP OID for JUNIPER-SLA-MIB::jnxTcaSlaRawdataTable.jnxTcaSlaRawdataEntry. */
	public static final String BASE_OID = ".1.3.6.1.4.1.27091.3.1.6.1";

	/** The collection status. */
	private int m_status;

	/** The list of SNMP collection resources. */
	private List<TcaCollectionResource> m_collectionResources;

	/** The collection timestamp. */
	private Date m_timestamp;

	/** The m_agent. */
	private CollectionAgent m_agent;

	/**
	 * Instantiates a new TCA collection set.
	 *
	 * @param agent the agent
	 */
	public TcaCollectionSet(CollectionAgent agent) {
		m_status = ServiceCollector.COLLECTION_FAILED;
		m_collectionResources = new ArrayList<TcaCollectionResource>();
		m_agent = agent;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionSet#getStatus()
	 */
	@Override
	public int getStatus() {
		return m_status;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionSet#visit(org.opennms.netmgt.config.collector.CollectionSetVisitor)
	 */
	@Override
	public void visit(CollectionSetVisitor visitor) {
		visitor.visitCollectionSet(this);
		for (CollectionResource resource : m_collectionResources) {
			resource.visit(visitor);
		}
		visitor.completeCollectionSet(this);
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionSet#ignorePersist()
	 */
	@Override
	public boolean ignorePersist() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionSet#getCollectionTimestamp()
	 */
	@Override
	public Date getCollectionTimestamp() {
		return m_timestamp;
	}

	/**
	 * Sets the collection timestamp.
	 *
	 * @param date the new collection timestamp
	 */
	public void setCollectionTimestamp(Date date) {
		m_timestamp = date;
	}

	/**
	 * Collect.
	 *
	 * @throws CollectionException the collection exception
	 */
	protected void collect() throws CollectionException {
		try {
			TcaData tracker = new TcaData(m_agent.getInetAddress());
			SnmpWalker walker = SnmpUtils.createWalker(m_agent.getAgentConfig(), "TcaCollector for " + m_agent.getHostAddress(), tracker);
			walker.start();
			log().debug("collect: successfully instantiated " + "TCA Collector for " + m_agent.getHostAddress());

			walker.waitFor();
			log().info("collect: node TCA query for address " + m_agent.getHostAddress() + " complete.");

			verifySuccessfulWalk(walker);
			process(tracker);

			m_status = ServiceCollector.COLLECTION_SUCCEEDED;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CollectionWarning("collect: Collection of node TCA data for interface " + m_agent.getHostAddress() + " interrupted: " + e, e);
		}
	}
	
	/**
	 * Gets the collection resources.
	 *
	 * @return the collection resources
	 */
	protected List<TcaCollectionResource> getCollectionResources() {
		return m_collectionResources;
	}

	/**
	 * Process.
	 *
	 * <p>A sample TCA Data looks like the following:</p>
	 * <ul>
	 * <li>OID=.1.3.6.1.4.1.27091.3.1.6.1.1.172.19.37.60.1, Type=OctetString, Value=172.19.37.60 </li>
	 * <li>OID=.1.3.6.1.4.1.27091.3.1.6.1.2.172.19.37.60.1, Type=OctetString, Value=
	 * |25|1327451762,11,0,11,0,1|1327451763,11,0,11,0,1|1327451764,11,0,11,0,1|1327451765,11,0,11,0,1|1327451766,11,0,11,0,1| 
	 * 1327451767,11,0,11,0,1|1327451768,11,0,11,0,1|1327451769,11,0,11,0,1|1327451770,11,0,11,0,1|1327451771,11,0,11,0,1| 
	 * 1327451772,11,0,11,0,1|1327451773,11,0,11,0,1|1327451774,11,0,11,0,1|1327451775,11,0,11,0,1|1327451776,11,0,11,0,1| 
	 * 1327451777,11,0,11,0,1|1327451778,11,0,11,0,1|1327451779,11,0,11,0,1|1327451780,11,0,11,0,1|1327451781,11,0,11,0,1| 
	 * 1327451782,11,0,11,0,1|1327451783,11,0,11,0,1|1327451784,11,0,11,0,1|1327451785,11,0,11,0,1|1327451786,11,0,11,0,1|</li>
	 * </ul>
	 * 
	 * <ul>
	 * <li>timestamp (epoch)</li>
	 * <li>delay local-remote</li>
	 * <li>jitter local-remote</li>
	 * <li>delay remote-local</li>
	 * <li>jitter remote-local</li>
	 * <li>status (1 = good, time is synced, 0 = bad, out-of sync)</li>
	 * </ul>
	 * 
	 * @param tracker the tracker
	 */
	private void process(TcaData tracker) {
		log().debug("process: tracker status: isFinished=" + tracker.isFinished() + ", isEmpty=" + tracker.isEmpty());
		log().debug("process: processing raw TCA data for " + tracker.size() + " resources.");
		AttributeGroupType attribGroupType = new AttributeGroupType(TcaCollectionResource.RESOURCE_TYPE_NAME, "all"); // It will be treated like a Multi-Instance Resource
		for (TcaDataEntry entry : tracker.getEntries()) {
			String[] rawData = entry.getRawData().split("|");
			int samples = Integer.parseInt(rawData[1]);
			for (int i=0; i<samples; i++) {
				log().debug("process: processing row " + i + ": " + rawData[2 + i]);
				String[] rawEntry = rawData[2 + i].split(",");
				if (rawEntry[5].equals("1")) {
					long timestamp = Long.parseLong(rawEntry[0]);
					TcaCollectionResource resource = new TcaCollectionResource(m_agent, entry.getPeerAddress());
					resource.setTimeKeeper(new ConstantTimeKeeper(timestamp));
					TcaCollectionAttributeType delayLocalRemote = new TcaCollectionAttributeType(attribGroupType, "delayLocalRemote");
					resource.setAttributeValue(delayLocalRemote, rawEntry[1]);
					TcaCollectionAttributeType jitterLocalRemote = new TcaCollectionAttributeType(attribGroupType, "jitterLocalRemote");
					resource.setAttributeValue(jitterLocalRemote, rawEntry[2]);
					TcaCollectionAttributeType delayRemoteLocal = new TcaCollectionAttributeType(attribGroupType, "delayRemoteLocal");
					resource.setAttributeValue(delayRemoteLocal, rawEntry[3]);
					TcaCollectionAttributeType jitterRemoteLocal = new TcaCollectionAttributeType(attribGroupType, "jitterRemoteLocal");
					resource.setAttributeValue(jitterRemoteLocal, rawEntry[4]);
					m_collectionResources.add(resource);
				}
			}
		}
	}

	/**
	 * Log error and return COLLECTION_FAILED is there is a failure.
	 *
	 * @param walker the walker
	 * @throws CollectionException the collection exception
	 */
	private void verifySuccessfulWalk(SnmpWalker walker) throws CollectionException {
		if (!walker.failed()) {
			return;
		}
		if (walker.timedOut()) {
			throw new CollectionTimedOut(walker.getErrorMessage());
		}
		String message = "collection failed for " + m_agent.getHostAddress()  + " due to: " + walker.getErrorMessage();
		throw new CollectionWarning(message, walker.getErrorThrowable());
	}

	/**
	 * Log.
	 *
	 * @return the thread category
	 */
	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}
}
