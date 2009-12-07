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
package org.opennms.core.tasks;

import org.opennms.core.utils.LogUtils;

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
              try {
                  SyncTask.this.run();
              } catch (Throwable t) {
                  LogUtils.debugf(this, t, "Exception occurred executing task %s", SyncTask.this);
              }
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
