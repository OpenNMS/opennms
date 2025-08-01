/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.svclayer.catstatus.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.dao.api.OutageDao;
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

import junit.framework.TestCase;


public class DefaultCategoryStatusServiceTest extends TestCase {

	private DefaultCategoryStatusService categoryStatusService;
	private ViewDisplayDao viewDisplayDao;
	private CategoryConfigDao categoryDao;
	private OutageDao outageDao;
	
        @Override
	protected void setUp() throws Exception {
		super.setUp();
		viewDisplayDao = mock(ViewDisplayDao.class);
		categoryDao = mock(CategoryConfigDao.class);
		outageDao = mock(OutageDao.class);
		categoryStatusService = new DefaultCategoryStatusService();	 
		categoryStatusService.setViewDisplayDao(viewDisplayDao);
		categoryStatusService.setCategoryConfigDao(categoryDao);
		categoryStatusService.setOutageDao(outageDao);
	}

	
	public void testCategoryGroupsReturnedWhenNoneExist() {
		
		
		View view = new View();
		
		
		when(viewDisplayDao.getView()).thenReturn(view);
		
		Collection<StatusSection> categories = categoryStatusService.getCategoriesStatus();
	
		assertTrue("Collection Should Be Empty", categories.isEmpty());

		verify(viewDisplayDao, atLeastOnce()).getView();
	}
	
	
	public void testGetCategoriesStatus(){
	
		View view = new View();
		Section section = new Section();
		
		section.setSectionName("Section One");
		section.addCategory("Category One");
		
		OnmsOutage outage = new OnmsOutage();
		Collection<OnmsOutage> outages = new ArrayList<>();
		
		outage.setId(300);
		
		
		OnmsServiceType svcType = new OnmsServiceType();
		svcType.setId(3);
		svcType.setName("HTTP");
		OnmsNode node = new OnmsNode();
		node.setId(1);
		node.setLabel("superLabel");
		OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(node, 1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		iface.setSnmpInterface(snmpIface);
		//iface.setId(9);
		OnmsMonitoredService monSvc = new OnmsMonitoredService(iface, svcType);

		outage.setMonitoredService(monSvc);
		
		outages.add(outage);

		view.addSection(section);
		List <String>services = new ArrayList<>();
		services.add("HTTP");
//		ServiceSelector selector = new ServiceSelector("isHTTP",(List<String>) services);
		
		
		
		when(viewDisplayDao.getView()).thenReturn(view);
		when(categoryDao.getCategoryByLabel("Category One")).thenReturn(createCategoryFromLabel("Category One"));
		when(outageDao.matchingCurrentOutages(isA(ServiceSelector.class))).thenReturn(outages);

		Collection<StatusSection> statusSections = categoryStatusService.getCategoriesStatus();
		
		assertEquals("Wrong Number of StatusSections",view.getSections().size(),statusSections.size());
		
		
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

                verify(viewDisplayDao, atLeastOnce()).getView();
                verify(categoryDao, atLeastOnce()).getCategoryByLabel(anyString());
                verify(outageDao, atLeastOnce()).matchingCurrentOutages(any(ServiceSelector.class));
	}


	private Category createCategoryFromLabel(String label) {
		
		Category category = new Category();
		
		category.setLabel(label);
		category.setNormalThreshold(0d);
		category.setWarningThreshold(0d);
		category.setRule("isHTTP");
		category.addService("HTTP");
		
		
		return category;
	}
	
	
	
	
}
