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

package org.opennms.web.svclayer.catstatus.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.web.svclayer.catstatus.CategoryStatusService;
import org.opennms.web.svclayer.catstatus.model.StatusCategory;
import org.opennms.web.svclayer.catstatus.model.StatusNode;
import org.opennms.web.svclayer.catstatus.model.StatusSection;
import org.opennms.web.svclayer.dao.CategoryConfigDao;
import org.opennms.web.svclayer.dao.ViewDisplayDao;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jason.aras@fastsearch.com">Jason Ayers</a>
 *
 */
public class DefaultCategoryStatusService implements CategoryStatusService {

	
	private ViewDisplayDao m_viewDisplayDao;
	private CategoryConfigDao m_categoryConfigDao;
	private OutageDao m_outagedao;
	
	public Collection<StatusSection> getCategoriesStatus() {
		
		Collection <StatusSection> statusSections = new ArrayList<StatusSection>();
		View view = m_viewDisplayDao.getView();

		Collection sections = view.getSectionCollection();
		
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			Section section = (Section) iter.next();
			StatusSection statusSection = createSection(section);
				
			statusSections.add(statusSection);
		}
		
		
		return statusSections;
	}

	private StatusSection createSection(Section section) {
		StatusSection statusSection = new StatusSection();
		
		statusSection.setName(section.getSectionName());
			
		Collection categories = section.getCategoryCollection();
		
		for (Iterator iter = categories.iterator(); iter.hasNext();){
			String category =  (String) iter.next();
			StatusCategory statusCategory = createCategory(category);
			statusSection.addCategory(statusCategory);
		}
			
		return statusSection;
	}


	private StatusCategory createCategory(String category) {

		Collection <OnmsOutage>outages; 
		
		CategoryBuilder categoryBuilder = new CategoryBuilder();
		
		StatusCategory statusCategory = new StatusCategory();
		Category categoryDetail =  m_categoryConfigDao.getCategoryByLabel(category);
		
		//statusCategory.setComment(categoryDetail.getCategoryComment());	
		statusCategory.setLabel(category);
		
				
		ServiceSelector selector = new ServiceSelector(categoryDetail.getRule(),categoryDetail.getServiceCollection());
		outages = m_outagedao.matchingCurrentOutages(selector);
		
		for (OnmsOutage outage : outages) {
			OnmsMonitoredService monitoredService = outage.getMonitoredService();
			OnmsServiceType serviceType = monitoredService.getServiceType();
			OnmsIpInterface ipInterface = monitoredService.getIpInterface();

			
			categoryBuilder.addOutageService(
					monitoredService.getNodeId(), 
					ipInterface.getIpAddress(), 
					ipInterface.getIpAddress(), 
					ipInterface.getNode().getLabel(), 
					serviceType.getName());
		
		}
		
		for (StatusNode node : categoryBuilder.getNodes()) {
			statusCategory.addNode(node);
		}

		return statusCategory;
	
		
		
	}

		
	public void setViewDisplayDao(ViewDisplayDao viewDisplayDao){	
		m_viewDisplayDao = viewDisplayDao;
	}

	public void setCategoryConfigDao(CategoryConfigDao categoryDao){
		m_categoryConfigDao = categoryDao;
		
	}

	public void setOutageDao(OutageDao outageDao) {
	
		m_outagedao = outageDao;
	}
	
}
