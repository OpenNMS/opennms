package org.opennms.plugins.dbnotifier.test.manual;

import java.util.HashMap;
import java.util.Map;

import org.opennms.plugins.elasticsearch.rest.NodeCache;

public class MockNodeCache implements NodeCache {

	@Override
	public Map getEntry(Long key) {
		Map<String,String> body=new HashMap<String,String>();
		
        body.put("nodelabel", "nodelabel_"+key);
        body.put("nodesysname", "nodesysname_"+key);
        body.put("nodesyslocation", "nodesyslocation_"+key);
        body.put("foreignsource", "mock_foreignsource");
        body.put("foreignid", "foreignid_"+key);
        body.put("operatingsystem", "linux");
        body.put("categories", "cat1,cat2,cat3,cat4");
        
        return body;
	}

	@Override
	public void refreshEntry(Long key) {
		// TODO Auto-generated method stub
		
	}

}
