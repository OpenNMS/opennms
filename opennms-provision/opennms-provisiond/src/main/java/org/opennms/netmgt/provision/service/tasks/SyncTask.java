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

package org.opennms.netmgt.provision.service.tasks;

/**
 * SyncTask
 *
 * @author brozow
 */
public class SyncTask extends Task {
    
    public static final String DEFAULT_EXECUTOR = "default";
    public static final String ADMIN_EXECUTOR = "admin";
    
    private final Runnable m_action;

    private String m_preferredExecutor = DEFAULT_EXECUTOR;
    
    public SyncTask(DefaultTaskCoordinator coordinator, ContainerTask parent, Runnable action) {
        this(coordinator, parent, action, DEFAULT_EXECUTOR);
    }


    public SyncTask(DefaultTaskCoordinator coordinator, ContainerTask parent, Runnable action, String preferredExecutor) {
        super(coordinator, parent);
        m_action = action;
        m_preferredExecutor = preferredExecutor;
    }

    @Override
    protected void doSubmit() {
        submitRunnable(getRunnable(), getPreferredExecutor());
    }

    /**
     * This is the run method where the 'work' related to the Task gets down.  This method can be overridden
     * or a Runnable can be passed to the task in the constructor.  The Task is not complete until this method
     * finishes
     */
    public void run() {
        if (m_action != null) {
            m_action.run();
        }
    }
    
    /**
     * This method is used by the TaskCoordinator to create runnable that will run this task
     */
    final Runnable getRunnable() {
        return new Runnable() {
          public void run() {
              SyncTask.this.run();
          }
          public String toString() { return "Runner for "+SyncTask.this; }
        };
    }

    public String getPreferredExecutor() {
        return m_preferredExecutor;
    }

    public void setPreferredExecutor(String preferredExecutor) {
        m_preferredExecutor = preferredExecutor;
    }

    public String toString() {
        return m_action == null ? super.toString() : m_action.toString();
    }


}
