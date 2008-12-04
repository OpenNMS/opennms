/*
 * This file is part of the OpenNMS(R) Application. OpenNMS(R) is Copyright
 * (C) 2008 The OpenNMS Group, Inc. All rights reserved. OpenNMS(R) is a
 * derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified and included code are below. OpenNMS(R) is a registered
 * trademark of The OpenNMS Group, Inc. Original code base Copyright (C)
 * 1999-2001 Oculan Corp. All rights reserved. This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place - Suite 330, Boston, MA 02111-1307, USA. For more information
 * contact: OpenNMS Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service.tasks;

import java.util.ArrayList;
import java.util.List;

/*
 * ContainerTask
 * @author brozow
 */
public class ContainerTask extends Task {

    protected Task m_triggerTask;
    protected List<Task> m_children = new ArrayList<Task>();
    protected String m_childPreferredExecutor = DEFAULT_EXECUTOR;
    
    public ContainerTask(DefaultTaskCoordinator coordinator) {
        super(coordinator);
        super.setPreferredExecutor(ADMIN_EXECUTOR);
        m_triggerTask = coordinator.createTask(new Runnable() {
            public void run() {};
            public String toString() { return "Trigger For "+ContainerTask.this; }
        });
        m_triggerTask.setPreferredExecutor(ADMIN_EXECUTOR);
    }

    @Override
    public void addPrerequisite(Task task) {
        super.addPrerequisite(task);
        m_triggerTask.addPrerequisite(task);
    }

    @Override
    public void schedule() {
        m_triggerTask.schedule();
        for(Task task : m_children) {
            task.schedule();
        }
        m_children.clear();
        super.schedule();
    }
    
    protected String getChildPreferredExecutor() {
        return m_childPreferredExecutor;
    }
    
    @Override
    public void setPreferredExecutor(String preferredExecutor) {
        m_childPreferredExecutor = preferredExecutor;
        for(Task task : m_children) {
            setPreferredExecutorOfChild(task);
        }
    }

    protected Task getTriggerTask() {
        return m_triggerTask;
    }

    public void add(Task task) {
        super.addPrerequisite(task);
        addChildDependencies(task);
        setPreferredExecutorOfChild(task);
        if (isScheduled()) {
            task.schedule();
        } else {
            m_children.add(task);
        }
    }

    private void setPreferredExecutorOfChild(Task task) {
        if (task instanceof ContainerTask) {
            ContainerTask container = (ContainerTask)task;
            if (container.getChildPreferredExecutor().equals(DEFAULT_EXECUTOR)) {
                container.setPreferredExecutor(getChildPreferredExecutor());
            }
        } else {
            if (task.getPreferredExecutor().equals(DEFAULT_EXECUTOR)) {
                task.setPreferredExecutor(getChildPreferredExecutor());
            }
        }
    }
    
    public void add(Runnable runnable) {
        add(getCoordinator().createTask(runnable));
    }

    protected void addChildDependencies(Task child) {
        child.addPrerequisite(m_triggerTask);
    }

}