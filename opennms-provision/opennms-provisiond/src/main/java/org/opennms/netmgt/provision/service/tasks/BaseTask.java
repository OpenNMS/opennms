/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BaseTask
 *
 * @author brozow
 */
public class BaseTask implements Callable<BaseTask> {
    
    private DefaultTaskCoordinator m_coordinator;
    private AtomicBoolean m_finished = new AtomicBoolean(false);
    
    public BaseTask(DefaultTaskCoordinator coordinator) {
        m_coordinator = coordinator;
    }
    
    public void run() {
        
    }

    public BaseTask call() throws Exception {
        try {
            run();
        } finally {
            m_finished.set(true);
        }
        
        return this;
    }
    

    public void schedule() {
        m_coordinator.schedule(this);
    }

    public void waitFor() throws InterruptedException, ExecutionException {
        m_coordinator.waitFor(this);
    }

    public void addDependency(BaseTask task1) {
        if (!task1.isFinished()) {
            m_coordinator.addDependency(task1, this);
        }
    }

    private boolean isFinished() {
        return m_finished.get();
    }

    public void waitFor(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        m_coordinator.waitFor(this, timeout, unit);
    }

    

}
