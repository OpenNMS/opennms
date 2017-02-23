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

package org.opennms.netmgt.vmmgr;

import java.util.List;

public interface ManagerMBean {

    /**
     * <p>dumpThreads</p>
     */
    public void dumpThreads();

    /**
     * <p>status</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> status();

    /**
     * <p>stop</p>
     */
    public void stop();

    /**
     * <p>doSystemExit</p>
     */
    public void doSystemExit();
    
    /**
     * <p>doTestLoadLibraries</p>
     */
    public void doTestLoadLibraries();
    
    /**
     * <p>getUptime</p>
     * 
     * @return a {@link java.lang.Long} expressing the time, in milliseconds,
     * since the manager first started 
     */
    public Long getUptime();
}
