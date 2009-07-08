/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.config;


public interface RancidAdapterConfig {
    
    /**
     * return the delay time for the specified address
     * the delay time is the time in msec that represents
     * a delay in the execution of a RancidAdapter
     * execution
     * @param ipaddress
     *          the ipaddress of the node
     * @return the delay time
     */
    public abstract long getDelay(String ipaddress);

    /**
     * return the number of retries in case of failure
     * for the specified address
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
     * @param ipaddress 
     *          the ipaddress of the node
     * @return true if use opennms category
     */
    public abstract boolean useCategories(String ipaddress);

    /**
     * return the Rancid Type String
     * @param sysoid 
     *          the system OID identifier of the node
     * @return RancidType String
     */
    public abstract String getType(String sysoid);  

    /**
     * Return if current time is ready to be scheduled.
     * 
     * @param ipaddress
     *          the ipaddress of the node
     * 
     * @return true if current time is in a schedules under policy manage
     */
    public abstract boolean isCurTimeInSchedule(String ipaddress);     

}
