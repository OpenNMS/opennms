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

package org.opennms.netmgt.dao.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class RrdTestUtils {
    // Reference the class name this way so that it is refactoring resistant
    private static final String RRD_CONFIG = "org.opennms.rrd.strategyClass=" + JRobinRrdStrategy.class.getName()
        + "\norg.opennms.rrd.usequeue=false";
    // Reference the class name this way so that it is refactoring resistant
    private static final String RRD_NULL_STRATEGY_CONFIG = "org.opennms.rrd.strategyClass=" + NullRrdStrategy.class.getName()
        + "\norg.opennms.rrd.usequeue=false";

    /**
     * This class cannot be instantiated.  Use static methods.
     */
    private RrdTestUtils() {
        
    }
    
    public static void initialize() throws IOException, RrdException {
        RrdConfig.loadProperties(new ByteArrayInputStream(RRD_CONFIG.getBytes()));
        RrdUtils.initialize();
    }
    
    public static void initializeNullStrategy() throws IOException, RrdException {
        RrdConfig.loadProperties(new ByteArrayInputStream(RRD_NULL_STRATEGY_CONFIG.getBytes()));
        RrdUtils.initialize();
    }
    
    
}
