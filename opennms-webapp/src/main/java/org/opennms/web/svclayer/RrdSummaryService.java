package org.opennms.web.svclayer;

import org.opennms.netmgt.config.attrsummary.Summary;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public interface RrdSummaryService {
	
	Summary getSummary(String filterRule, long startTime, long endTime);

}
