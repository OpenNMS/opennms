//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.poller.Package;

/**
 * Represents a collection of nodes each with interfaces and services
 */
public class PollableNetwork {

    private Poller m_poller;
    /**
     * Map of 'PollableNode' objects keyed by nodeId
     */
    private Map m_pollableNodes;
    private List m_pollableServices;
    
    public PollableNetwork(Poller poller) {
        m_poller = poller;
        m_pollableNodes = Collections.synchronizedMap(new HashMap());
        m_pollableServices = Collections.synchronizedList(new ArrayList());
    }

    /**
     * @param pNode
     * @param poller
     */
    public void addNode(PollableNode pNode) {
        m_pollableNodes.put(new Integer(pNode.getNodeId()), pNode);
        Category log = ThreadCategory.getInstance(getClass());
        log.debug("PollableNetwork.addNode: adding pollable node with id: " + pNode.getNodeId() + " new size: " + m_pollableNodes.size());
    }

    /**
     * @param nodeId
     * @param poller
     * @return
     */
    public PollableNode findNode(int nodeId) {
        Integer key = new Integer(nodeId);
        if (m_pollableNodes.containsKey(key)) {
            return (PollableNode) m_pollableNodes.get(key);
        } else {
            return null;
        }
    }

    /**
     * @param nodeId
     * @param poller
     */
    public void removeNode(int nodeId) {
        m_pollableNodes.remove(new Integer(nodeId));
    }
    
    public void visit(PollableVisitor v) {
        Iterator j = m_pollableNodes.values().iterator();
        while (j.hasNext()) {
            PollableNode pNode = (PollableNode) j.next();
            v.visitNode(pNode);
            Iterator k = pNode.getInterfaces().iterator();
            while (k.hasNext()) {
                PollableInterface pIf = (PollableInterface) k.next();
                v.visitInterface(pIf);
                Iterator s = pIf.getServices().iterator();
                while (s.hasNext()) {
                    PollableService pSvc = (PollableService) s.next();
                    v.visitService(pSvc);
                }
            }
        }
        
    }

    /**
     * @param poller
     */
    public void dumpNetwork() {
        final Category log = ThreadCategory.getInstance(getClass());
        
        PollableVisitor dumper = new PollableVisitor() {
            public void visitNode(PollableNode pNode) {
                log.debug(" nodeid=" + pNode.getNodeId() + " status=" + Pollable.statusType[pNode.getStatus()]);
            }
            
            public void visitInterface(PollableInterface pIf) {
                log.debug("     interface=" + pIf.getAddress().getHostAddress() + " status=" + Pollable.statusType[pIf.getStatus()]);
            }
            
            public void visitService(PollableService pSvc) {
                log.debug("         service=" + pSvc.getServiceName() + " status=" + Pollable.statusType[pSvc.getStatus()]);
            }
        };
        visit(dumper);
    
    }

    public PollableService createPollableService(int nodeId, String ipAddr, String svcName, Package pkg, int lastKnownStatus, Date svcLostDate) throws InterruptedException, UnknownHostException {
        Category log = ThreadCategory.getInstance();
        PollableService pSvc;
        PollableNode pNode = null;
        boolean ownLock = false;
        try {
            PollableInterface pInterface = null;
            boolean nodeCreated = false;
            boolean interfaceCreated = false;
    
            // Does the node already exist in the poller's
            // pollable node map?
            //
            pNode = findNode(nodeId);
            if (pNode == null) {
                // Nope...so we need to create it
                pNode = new PollableNode(nodeId, m_poller);
                nodeCreated = true;
            } else {
                // Obtain node lock
                //
                ownLock = pNode.getNodeLock(Poller.WAIT_FOREVER);
            }
    
            // Does the interface exist in the pollable
            // node?
            //
            pInterface = pNode.findInterface(ipAddr);
            if (pInterface == null) {
                // Create the PollableInterface
                pInterface = new PollableInterface(pNode, InetAddress.getByName(ipAddr));
                interfaceCreated = true;
            }
    
            // Create a new PollableService representing
            // this node, interface,
            // service and package pairing
            //
            pSvc = new PollableService(pInterface, svcName, pkg, lastKnownStatus, svcLostDate);
    
            // Add the service to the PollableInterface
            // object
            //
            // WARNING: The PollableInterface stores
            // services in a map
            // keyed by service name, therefore, only the
            // LAST
            // PollableService aded to the interface for a
            // particular service will be represented in the
            // map. THIS IS BY DESIGN.
            //
            // NOTE: addService() calls recalculateStatus()
            // on the interface
            log.debug("createPollableService: adding pollable service to service list of interface: " + ipAddr);
            pInterface.addService(pSvc);
    
            if (interfaceCreated) {
                // Add the interface to the node
                //
                // NOTE: addInterface() calls
                // recalculateStatus() on the node
                if (log.isDebugEnabled())
                    log.debug("createPollableService: adding new pollable interface " + ipAddr + " to pollable node " + nodeId);
                pNode.addInterface(pInterface);
            } else {
                // Recalculate node status
                //
                pNode.recalculateStatus();
            }
    
            if (nodeCreated) {
                // Add the node to the node map
                //
                if (log.isDebugEnabled())
                    log.debug("createPollableService: adding new pollable node: " + nodeId);
                addNode(pNode);
            }
            
            // Add new service to the pollable services
            // list.
            //
            m_pollableServices.add(pSvc);


        } finally {
            if (ownLock) {
                try {
                    pNode.releaseNodeLock();
                } catch (InterruptedException iE) {
                    log.error("createPollableService: Failed to release node lock on nodeid " + pNode.getNodeId() + ", thread interrupted.");
                }
            }
    
        }
        return pSvc;
    }

    /**
     * @return
     */
    public List getPollableServices() {
        return m_pollableServices;
    }

}
