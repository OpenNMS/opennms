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
    boolean addService(String packageName, String svcName, int interval, Map<?,?> parameters);
    
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
    Map<?,?> getServiceParameters(String packageName, String svcName);

}
