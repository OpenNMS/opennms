/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.catstatus.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.dao.api.OutageDao;
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
        @Override
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

			final String ipAddress = InetAddressUtils.str(ipInterface.getIpAddress());
			categoryBuilder.addOutageService(
					monitoredService.getNodeId(), 
					ipAddress, 
					ipAddress, 
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
	 * @param outageDao a {@link org.opennms.netmgt.dao.api.OutageDao} object.
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
