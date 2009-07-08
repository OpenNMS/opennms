/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.collectd;

import java.util.Map;

import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;

/**
 * The Collector class.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * 
 */
public interface ServiceCollector {
    /**
     * Status of the collector object.
     */
    public static final int COLLECTION_UNKNOWN = 0;

    public static final int COLLECTION_SUCCEEDED = 1;

    public static final int COLLECTION_FAILED = 2;

    public static final String[] statusType = {
        "Unknown",
        "COLLECTION_SUCCEEDED",
        "COLLECTION_FAILED"
        };

    public void initialize(Map<String, String> parameters);

    public void release();

    public void initialize(CollectionAgent agent, Map<String, String> parameters);

    public void release(CollectionAgent agent);

    /**
     * Invokes a collection on the object.
     */
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) throws CollectionException;

    public RrdRepository getRrdRepository(String collectionName);
}
