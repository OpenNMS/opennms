/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 2007 Feb 01: Indent, add methods to support searches with OnmsCriteria. - dj@opennms.org
 * 
 * Created: August 16, 2006
 *
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional(readOnly = true)
public interface OutageService {

    public Collection<OnmsOutage> getCurrentOutages();
    
    public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, OnmsCriteria criteria);

    public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);
    
    public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction);    
    
    public Collection<OnmsOutage> getSuppressedOutages();

    public Integer getCurrentOutageCount();
    
    public Integer getOutageCount(OnmsCriteria criteria);

    public Integer getSuppressedOutageCount();

    public Collection<OnmsOutage> getCurrentOutagesForNode(int nodeId);

    public Collection<OnmsOutage> getNonCurrentOutagesForNode(int nodeId);

    public Collection<OnmsOutage> getOutagesForNode(int nodeId);

    public Collection<OnmsOutage> getOutagesForNode(int nodeId, Date time);

    public Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipInterface);

    public Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipAddr, Date time);

    public Collection<OnmsOutage> getOutagesForService(int nodeId, String ipInterface, int serviceId);
    
    public Collection<OnmsOutage>  getOutagesForService(int nodeId, String ipAddr, int serviceId, Date time);

    public Collection<OnmsOutage> getCurrentOutages(String orderProperty);

    public OnmsOutage load(Integer outageid);

    public void update(OnmsOutage outage);

    public Integer getOutageCount();

    public Integer outageCountFiltered(String filter);

    public Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);

    public Integer outageResolvedCountFiltered(String searchFilter);
	
    // This we may have to define 
    /*
    public OutageSummary[] getCurrentOutageSummaries() ;

    public OutageSummary[] getCurrentSDSOutageSummaries() ;
    */
    
}
