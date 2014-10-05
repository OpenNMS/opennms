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

package org.opennms.netmgt.capsd.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>CapsdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CapsdMBean extends BaseOnmsMBean {
    
    /**
     * @return The approximate current number of active threads in the
     *         new-suspect scan thread pool
     */
    long getActiveSuspectThreads();
    
    /**
     * @return The approximate current number of active threads in the
     *         rescan thread pool
     */
    long getActiveRescanThreads();
    
    /**
     * @return The approximate total number of tasks completed by the
     *         new-suspect scan thread pool since Capsd was last started
     */
    long getSuspectCompletedTasks();
    
    /**
     * @return The approximate total number of tasks completed by the
     *         rescan thread pool since Capsd was last started
     */
    long getRescanCompletedTasks();
    
    /**
     * @return The approximate total number of tasks delegated to the
     *         new-suspect scan thread pool since Capsd was last started
     */
    long getSuspectTotalTasks();
    
    /**
     * @return The approximate total number of tasks delegated to the
     *         rescan thread pool since Capsd was last started
     */
    long getRescanTotalTasks();
    
    /**
     * @return The approximate ratio of tasks completed to tasks delegated
     *         for the new-suspect scan thread pool since Capsd was last
     *         started
     */
    double getSuspectTaskCompletionRatio();
    
    /**
     * @return The approximate ratio of tasks completed to tasks delegated
     *         for the rescan thread pool since Capsd was last started
     */
    double getRescanTaskCompletionRatio();
    
    long getSuspectQueueSize();
    
    long getRescanQueueSize();
    
    
}
