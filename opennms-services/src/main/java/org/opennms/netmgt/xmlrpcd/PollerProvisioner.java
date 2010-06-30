/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 13, 2005
 * 
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.xmlrpcd;

import java.util.Map;

/**
 * <p>PollerProvisioner interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollerProvisioner {
    
    /**
     * <p>addMonitor</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param className a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean addMonitor(String svcName, String className);
    
    /**
     * <p>addPackage</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param downtimeInterval a int.
     * @param downtimeDuration a int.
     * @return a boolean.
     */
    boolean addPackage(String packageName, int downtimeInterval, int downtimeDuration);
    
    /**
     * <p>getDowntimeInterval</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @return a int.
     */
    int getDowntimeInterval(String packageName);
    
    /**
     * <p>getDowntimeDuration</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @return a int.
     */
    int getDowntimeDuration(String packageName);
    
    /**
     * <p>addService</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param interval a int.
     * @param parameters a {@link java.util.Map} object.
     * @return a boolean.
     */
    boolean addService(String packageName, String svcName, int interval, Map parameters);
    
    /**
     * <p>getServiceInterval</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a int.
     */
    int getServiceInterval(String packageName, String svcName);
    
    /**
     * <p>getServiceParameters</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    Map getServiceParameters(String packageName, String svcName);

}
