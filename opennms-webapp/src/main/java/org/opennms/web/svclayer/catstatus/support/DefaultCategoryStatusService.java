package org.opennms.web.svclayer.catstatus.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.web.svclayer.catstatus.dao.CategoryDao;
import org.opennms.web.svclayer.catstatus.CategoryStatusService;
import org.opennms.web.svclayer.catstatus.dao.ViewDisplayDao;
import org.opennms.web.svclayer.catstatus.model.StatusCategory;
import org.opennms.web.svclayer.catstatus.model.StatusInterface;
import org.opennms.web.svclayer.catstatus.model.StatusNode;
import org.opennms.web.svclayer.catstatus.model.StatusSection;
import org.opennms.web.svclayer.catstatus.model.StatusService;

public class DefaultCategoryStatusService implements CategoryStatusService {

	
	private ViewDisplayDao m_viewdisplaydao;
	private CategoryDao m_categorydao;
	private OutageDao m_outagedao;
	
	public Collection<StatusSection> getCategoriesStatus() {
		
		Collection <StatusSection> statusSections = new ArrayList<StatusSection>();
		View view = m_viewdisplaydao.getView();

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
		Category categoryDetail =  m_categorydao.getCategoryByLabel(category);
		
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
		m_viewdisplaydao = viewDisplayDao;
	}

	public void setCategoryDao(CategoryDao categoryDao){
		m_categorydao = categoryDao;
		
	}

	public void setOutageDao(OutageDao outageDao) {
	
		m_outagedao = outageDao;
	}
	
}
