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
 * 
 * 2007 Jul 24: Organize imports, Java 5 generics, format code. - dj@opennms.org
 * 
 *              
 * Created: July 27, 2006
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
package org.opennms.web.svclayer.catstatus.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
 * <p>DefaultCategoryStatusService class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultCategoryStatusService implements CategoryStatusService {
	private ViewDisplayDao m_viewDisplayDao;
	private CategoryConfigDao m_categoryConfigDao;
	private OutageDao m_outageDao;
	
	/**
	 * <p>getCategoriesStatus</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusSection> getCategoriesStatus() {
		View view = m_viewDisplayDao.getView();

		Collection<Section> sections = getSectionsForView(view);
		
        Collection<StatusSection> statusSections = new ArrayList<StatusSection>();
		for (Section section : sections) {
			statusSections.add(createSection(section));
		}
		
		return statusSections;
	}

	private StatusSection createSection(Section section) {
		StatusSection statusSection = new StatusSection();
		
		statusSection.setName(section.getSectionName());
			
		Collection<String> categories = getCategoriesForSection(section);
		
		for (String category : categories) {
			StatusCategory statusCategory = createCategory(category);
			statusSection.addCategory(statusCategory);
		}
			
		return statusSection;
	}

	private StatusCategory createCategory(String category) {
		Collection<OnmsOutage> outages; 
		
		CategoryBuilder categoryBuilder = new CategoryBuilder();
		
		StatusCategory statusCategory = new StatusCategory();
		Category categoryDetail =  m_categoryConfigDao.getCategoryByLabel(category);
		
		//statusCategory.setComment(categoryDetail.getCategoryComment());	
		statusCategory.setLabel(category);
				
		ServiceSelector selector = new ServiceSelector(categoryDetail.getRule(), getServicesForCategory(categoryDetail));
		outages = m_outageDao.matchingCurrentOutages(selector);
		
		for (OnmsOutage outage : outages) {
			OnmsMonitoredService monitoredService = outage.getMonitoredService();
			OnmsServiceType serviceType = monitoredService.getServiceType();
			OnmsIpInterface ipInterface = monitoredService.getIpInterface();

			
			categoryBuilder.addOutageService(
					monitoredService.getNodeId(), 
					ipInterface.getIpAddressAsString(), 
					ipInterface.getIpAddressAsString(), 
					ipInterface.getNode().getLabel(), 
					serviceType.getName());
		
		}
		
		for (StatusNode node : categoryBuilder.getNodes()) {
			statusCategory.addNode(node);
		}

		return statusCategory;
	
		
		
	}

	/**
	 * <p>setViewDisplayDao</p>
	 *
	 * @param viewDisplayDao a {@link org.opennms.web.svclayer.dao.ViewDisplayDao} object.
	 */
	public void setViewDisplayDao(ViewDisplayDao viewDisplayDao){	
		m_viewDisplayDao = viewDisplayDao;
	}

	/**
	 * <p>setCategoryConfigDao</p>
	 *
	 * @param categoryDao a {@link org.opennms.web.svclayer.dao.CategoryConfigDao} object.
	 */
	public void setCategoryConfigDao(CategoryConfigDao categoryDao){
		m_categoryConfigDao = categoryDao;
	}

	/**
	 * <p>setOutageDao</p>
	 *
	 * @param outageDao a {@link org.opennms.netmgt.dao.OutageDao} object.
	 */
	public void setOutageDao(OutageDao outageDao) {
		m_outageDao = outageDao;
	}

    private List<Section> getSectionsForView(View view) {
        return view.getSectionCollection();
    }

    private List<String> getCategoriesForSection(Section section) {
        return section.getCategoryCollection();
    }

    private List<String> getServicesForCategory(Category categoryDetail) {
        return categoryDetail.getServiceCollection();
    }
}
