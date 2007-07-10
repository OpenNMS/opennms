package org.opennms.web.svclayer.support;

import org.opennms.netmgt.config.attrsummary.Attribute;
import org.opennms.netmgt.config.attrsummary.Resource;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.netmgt.config.attrsummary.Value;
import org.opennms.netmgt.config.attrsummary.types.Cf;
import org.opennms.web.svclayer.RrdSummaryService;
import org.springframework.util.Assert;

public class DefaultRrdSummaryService implements RrdSummaryService {
	
	static class SummaryBuilder {
		private Summary m_summary;
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
			m_summary.addResource(m_currResource);
		}
		
		public void addAttribute(String string) {
			Assert.state(m_currResource != null, "addResource must be called before calling addAttribute");
			m_currAttr = new Attribute();
			m_currResource.addAttribute(m_currAttr);
		}
		
		public void addValue(Cf function, double value) {
			Assert.state(m_currAttr != null, "addAttribute must be called before called addValue");
			Value val = new Value();
			val.setFunction(function);
			val.setContent(value);
			m_currAttr.addValue(val);

		}
	}

	public Summary getSummary(String filterRule, long startTime, long endTime) {
		SummaryBuilder bldr = new SummaryBuilder();
		
		bldr.addResource("resource1");
		bldr.addAttribute("attr1");
		bldr.addValue(Cf.AVERAGE, 1.0);
		bldr.addValue(Cf.MIN, 1.0);
		bldr.addValue(Cf.MAX, 1.0);
		bldr.addAttribute("attr2");
		bldr.addValue(Cf.AVERAGE, 2.0);
		bldr.addValue(Cf.MIN, 1.0);
		bldr.addValue(Cf.MAX, 3.0);
		bldr.addResource("resource2");
		bldr.addAttribute("attr1");
		bldr.addValue(Cf.AVERAGE, 1.0);
		bldr.addValue(Cf.MIN, 1.0);
		bldr.addValue(Cf.MAX, 1.0);
		bldr.addAttribute("attr2");
		bldr.addValue(Cf.AVERAGE, 2.0);
		bldr.addValue(Cf.MIN, 1.0);
		bldr.addValue(Cf.MAX, 3.0);

		
		return bldr.getSummary();
	}

}
