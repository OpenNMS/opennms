/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd.scheduler;

/**
 * This interface extends the {@link java.lang.Runnable runnable}interface and
 * provides a method to determine if the runnable is ready to start.
 *
 * @author <a href="mailto:antonio@opennms.org">Antonio Russo</a>
 */
public interface ReadyRunnable extends Runnable {
    /**
     * Returns true if the runnable is ready to start.
     *
     * @return a boolean.
     */
    public boolean isReady();

    /**
     * <p>suspend</p>
     */
    public void suspend();
    
    /**
     * <p>isSuspended</p>
     *
     * @return a boolean.
     */
    public boolean isSuspended();
    
    /**
     * <p>wakeUp</p>
     */
    public void wakeUp();
    
    /**
     * <p>unschedule</p>
     */
    public void unschedule();
    
    /**
     * <p>schedule</p>
     */
    public void schedule();
    
    /**
     * <p>getInfo</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInfo();
    
    /**
     * <p>getPackageName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName();
    
    /**
     * <p>setPackageName</p>
     *
     * @param pkg a {@link java.lang.String} object.
     */
    public void setPackageName(String pkg);

    
}
