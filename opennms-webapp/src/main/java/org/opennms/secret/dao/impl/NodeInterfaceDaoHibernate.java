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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.secret.dao.impl;

import java.util.Collection;

import org.opennms.secret.dao.NodeInterfaceDao;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;
/**
 * <p>NodeInterfaceDaoHibernate class.</p>
 *
 * @author david
 * @version $Id: $
 * @since 1.6.12
 */
public class NodeInterfaceDaoHibernate implements NodeInterfaceDao {

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#initialize(java.lang.Object)
     */
    /** {@inheritDoc} */
    public void initialize(Object obj) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#getNodeInterface(java.lang.Long)
     */
    /** {@inheritDoc} */
    public NodeInterface getNodeInterface(Long interfaceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#getNodeInterfaces(org.opennms.secret.model.Node)
     */
    /** {@inheritDoc} */
    public Collection getNodeInterfaces(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#createInterface(org.opennms.secret.model.NodeInterface)
     */
    /** {@inheritDoc} */
    public void createInterface(NodeInterface iface) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeInterfaceDao#getServiceCollection(org.opennms.secret.model.NodeInterface)
     */
    /** {@inheritDoc} */
    public Collection getServiceCollection(NodeInterface ni) {
        // TODO Auto-generated method stub
        return null;
    }

}
