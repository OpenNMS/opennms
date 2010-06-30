//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 23: Suppress warnings for unused code. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.secret.service.impl;

import java.util.HashSet;

import org.opennms.secret.dao.ServiceDao;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.NodeInterface;
import org.opennms.secret.service.ServiceService;

/**
 * <p>ServiceServiceImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class ServiceServiceImpl implements ServiceService {
    @SuppressWarnings("unused")
    private ServiceDao m_serviceDao;
    
    private String[] s_serviceNames = new String[] { "ICMP", "HTTP", "DNS", "SSH", "HTTPS" };
    
    /** {@inheritDoc} */
    public void setServiceDao(ServiceDao serviceDao) {
        m_serviceDao = serviceDao;
    }
    
    /** {@inheritDoc} */
    public HashSet<InterfaceService> getServices(NodeInterface iface) {
		HashSet<InterfaceService> services = new HashSet<InterfaceService>();
		for (int i = 0; i < 5; i++) {
			InterfaceService service = new InterfaceService();
			service.setId(new Long(i));
			service.setIpAddr("1.1.1." + (i + 1));
            service.setServiceName(s_serviceNames[i]);
			services.add(service);
		}
		return services;
	}

}
