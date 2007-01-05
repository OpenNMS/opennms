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
package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsOutage;

public interface OutageService {

    Collection<OnmsOutage> getCurrentOutages();
    
    Collection<OnmsOutage> getCurrentOutagesByRange(Integer offset, Integer oimit, String orderProperty, String direction);

    Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);
    
    Collection<OnmsOutage> getSuppressedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction);    
    
    Collection<OnmsOutage> getSuppressedOutages();

    Integer getCurrentOutageCount() ;

    Integer getSuppressedOutageCount() ;

    Collection<OnmsOutage> getCurrentOutagesForNode(int nodeId)
            ;

    Collection<OnmsOutage> getNonCurrentOutagesForNode(int nodeId)
            ;

    Collection<OnmsOutage> getOutagesForNode(int nodeId) ;

    Collection<OnmsOutage> getOutagesForNode(int nodeId, Date time)
            ;

    Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipInterface)
            ;

    Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipAddr, Date time) ;

    Collection<OnmsOutage> getOutagesForService(int nodeId, String ipInterface, int serviceId) ;
    
    Collection<OnmsOutage>  getOutagesForService(int nodeId, String ipAddr, int serviceId, Date time) ;

	Collection<OnmsOutage> getCurrentOutages(String orderProperty);

	OnmsOutage load(Integer outageid);
	
	void update(OnmsOutage outage);
	
	Integer getOutageCount() ;
	
	Integer outageCountFiltered(String filter);

	Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);

	Integer outageResolvedCountFiltered(String searchFilter);
	
    // This we may have to define 
    /*
    OutageSummary[] getCurrentOutageSummaries() ;

    OutageSummary[] getCurrentSDSOutageSummaries() ;
    */
    
}
