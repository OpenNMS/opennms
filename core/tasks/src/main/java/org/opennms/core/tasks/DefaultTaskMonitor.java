/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

/**
 * DefaultTaskMonitor
 *
 * @author brozow
 */
public class DefaultTaskMonitor implements TaskMonitor {

    public DefaultTaskMonitor(Task task) {
    }

    public void completed(Task task) {
        log("completed(%s)", task);
    }

    public void prerequisiteAdded(Task monitored, Task prerequsite) {
        log("prerequisiteAdded(%s, %s)", monitored, prerequsite);
    }

    public void prerequisiteCompleted(Task monitored, Task prerequisite) {
        log("prerequisiteCompleted(%s, %s)", monitored, prerequisite);
    }

    public void scheduled(Task task) {
        log("scheduled(%s)", task);
    }

    public void started(Task task) {
        log("started(%s)", task);
    }

    public void submitted(Task task) {
        log("submitted(%s)", task);
    }

    public void monitorException(Throwable t) {
        log(t, "monitorException(%s)", t);
    }
    
    public TaskMonitor getChildTaskMonitor(Task task, Task child) {
        return this;
    }

    private void log(String format, Object... args) {
        //String msg = String.format("%1$tY-%1$tm-%1$td %1$tT,%1$tL : [%2$s] MONITOR: %3$s", System.currentTimeMillis(), Thread.currentThread(), String.format(format, args));
        //System.err.println(msg);
    }
    
    private void log(Throwable t, String format, Object... args) {
        //log(format, args);
        //t.printStackTrace();
    }

}
