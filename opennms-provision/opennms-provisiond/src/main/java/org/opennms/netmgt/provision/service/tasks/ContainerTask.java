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
 * 
 * TODO derive directly from Task
 */
public class ContainerTask extends Task {

    /**
     * TaskTrigger
     *
     * @author brozow
     */
    private final class TaskTrigger extends Task {
        public TaskTrigger(DefaultTaskCoordinator coordinator, ContainerTask parent) {
            super(coordinator, parent);
        }


        @Override
        protected void completeSubmit() {
            getCoordinator().markTaskAsCompleted(TaskTrigger.this);
        }


        public String toString() { return "Trigger For "+ContainerTask.this; }
    }

    protected final Task m_triggerTask;
    protected final List<Task> m_children = new ArrayList<Task>();
    
    public ContainerTask(DefaultTaskCoordinator coordinator, ContainerTask parent) {
        super(coordinator, parent);
        m_triggerTask = new TaskTrigger(coordinator, this);

    }

    @Override
    public void addPrerequisite(Task task) {
        super.addPrerequisite(task);
        m_triggerTask.addPrerequisite(task);
    }

    @Override
    public void preSchedule() {
        m_triggerTask.schedule();
        synchronized (m_children) {
            for(Task task : m_children) {
                task.schedule();
            }
            m_children.clear();
        }
    }
    
    protected Task getTriggerTask() {
        return m_triggerTask;
    }

    public void add(Task task) {
        super.addPrerequisite(task);
        addChildDependencies(task);
        //setPreferredExecutorOfChild(task);
        if (isScheduled()) {
            task.schedule();
        } else {
            synchronized (m_children) {
                m_children.add(task);
            }
        }
    }

//    private void setPreferredExecutorOfChild(Task task) {
//        if (task instanceof ContainerTask) {
//            ContainerTask container = (ContainerTask)task;
//            if (container.getChildPreferredExecutor().equals(DEFAULT_EXECUTOR)) {
//                container.setPreferredExecutor(getChildPreferredExecutor());
//            }
//        } else if (task instanceof SyncTask){
//            SyncTask syncTask = (SyncTask)task;
//            if (syncTask.getPreferredExecutor().equals(DEFAULT_EXECUTOR)) {
//                syncTask.setPreferredExecutor(getChildPreferredExecutor());
//            }
//        }
//    }
    
    
    
    @Override
    protected void completeSubmit() {
        getCoordinator().markTaskAsCompleted(this);
    }

    public SyncTask add(Runnable runnable) {
        SyncTask task = createTask(runnable);
        add(task);
        return task;
    }

    public SyncTask add(Runnable runnable, String schedulingHint) {
        SyncTask task = createTask(runnable, schedulingHint);
        add(task);
        return task;
    }
    
    public <T> AsyncTask<T> add(Async<T> async, Callback<T> cb) {
        AsyncTask<T> task = createTask(async, cb);
        add(task);
        return task;
    }

    private SyncTask createTask(Runnable runnable) {
        return getCoordinator().createTask(this, runnable);
    }

    private SyncTask createTask(Runnable runnable, String schedulingHint) {
        return getCoordinator().createTask(this, runnable, schedulingHint);
    }
    
    private <T> AsyncTask<T> createTask(Async<T> async, Callback<T> cb) {
        return getCoordinator().createTask(this, async, cb);
    }

    protected void addChildDependencies(Task child) {
        child.addPrerequisite(m_triggerTask);
    }
}