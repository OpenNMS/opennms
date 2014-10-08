/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;


/**
 * <p>RancidAdapterConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface RancidAdapterConfig {
    
    /**
     * return the delay time for the specified address
     * the delay time is the time in msec that represents
     * a delay in the execution of a RancidAdapter
     * execution
     *
     * @param ipaddress
     *          the ipaddress of the node
     * @return the delay time
     */
    public abstract long getDelay(String ipaddress);

    /**
     * return the number of retries in case of failure
     * for the specified address
     *
     * @param ipaddress
     *          the ipaddress of the node
     * @return the number of retries
     */
    public abstract int getRetries(String ipaddress);
    
    /**
     * return the delay time for the specified address
     * the retrydelay time is the time in msec that represents
     * a delay in the execution of a RancidAdapter
     * execution retry after a failure
     * @param ipaddress
     *          the ipaddress of the node
     * @return the delay time for retry
     */
//    public abstract long getRetryDelay(String ipaddress);
       
    /**
     * return if is to be used the opennms categories to get
     * rancid device type
     *
     * @param ipaddress
     *          the ipaddress of the node
     * @return true if use opennms category
     */
    public abstract boolean useCategories(String ipaddress);

    /**
     * return the Rancid Type String
     *
     * @param sysoid
     *          the system OID identifier of the node
     * @return RancidType String
     */
    public abstract String getType(String sysoid, String sysdescr);  

    /**
     * Return if current time is ready to be scheduled.
     *
     * @param ipaddress
     *          the ipaddress of the node
     * @return true if current time is in a schedules under policy manage
     */
    public abstract boolean isCurTimeInSchedule(String ipaddress);     

}
