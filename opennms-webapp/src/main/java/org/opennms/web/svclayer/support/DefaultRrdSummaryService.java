package org.opennms.web.svclayer.support;

import java.util.Collection;
import java.util.LinkedList;

import org.opennms.netmgt.config.attrsummary.Attribute;
import org.opennms.netmgt.config.attrsummary.Resource;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.dao.support.NodeSnmpResourceType;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.web.svclayer.RrdSummaryService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultRrdSummaryService implements RrdSummaryService, InitializingBean {
	
	static class SummaryBuilder {
		private Summary m_summary;
		private LinkedList<Resource> m_resourceStack = new LinkedList<Resource>();
		private Resource m_currResource;
		private Attribute m_currAttr;
		
		
		SummaryBuilder() {
			m_summary = new Summary();
		}
		
		
		Summary getSummary() {
			return m_summary;
		}

		public void addResource(String string) {
			m_currResource = new Resource();
			m_currResource.setName(string);
			addCurrentResource();
		}


		private void addCurrentResource() {
			if (m_resourceStack.isEmpty()) {
				m_summary.addResource(m_currResource);
			} else {
				m_resourceStack.getFirst().addResource(m_currResource);
			}
		}
		
		
		
		public void addAttribute(String string) {
			Assert.state(m_currResource != null, "addResource must be called before calling addAttribute");
			m_currAttr = new Attribute();
			m_currAttr.setName(string);
			m_currResource.addAttribute(m_currAttr);
		}
		
		public void setMin(double min){
			checkForCurrAttr();
			m_currAttr.setMin(min);

		}


		private void checkForCurrAttr() {
			Assert.state(m_currAttr != null, "addAttribute must be called before calling setMin,setMax or setAverage");
		}


		public void setAverage(double avg) {
			checkForCurrAttr();
			m_currAttr.setAverage(avg);
		}


		public void setMax(double max) {
			checkForCurrAttr();
			m_currAttr.setMax(max);
		}


		public void pushResource(String label) {
			addResource(label);
			m_resourceStack.addFirst(m_currResource);
		}


		public void popResource() {
			Assert.state(!m_resourceStack.isEmpty(), "cannot pop a resource that has not been pushed!");
			m_resourceStack.removeFirst();
		}
	}
	
	public FilterDao m_filterDao;
	public ResourceDao m_resourceDao;
	public RrdDao m_rrdDao;

	public Summary getSummary(String filterRule, final long startTime, final long endTime) {
		final SummaryBuilder bldr = new SummaryBuilder();

		
		m_filterDao.walkMatchingNodes(filterRule, new AbstractEntityVisitor() {
			public void visitNode(OnmsNode node) {
				OnmsResource nodeResource = m_resourceDao.getResourceForNode(node);
				
				bldr.pushResource(node.getLabel());

				for(OnmsResource child : nodeResource.getChildResources()) {
					if (child.getResourceType() instanceof NodeSnmpResourceType) {
						addAttributes(child.getRrdGraphAttributes().values());
					} 
				}
				
				for(OnmsResource child : nodeResource.getChildResources()) {
					if (!(child.getResourceType() instanceof NodeSnmpResourceType)) {
						addResource(child);
					}
				}
				
				bldr.popResource();
			}
			
			private void addResource(OnmsResource resource) {
				addResource(resource, resource.getLabel());
			}

			private void addResource(OnmsResource resource, String label) {
				Collection<RrdGraphAttribute> attrs = resource.getRrdGraphAttributes().values();
				if (attrs.size() > 0) {
					bldr.addResource(label);
					addAttributes(attrs);
				}
			}

			private void addAttributes(Collection<RrdGraphAttribute> attrs) {
				for(RrdGraphAttribute attr : attrs) {
					//System.err.println("Getting values for attribute: "+attr);
					bldr.addAttribute(attr.getName());
					bldr.setMin(m_rrdDao.getPrintValue(attr, "AVERAGE", "MIN", startTime*1000, endTime*1000));
					bldr.setAverage(m_rrdDao.getPrintValue(attr, "AVERAGE", "AVERAGE", startTime*1000, endTime*1000));
					bldr.setMax(m_rrdDao.getPrintValue(attr, "AVERAGE", "MAX", startTime*1000, endTime*1000));
				}
			}
		});
		
		return bldr.getSummary();
	}

	public void afterPropertiesSet() throws Exception {
		Assert.state(m_filterDao != null, "filterDao property must be set");
		Assert.state(m_resourceDao != null, "resourceDao property must be set");
		Assert.state(m_rrdDao != null, "rrdDao property must be set");
	}

	public void setFilterDao(FilterDao filterDao) {
		m_filterDao = filterDao;
	}

	public void setResourceDao(ResourceDao resourceDao) {
		m_resourceDao = resourceDao;
	}

	public void setRrdDao(RrdDao rrdDao) {
		m_rrdDao = rrdDao;
	}

}
