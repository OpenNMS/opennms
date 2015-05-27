/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import java.util.Map;

import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * The Collector class.
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public interface ServiceCollector {
    /**
     * Status of the collector object.
     */
    static final int COLLECTION_UNKNOWN = 0;

    /** 
     * Constant <code>COLLECTION_SUCCEEDED=1</code> 
     */
    static final int COLLECTION_SUCCEEDED = 1;

    /** 
     * Constant <code>COLLECTION_FAILED=2</code> 
     */
    static final int COLLECTION_FAILED = 2;

    /** 
     * Constant <code>statusType="{Unknown,COLLECTION_SUCCEEDED,COLLECTIO"{trunked}</code> 
     */
    static final String[] statusType = {
        "Unknown",
        "COLLECTION_SUCCEEDED",
        "COLLECTION_FAILED"
    };

    /**
     * <p>initialize</p>
     *
     * @param parameters a {@link java.util.Map} object.
     */
    void initialize(Map<String, String> parameters) throws CollectionInitializationException;

    /**
     * <p>release</p>
     */
    void release();

    /**
     * <p>initialize</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param parameters a {@link java.util.Map} object.
     */
    void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException;

    /**
     * <p>release</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    void release(CollectionAgent agent);

    /**
     * Invokes a collection on the object.
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param eproxy a {@link org.opennms.netmgt.events.api.EventProxy} object.
     * @param parameters a {@link java.util.Map} object.
     * @return a {@link org.opennms.netmgt.config.collector.CollectionSet} object.
     * @throws org.opennms.netmgt.collectd.CollectionException if any.
     */
    CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException;

    /**
     * <p>getRrdRepository</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    RrdRepository getRrdRepository(String collectionName);
}
