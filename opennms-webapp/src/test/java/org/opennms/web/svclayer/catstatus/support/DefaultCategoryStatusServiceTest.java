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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.web.svclayer.catstatus.model.StatusCategory;
import org.opennms.web.svclayer.catstatus.model.StatusNode;
import org.opennms.web.svclayer.catstatus.model.StatusSection;
import org.opennms.web.svclayer.dao.CategoryConfigDao;
import org.opennms.web.svclayer.dao.ViewDisplayDao;


public class DefaultCategoryStatusServiceTest extends TestCase {

	private DefaultCategoryStatusService categoryStatusService;
	private ViewDisplayDao viewDisplayDao;
	private CategoryConfigDao categoryDao;
	private OutageDao outageDao;
	
	protected void setUp() throws Exception {
		super.setUp();
		viewDisplayDao = createMock(ViewDisplayDao.class);
		categoryDao = createMock(CategoryConfigDao.class);
		outageDao = createMock(OutageDao.class);
		categoryStatusService = new DefaultCategoryStatusService();	 
		categoryStatusService.setViewDisplayDao(viewDisplayDao);
		categoryStatusService.setCategoryConfigDao(categoryDao);
		categoryStatusService.setOutageDao(outageDao);
	}

	
	public void testCategoryGroupsReturnedWhenNoneExist() {
		
		
		View view = new View();
		
		
		expect(viewDisplayDao.getView()).andReturn(view);
		replay(viewDisplayDao);
		
		Collection<StatusSection> categories = categoryStatusService.getCategoriesStatus();
	
		verify(viewDisplayDao);
		
		assertTrue("Collection Should Be Empty", categories.isEmpty());
	}
	
	
	public void testGetCategoriesStatus(){
	
		View view = new View();
		Section section = new Section();
		org.opennms.netmgt.config.views.Category category = new org.opennms.netmgt.config.views.Category();
		
		section.setSectionName("Section One");
		section.addCategory("Category One");
		
		category.setLabel("Category One");
		//category.setCategoryComment("Category One Comment");
	
		OnmsOutage outage = new OnmsOutage();
		Collection<OnmsOutage> outages = new ArrayList<OnmsOutage>();
		
		outage.setId(300);
		
		
		OnmsServiceType svcType = new OnmsServiceType();
		svcType.setId(3);
		svcType.setName("HTTP");
		OnmsNode node = new OnmsNode();
		node.setId(1);
		node.setLabel("superLabel");
		OnmsSnmpInterface snmpIface = new OnmsSnmpInterface("192.168.1.1", 1, node);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		iface.setSnmpInterface(snmpIface);
		//iface.setId(9);
		OnmsMonitoredService monSvc = new OnmsMonitoredService(iface, svcType);

		outage.setMonitoredService(monSvc);
		
		outages.add(outage);

		view.addSection(section);
		List <String>services = new ArrayList<String>();
		services.add("HTTP");
//		ServiceSelector selector = new ServiceSelector("isHTTP",(List<String>) services);
		
		
		
		expect(viewDisplayDao.getView()).andReturn(view);
		expect(categoryDao.getCategoryByLabel( category.getLabel() ) ).andReturn(createCategoryFromLabel(category.getLabel()));
		expect(outageDao.matchingCurrentOutages(isA(ServiceSelector.class))).andReturn(outages);
		
		
		replay(categoryDao);
		replay(viewDisplayDao);
		replay(outageDao);
		
		Collection<StatusSection> statusSections = categoryStatusService.getCategoriesStatus();
		verify(viewDisplayDao);
		verify(categoryDao);
		verify(outageDao);
		
		assertEquals("Wrong Number of StatusSections",view.getSectionCount(),statusSections.size());
		
		
		for (StatusSection statusSection : statusSections) {
		
			
			assertEquals("StatusSection Name Does Not Match","Section One",statusSection.getName());
				
			Collection <StatusCategory> statusCategorys = statusSection.getCategories();  
			
			for(StatusCategory statusCategory : statusCategorys){
				
				assertEquals("StatusCategoryName does not match","Category One",statusCategory.getLabel());
				//assertEquals("Category Comment Does not match","Category One Comment",statusCategory.getComment());				
				assertTrue("Nodes >= 1",statusCategory.getNodes().size() >= 1);	
				
				for(StatusNode statusNode : statusCategory.getNodes()){
				
					assertEquals("Label does not match","superLabel",statusNode.getLabel());
				}
			}
			
		}
		
		
	}


	private Category createCategoryFromLabel(String label) {
		
		Category category = new Category();
		
		category.setLabel(label);
		category.setRule("isHTTP");
		category.addService("HTTP");
		
		
		return category;
	}
	
	
	
	
}
