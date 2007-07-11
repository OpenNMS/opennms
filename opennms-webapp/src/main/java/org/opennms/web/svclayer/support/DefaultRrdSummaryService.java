package org.opennms.web.svclayer.support;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	public Stats m_stats = new Stats();
	
	static class OpStats {
		private String m_name;
		private int m_count = 0;
		private long m_total = 0;
		private long m_lastStarted = -1;
		
		OpStats(String n) {
			m_name = n;
		}
		
		void begin() {
			m_count++;
			m_lastStarted = System.nanoTime();
		}
		
		void end() {
			long ended  = System.nanoTime();
			Assert.state(m_lastStarted >= 0, "must call begin before calling end");
			m_total += (ended - m_lastStarted);
			m_lastStarted = -1;
		}

		@Override
		public String toString() {
			double total = (double)m_total;
			return String.format("stats: %s: count=%d, totalTime=%f ms ( %f us/call )", m_name, m_count, total/1000000.0, total/(m_count*1000.0));
		}
		
		
	}
	
	static class Stats {
		Map<String, OpStats> map = new LinkedHashMap<String, OpStats>();
		public void begin(String operation) {
			if (!map.containsKey(operation)) {
				map.put(operation, new OpStats(operation));
			}
			map.get(operation).begin();
		}

		public void end(String operation) {
			map.get(operation).end();
		}

		@Override
		public String toString() {
			StringBuilder bldr = new StringBuilder(map.size()*50);
			for (OpStats opStat : map.values()) {
				bldr.append(opStat);
				bldr.append('\n');
			}
			return bldr.toString();
		}
		
	}

	public Summary getSummary(String filterRule, final long startTime, final long endTime) {
		m_stats.begin("getSummary");
		try {
			final SummaryBuilder bldr = new SummaryBuilder();


			m_filterDao.walkMatchingNodes(filterRule, new AbstractEntityVisitor() {
				public void visitNode(OnmsNode node) {

					OnmsResource nodeResource = getResourceForNode(node);

					bldr.pushResource(node.getLabel());

					for(OnmsResource child : getChildResources1(nodeResource)) {
						if (child.getResourceType() instanceof NodeSnmpResourceType) {
							addAttributes(getResourceGraphAttributes(child));
						} 
					}

					for(OnmsResource child : getChildResources2(nodeResource)) {
						if (!(child.getResourceType() instanceof NodeSnmpResourceType)) {
							addResource(child);
						}
					}

					bldr.popResource();
				}

				private Collection<RrdGraphAttribute> getResourceGraphAttributes(OnmsResource child) {
					String op = "getResourceGraphAttributes-"+child.getResourceType().getName();
					m_stats.begin(op);
					try {
						return child.getRrdGraphAttributes().values();
					} finally {
						m_stats.end(op);
					}
				}

				private List<OnmsResource> getChildResources1(OnmsResource nodeResource) {
					m_stats.begin("getChildResources1");
					try {
						return nodeResource.getChildResources();
					} finally {
						m_stats.end("getChildResources1");
					}
				}

				private List<OnmsResource> getChildResources2(OnmsResource nodeResource) {
					m_stats.begin("getChildResources2");
					try {
						return nodeResource.getChildResources();
					} finally {
						m_stats.end("getChildResources2");
					}
				}

				private OnmsResource getResourceForNode(OnmsNode node) {
					m_stats.begin("getResourceForNode");
					try {
						return m_resourceDao.getResourceForNode(node);
					} finally {
						m_stats.end("getResourceForNode");
					}
				}

				private void addResource(OnmsResource resource) {
					addResource(resource, resource.getLabel());
				}

				private void addResource(OnmsResource resource, String label) {
					Collection<RrdGraphAttribute> attrs = getResourceGraphAttributes(resource);
					if (attrs.size() > 0) {
						bldr.addResource(label);
						addAttributes(attrs);
					}
				}

				private void addAttributes(Collection<RrdGraphAttribute> attrs) {
					m_stats.begin("addAttribute");
					try {
						for(RrdGraphAttribute attr : attrs) {
							//System.err.println("Getting values for attribute: "+attr);
							bldr.addAttribute(attr.getName());
							double[] values = getValues(attr);
							bldr.setMin(values[0]);
							bldr.setAverage(values[1]);
							bldr.setMax(values[2]);
						}
					} finally {
						m_stats.end("addAttribute");
					}
				}
				
				private double[] getValues(RrdGraphAttribute attr) {
					m_stats.begin("getValues");
					try {
						return m_rrdDao.getPrintValues(attr, "AVERAGE", startTime*1000, endTime*1000, "MIN", "AVERAGE", "MAX");
					} finally {
						m_stats.end("getValues");
					}
				}

			});

			return bldr.getSummary();
		} finally {
			m_stats.end("getSummary");
			System.err.println(m_stats);
		}
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
