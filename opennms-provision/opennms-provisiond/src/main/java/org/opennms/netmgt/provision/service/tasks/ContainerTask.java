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
public class ContainerTask extends BaseTask {

    protected BaseTask m_startTask;
    protected List<BaseTask> m_children = new ArrayList<BaseTask>();

    public ContainerTask(DefaultTaskCoordinator coordinator) {
        super(coordinator);
        m_startTask = new BaseTask(coordinator);
    }

    @Override
    public void addPrerequisite(BaseTask task) {
        super.addPrerequisite(task);
        m_startTask.addPrerequisite(task);
    }

    @Override
    public void schedule() {
        m_startTask.schedule();
        for(BaseTask task : m_children) {
            task.schedule();
        }
        super.schedule();
    }

    public void add(BaseTask task) {
        super.addPrerequisite(task);
        addChildDependencies(task);
        m_children.add(task);
    }

    protected void addChildDependencies(BaseTask child) {
        child.addPrerequisite(m_startTask);
    }

}