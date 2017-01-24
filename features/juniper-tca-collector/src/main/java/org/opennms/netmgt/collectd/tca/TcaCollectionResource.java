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

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.collection.support.DefaultTimeKeeper;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.model.ResourcePath;

/**
 * The Class TcaCollectionResource.
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
public class TcaCollectionResource extends AbstractCollectionResource {

	/** The Constant RESOURCE_TYPE_NAME. */
	public static final String RESOURCE_TYPE_NAME = "juniperTcaEntry";

	/** The Time Keeper. */
	private TimeKeeper m_timeKeeper = new DefaultTimeKeeper();

	/** The m_peer address. */
	private final String m_peerAddress;

	/** The m_strategy. */
	private final StorageStrategy m_strategy;

	/**
	 * Instantiates a new TCA collection resource.
	 *
	 * @param agent the collection agent
	 * @param peerAddress the TCA peer address
	 */
	protected TcaCollectionResource(CollectionAgent agent, String peerAddress) {
		super(agent);
		m_peerAddress = peerAddress;
		m_strategy = new IndexStorageStrategy();
		m_strategy.setResourceTypeName(RESOURCE_TYPE_NAME);
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionResource#getResourceTypeName()
	 */
	@Override
	public String getResourceTypeName() {
		return RESOURCE_TYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionResource#getInstance()
	 */
	@Override
	public String getInstance() {
		return m_peerAddress;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getLabel()
	 */
	@Override
	public String getInterfaceLabel() {
		return m_peerAddress;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "node[" + m_agent.getNodeId() + "]." + getResourceTypeName() + "[" + getInterfaceLabel() +"]";
	}

    @Override
    public ResourcePath getPath() {
        return m_strategy.getRelativePathForAttribute(getParent(), getInterfaceLabel());
    }

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getTimeKeeper()
	 */
	@Override
	public TimeKeeper getTimeKeeper() {
		return m_timeKeeper;
	}

	/**
	 * Sets the time keeper.
	 *
	 * @param timeKeeper the new time keeper
	 */
	public void setTimeKeeper(TimeKeeper timeKeeper) {
		m_timeKeeper = timeKeeper;
	}
	
    /**
     * Sets the attribute value.
     *
     * @param type the type
     * @param value the value
     */
    public void setAttributeValue(TcaCollectionAttributeType type, String value) {
        TcaCollectionAttribute attr = new TcaCollectionAttribute(this, type, value);
        addAttribute(attr);
    }

}
