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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;


public interface OutageDao extends OnmsDao {

    public abstract OnmsOutage load(Integer id);

    public abstract void save(OnmsOutage outage);

    public abstract void update(OnmsOutage outage);

    public abstract void saveOrUpdate(OnmsOutage outage);

    public abstract Collection<OnmsOutage> findAll();

    public abstract Integer currentOutageCount();

    public abstract Integer currentSuppressedOutageCount();

    public abstract Collection<OnmsOutage> currentOutages();
    
    public abstract Collection<OnmsOutage> matchingCurrentOutages(ServiceSelector selector);
    
    public abstract Collection<OnmsOutage> suppressedOutages();

    public abstract Collection<OnmsOutage> openAndResolvedOutages();

    public abstract Collection<OnmsOutage> currentOutages(Integer offset, Integer limit, String orderBy, String direction);

    public abstract Collection<OnmsOutage> suppressedOutages(Integer offset, Integer limit);

    public abstract Collection<OnmsOutage> findAll(Integer offset, Integer limit);

	public abstract Collection<OnmsOutage> currentOutages(String orderBy);

	public abstract Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String order, String direction);
	
	public abstract Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String order, String direction, String filter);

	public abstract Integer outageCount();
	
	public abstract Integer outageCountFiltered(String filter);

}
