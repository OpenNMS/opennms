/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.plugins.elasticsearch.rest;

import java.util.HashMap;
import java.util.Map;

public class MockNodeCache implements NodeCache {

	@Override
	public Map getEntry(Long key) {
		Map<String,String> body = new HashMap<String,String>();
		
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

	}

}
