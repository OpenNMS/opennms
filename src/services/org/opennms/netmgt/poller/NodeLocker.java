//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.poller;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public abstract class NodeLocker {

    private PollerNode m_node;

    private String m_caller;

    /**
     * Integer constant for passing in to PollableNode.getNodeLock() method in
     * order to indicate that the method should block until node lock is
     * available.
     */
    static final int WAIT_FOREVER = 0;

    public NodeLocker(PollerNode node, String caller) {
        m_node = node;
        m_caller = caller;
    }

    public void lockAndProcess() {
        // acquire lock to 'PollableNode'

        Category log = ThreadCategory.getInstance(getClass());
        boolean ownLock = false;
        try {
            // Attempt to obtain node lock...wait as long as it takes.
            //
            if (log.isDebugEnabled())
                log.debug(m_caller + ": Trying to get node lock for nodeId " + m_node.getNodeId());

            ownLock = m_node.getNodeLock(NodeLocker.WAIT_FOREVER);
            if (ownLock) {
                if (log.isDebugEnabled())
                    log.debug(m_caller + ": obtained node lock for nodeid: " + m_node.getNodeId());

                process();
            } else {
                // failed to acquire lock
                log.error(m_caller + ": failed to obtain lock on nodeId " + m_node.getNodeId());
                handleLockFailed();
            }
        } catch (InterruptedException iE) {
            // failed to acquire lock
            log.error(m_caller + ": thread interrupted...failed to obtain lock on nodeId " + m_node.getNodeId());
            handleException(iE);
        } catch (Throwable t) {
            log.error(m_caller+": unexpected exception.", t);
            handleException(t);
        } finally {
        
            if (ownLock) {
                if (log.isDebugEnabled())
                    log.debug(m_caller + ": releasing node lock for nodeid: " + m_node.getNodeId());
                try {
                    m_node.releaseNodeLock();
                } catch (InterruptedException iE) {
                    log.error(m_caller + ": thread interrupted...failed to release lock on nodeId " + m_node.getNodeId());
                    handleException(iE);
                }
            }
        }
    }

    /**
     * 
     */
    protected void handleLockFailed() {
        // TODO Auto-generated method stub
        
    }

    protected void handleException(Throwable t) {
        // TODO Auto-generated method stub
        
    }

    abstract protected void process();
}