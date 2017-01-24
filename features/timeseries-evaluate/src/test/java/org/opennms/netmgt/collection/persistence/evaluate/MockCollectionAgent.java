/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.collection.persistence.evaluate;

import java.io.File;
import java.net.InetAddress;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.model.ResourcePath;

/**
 * The Class MockCollectionAgent.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockCollectionAgent implements CollectionAgent {

    /** The node id. */
    private int nodeId;

    /** The node label. */
    private String nodeLabel;

    /** The foreign source. */
    private String foreignSource;

    /** The foreign id. */
    private String foreignId;

    /** The ip address. */
    private InetAddress ipAddress;

    /**
     * Instantiates a new mock collection agent.
     *
     * @param nodeId the node id
     * @param nodeLabel the node label
     * @param foreignSource the foreign source
     * @param foreignId the foreign id
     * @param ipAddress the ip address
     */
    public MockCollectionAgent(int nodeId, String nodeLabel, String foreignSource, String foreignId, InetAddress ipAddress) {
        super();
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
        this.foreignSource = foreignSource;
        this.foreignId = foreignId;
        this.ipAddress = ipAddress;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.NetworkInterface#getType()
     */
    @Override
    public int getType() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.NetworkInterface#getAddress()
     */
    @Override
    public InetAddress getAddress() {
        return ipAddress;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.NetworkInterface#getAttribute(java.lang.String)
     */
    @Override
    public <V> V getAttribute(String property) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.NetworkInterface#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public Object setAttribute(String property, Object value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#isStoreByForeignSource()
     */
    @Override
    public Boolean isStoreByForeignSource() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getHostAddress()
     */
    @Override
    public String getHostAddress() {
        return ipAddress.getHostAddress();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#setSavedIfCount(int)
     */
    @Override
    public void setSavedIfCount(int ifCount) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getNodeId()
     */
    @Override
    public int getNodeId() {
        return nodeId;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getNodeLabel()
     */
    @Override
    public String getNodeLabel() {
        return nodeLabel;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getForeignSource()
     */
    @Override
    public String getForeignSource() {
        return foreignSource;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getForeignId()
     */
    @Override
    public String getForeignId() {
        return foreignId;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getLocationName()
     */
    @Override
    public String getLocationName() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getStorageDir()
     */
    @Override
    public ResourcePath getStorageResourcePath() {
        return ResourcePath.get("fs" + File.separator + foreignSource + File.separator + foreignId);
    }

    /* (non-Javadoc)
         * @see org.opennms.netmgt.collection.api.CollectionAgent#getSysObjectId()
         */
    @Override
    public String getSysObjectId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#getSavedSysUpTime()
     */
    @Override
    public long getSavedSysUpTime() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAgent#setSavedSysUpTime(long)
     */
    @Override
    public void setSavedSysUpTime(long sysUpTime) {
    }

}
