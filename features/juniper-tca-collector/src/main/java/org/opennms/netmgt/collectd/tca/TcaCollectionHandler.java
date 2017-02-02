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

import java.util.Date;
import java.util.Objects;

import org.opennms.netmgt.collectd.CollectionTimedOut;
import org.opennms.netmgt.collectd.CollectionWarning;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
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
 * <p>The resource type is fixed to <code>juniperTcaEntry</code></p>
 * <p>This requires to define a datacollection-group like this:</p>
 * <pre>
 * &lt;datacollection-group name="Juniper TCA"&gt;
 *    &lt;resourceType name="juniperTcaEntry" label="Juniper TCA Entry" resourceLabel="Peer ${index}"&gt;
 *     &lt;persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"/&gt;
 *     &lt;storageStrategy class="org.opennms.netmgt.collection.support.IndexStorageStrategy"/&gt;
 *   &lt;/resourceType&gt;
 * &lt;/datacollection-group&gt;
 * </pre>
 * <p>Note: the persistenceSelectorStrategy and storageStrategy won't be used by the collector.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class TcaCollectionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(TcaCollectionHandler.class);

	/** The Constant RESOURCE_TYPE_NAME. */
	public static final String RESOURCE_TYPE_NAME = "juniperTcaEntry";

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


	/** The Collection Agent. */
	private SnmpCollectionAgent m_agent;

	private final RrdRepository m_repository;

	private final ResourceStorageDao m_resourceStorageDao;

    private final ResourceType m_resourceType;

	/**
	 * Instantiates a new TCA collection set.
	 *
	 * @param agent the agent
	 * @param repository the repository
	 */
	public TcaCollectionHandler(SnmpCollectionAgent agent, RrdRepository repository, ResourceStorageDao resourceStorageDao, ResourceTypesDao resourceTypesDao) {
		m_agent = Objects.requireNonNull(agent);
		m_repository = Objects.requireNonNull(repository);
		m_resourceStorageDao = Objects.requireNonNull(resourceStorageDao);
		m_resourceType = Objects.requireNonNull(resourceTypesDao).getResourceTypeByName(RESOURCE_TYPE_NAME);
		if (m_resourceType == null) {
		    throw new IllegalArgumentException("No resource of type juniperTcaEntry is defined.");
		}
	}

	/**
	 * Collect.
	 *
	 * @throws CollectionException the collection exception
	 */
	protected CollectionSet collect() throws CollectionException {
		try {
			CollectionSetBuilder builder = new CollectionSetBuilder(m_agent);

			TcaData tracker = new TcaData(m_agent.getAddress());
			try(SnmpWalker walker = SnmpUtils.createWalker(m_agent.getAgentConfig(), "TcaCollector for " + m_agent.getHostAddress(), tracker)) {
    			walker.start();
    			LOG.debug("collect: successfully instantiated TCA Collector for {}", m_agent.getHostAddress());

    			walker.waitFor();
    			LOG.info("collect: node TCA query for address {} complete.", m_agent.getHostAddress());

    			verifySuccessfulWalk(walker);
			}
			process(tracker, builder);

			return builder.build();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CollectionWarning("Collection of node TCA data for interface " + m_agent.getHostAddress() + " interrupted: " + e, e);
		} catch (Exception e) {
			throw new CollectionException("Can't collect TCA data because " + e.getMessage(), e);
		}
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
	private void process(TcaData tracker, CollectionSetBuilder builder) throws Exception {
		LOG.debug("process: processing raw TCA data for {} peers.", tracker.size());

		final NodeLevelResource nodeResource = new NodeLevelResource(m_agent.getNodeId());

		long timestamp = 0;
		for (TcaDataEntry entry : tracker.getEntries()) {
			GenericTypeResource resource = new GenericTypeResource(nodeResource, m_resourceType, entry.getPeerAddress());
			CollectionResource collectionResource = CollectionSetBuilder.toCollectionResource(resource, m_agent);

			long lastTimestamp = getLastTimestamp(collectionResource);
			String[] rawData = entry.getRawData().split("\\|");
			int samples = Integer.parseInt(rawData[1]);
			SnmpObjId entryObjId = SnmpObjId.get(".1.3.6.1.4.1.27091.3.1.6.1.2", entry.getInstance().toString());
			String identifierPrefix = String.format("TCA_%s_", entryObjId);

			for (int i=0; i<samples; i++) {
				LOG.debug("process: processing row {}: {}", i, rawData[2 + i]);
				String[] rawEntry = rawData[2 + i].split(",");
				timestamp = Long.parseLong(rawEntry[0]);
				if (timestamp > lastTimestamp) {
					resource = new GenericTypeResource(nodeResource, m_resourceType, entry.getPeerAddress());
					resource.setTimestamp(new Date(timestamp * 1000));
					builder.withIdentifiedNumericAttribute(resource, RESOURCE_TYPE_NAME, INBOUND_DELAY, Double.parseDouble(rawEntry[1]), AttributeType.GAUGE, identifierPrefix + INBOUND_DELAY);
					builder.withIdentifiedNumericAttribute(resource, RESOURCE_TYPE_NAME, INBOUND_JITTER, Double.parseDouble(rawEntry[2]), AttributeType.GAUGE, identifierPrefix + INBOUND_JITTER);
					builder.withIdentifiedNumericAttribute(resource, RESOURCE_TYPE_NAME, OUTBOUND_DELAY, Double.parseDouble(rawEntry[3]), AttributeType.GAUGE, identifierPrefix + OUTBOUND_DELAY);
					builder.withIdentifiedNumericAttribute(resource, RESOURCE_TYPE_NAME, OUTBOUND_JITTER, Double.parseDouble(rawEntry[4]), AttributeType.GAUGE, identifierPrefix + OUTBOUND_JITTER);
					builder.withIdentifiedNumericAttribute(resource, RESOURCE_TYPE_NAME, TIMESYNC_STATUS, Double.parseDouble(rawEntry[5]), AttributeType.GAUGE, identifierPrefix + TIMESYNC_STATUS);
				} else {
					LOG.debug("process: skipping row {} {} because it was already processed.", i, rawData[2+i]);
				}
			}
			setLastTimestamp(collectionResource, timestamp);
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
	private long getLastTimestamp(CollectionResource resource) throws Exception {
		long timestamp = 0;
		ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(m_repository, resource.getPath());
		try {
			LOG.debug("Retrieving timestamp from path {}", path);
			String ts = m_resourceStorageDao.getStringAttribute(path, LAST_TIMESTAMP);
			if (ts != null) {
				timestamp = Long.parseLong(ts);
			}
		} catch (Exception e) {
			LOG.error("Failed to retrieve timestamp from path {}", path, e);
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
	private void setLastTimestamp(CollectionResource resource, long timestamp) throws Exception {
		ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(m_repository, resource.getPath());
		LOG.debug("Setting timestamp to {} at path {}", timestamp, path);
		m_resourceStorageDao.setStringAttribute(path, LAST_TIMESTAMP, Long.toString(timestamp));
	}
}
