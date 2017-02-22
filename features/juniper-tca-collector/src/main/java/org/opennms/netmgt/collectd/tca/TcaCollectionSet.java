/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.collectd.CollectionTimedOut;
import org.opennms.netmgt.collectd.CollectionWarning;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.support.AbstractCollectionSet;
import org.opennms.netmgt.collection.support.ConstantTimeKeeper;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TcaCollectionSet.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class TcaCollectionSet extends AbstractCollectionSet {	
	private static final Logger LOG = LoggerFactory.getLogger(TcaCollectionSet.class);

	/** The Constant LAST_TIMESTAMP. */
	public static final String LAST_TIMESTAMP = "__tcaLastTimestamp";

	/** The Constant INBOUND_DELAY. */
	public static final String INBOUND_DELAY = "inboundDelay";

	/** The Constant INBOUND_JITTER. */
	public static final String INBOUND_JITTER = "inboundJitter";

	/** The Constant OUTBOUND_DELAY. */
	public static final String OUTBOUND_DELAY = "outboundDelay";

	/** The Constant OUTBOUND_JITTER. */
	public static final String OUTBOUND_JITTER = "outboundJitter";

	/** The Constant TIMESYNC_STATUS. */
	public static final String TIMESYNC_STATUS = "timesyncStatus";

	/** The Collection Status. */
	private int m_status;

	/** The list of SNMP collection resources. */
	private List<TcaCollectionResource> m_collectionResources;

	/** The Collection timestamp. */
	private Date m_timestamp;

	/** The Collection Agent. */
	private SnmpCollectionAgent m_agent;

	private final RrdRepository m_repository;

	private final ResourceStorageDao m_resourceStorageDao;

	/**
	 * Instantiates a new TCA collection set.
	 *
	 * @param agent the agent
	 * @param repository the repository
	 */
	public TcaCollectionSet(SnmpCollectionAgent agent, RrdRepository repository, ResourceStorageDao resourceStorageDao) {
		m_status = ServiceCollector.COLLECTION_FAILED;
		m_collectionResources = new ArrayList<TcaCollectionResource>();
		m_agent = agent;
		m_repository = repository;
		m_resourceStorageDao = resourceStorageDao;
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
			TcaData tracker = new TcaData(m_agent.getAddress());
			SnmpWalker walker = SnmpUtils.createWalker(m_agent.getAgentConfig(), "TcaCollector for " + m_agent.getHostAddress(), tracker);
			walker.start();
			LOG.debug("collect: successfully instantiated TCA Collector for {}", m_agent.getHostAddress());

			walker.waitFor();
			LOG.info("collect: node TCA query for address {} complete.", m_agent.getHostAddress());

			verifySuccessfulWalk(walker);
			process(tracker);

			m_status = ServiceCollector.COLLECTION_SUCCEEDED;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CollectionWarning("Collection of node TCA data for interface " + m_agent.getHostAddress() + " interrupted: " + e, e);
		} catch (Exception e) {
			throw new CollectionException("Can't collect TCA data because " + e.getMessage(), e);
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
	 * <li>delay local-remote ~ current inbound-delay</li>
	 * <li>jitter local-remote ~ current inbound-jitter</li>
	 * <li>delay remote-local ~ current outbound-delay</li>
	 * <li>jitter remote-local ~ current outbound-jitter-</li>
	 * <li>timesync status (1 = good, time is synced, 0 = bad, out-of sync)</li>
	 * </ul>
	 *
	 * @param tracker the tracker
	 * @throws Exception the exception
	 */
	private void process(TcaData tracker) throws Exception {
		LOG.debug("process: processing raw TCA data for {} peers.", tracker.size());
		AttributeGroupType attribGroupType = new AttributeGroupType(TcaCollectionResource.RESOURCE_TYPE_NAME, AttributeGroupType.IF_TYPE_ALL); // It will be treated like a Multi-Instance Resource
		long timestamp = 0;
		for (TcaDataEntry entry : tracker.getEntries()) {
			long lastTimestamp = getLastTimestamp(new TcaCollectionResource(m_agent, entry.getPeerAddress()));
			String[] rawData = entry.getRawData().split("\\|");
			int samples = Integer.parseInt(rawData[1]);
			SnmpObjId entryObjId = SnmpObjId.get(".1.3.6.1.4.1.27091.3.1.6.1.2", entry.getInstance().toString());
			for (int i=0; i<samples; i++) {
				LOG.debug("process: processing row {}: {}", i, rawData[2 + i]);
				String[] rawEntry = rawData[2 + i].split(",");
				timestamp = Long.parseLong(rawEntry[0]);
				if (timestamp > lastTimestamp) {
					TcaCollectionResource resource = new TcaCollectionResource(m_agent, entry.getPeerAddress());
					resource.setTimeKeeper(new ConstantTimeKeeper(new Date(timestamp * 1000)));
					resource.setAttributeValue(new TcaCollectionAttributeType(attribGroupType, entryObjId, INBOUND_DELAY), rawEntry[1]);
					resource.setAttributeValue(new TcaCollectionAttributeType(attribGroupType, entryObjId, INBOUND_JITTER), rawEntry[2]);
					resource.setAttributeValue(new TcaCollectionAttributeType(attribGroupType, entryObjId, OUTBOUND_DELAY), rawEntry[3]);
					resource.setAttributeValue(new TcaCollectionAttributeType(attribGroupType, entryObjId, OUTBOUND_JITTER), rawEntry[4]);
					resource.setAttributeValue(new TcaCollectionAttributeType(attribGroupType, entryObjId, TIMESYNC_STATUS), rawEntry[5]);
					m_collectionResources.add(resource);
				} else {
					LOG.debug("process: skipping row {} {} because it was already processed.", i, rawData[2+i]);
				}
			}
			setLastTimestamp(new TcaCollectionResource(m_agent, entry.getPeerAddress()), timestamp);
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
	 * Gets the last timestamp.
	 *
	 * @param resource the TCA resource
	 * @return the last timestamp
	 * @throws Exception the exception
	 */
	private long getLastTimestamp(TcaCollectionResource resource) throws Exception {
		File file = null;
		long timestamp = 0;
		try {
		    ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(m_repository, resource.getPath());
			String ts = m_resourceStorageDao.getStringAttribute(path, LAST_TIMESTAMP);
			if (ts != null)
				timestamp = Long.parseLong(ts);
		} catch (Exception e) {
			LOG.info("getLastFilename: creating a new filename tracker on {}", file);
		}
		return timestamp;
	}

	/**
	 * Sets the last timestamp.
	 *
	 * @param resource the resource
	 * @param timestamp the timestamp
	 * @throws Exception the exception
	 */
	private void setLastTimestamp(TcaCollectionResource resource, long timestamp) throws Exception {
	    ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(m_repository, resource.getPath());
	    m_resourceStorageDao.setStringAttribute(path, LAST_TIMESTAMP, Long.toString(timestamp));
	}
}
