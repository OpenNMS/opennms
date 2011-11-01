/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.io.File;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.model.RrdRepository;

/**
 * The Class XmlMultiInstanceCollectionResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlMultiInstanceCollectionResource extends XmlCollectionResource {

    /** The collection resource instance. */
    private String m_instance;

    /** The collection resource type name. */
    private String m_resourceTypeName;

    /**
     * Instantiates a new XML Multi-instance collection resource.
     *
     * @param agent the agent
     * @param instance the instance
     * @param name the name
     */
    public XmlMultiInstanceCollectionResource(CollectionAgent agent, String instance, String name) {
        super(agent);
        m_instance = instance;
        m_resourceTypeName = name;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionResource#getResourceDir(org.opennms.netmgt.model.RrdRepository)
     */
    @Override
    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File nodeDir = new File(rrdBaseDir, String.valueOf(m_agent.getNodeId()));
        File typeDir = new File(nodeDir, m_resourceTypeName);
        File instDir = new File(typeDir, getInstance());
        if (log().isDebugEnabled()) {
            log().debug("getResourceDir: " + instDir.toString());
        }
        return instDir;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionResource#getResourceTypeName()
     */
    @Override
    public String getResourceTypeName() {
        return m_resourceTypeName;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionResource#getInstance()
     */
    @Override
    public String getInstance() {
        return m_instance.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_").replaceAll("[|/]", "_").replaceAll("=", "").replaceAll("[_]+$", "").replaceAll("___", "_");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "node[" + m_agent.getNodeId() + "]." + getResourceTypeName() + "[" + getInstance() +"]";
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
