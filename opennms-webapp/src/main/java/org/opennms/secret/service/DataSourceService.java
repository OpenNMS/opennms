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
/**
 * 
 */
package org.opennms.secret.service;

import java.util.List;

import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;

/**
 * <p>DataSourceService interface.</p>
 *
 * @author Ted Kaczmarek
 * @author DJ Gregor
 * @version $Id: $
 * @since 1.6.12
 */
public interface DataSourceService {

	/**
	 * <p>getDataSourcesByInterface</p>
	 *
	 * @param iface a {@link org.opennms.secret.model.NodeInterface} object.
	 * @return a {@link java.util.List} object.
	 */
	public abstract List getDataSourcesByInterface(NodeInterface iface);
    /**
     * <p>getDataSourceByService</p>
     *
     * @param service a {@link org.opennms.secret.model.InterfaceService} object.
     * @return a {@link org.opennms.secret.model.DataSource} object.
     */
    public abstract DataSource getDataSourceByService(InterfaceService service);
	/**
	 * <p>getDataSourcesByNode</p>
	 *
	 * @param node a {@link org.opennms.secret.model.Node} object.
	 * @return a {@link java.util.List} object.
	 */
	public abstract List getDataSourcesByNode(Node node);
    /**
     * <p>getDataSourceById</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.opennms.secret.model.DataSource} object.
     */
    public abstract DataSource getDataSourceById(String id);
	
}
