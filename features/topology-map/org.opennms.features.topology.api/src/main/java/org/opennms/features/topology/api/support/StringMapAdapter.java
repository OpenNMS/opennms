/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 */
public class StringMapAdapter extends XmlAdapter<StringMapAdapter.JaxbMap, Map<String,String>> {

	public static final class JaxbMap {
		public List<Entry> entry = new ArrayList<Entry>(0);
	}

	public static final class Entry {
		
		@XmlAttribute
		public String key;
		@XmlAttribute
		public String value;
	}

	@Override
	public StringMapAdapter.JaxbMap marshal(Map<String,String> v) throws Exception {
		JaxbMap retval = new JaxbMap();
		for (String key : v.keySet()) {
			Entry entry = new Entry();
			entry.key = key;
			entry.value = v.get(key);
			retval.entry.add(entry);
		}
		return retval;
	}

	@Override
	public Map<String,String> unmarshal(StringMapAdapter.JaxbMap v) throws Exception {
		Map<String, String> retval = new HashMap<String, String>();
		for (Entry entry : v.entry) {
			retval.put(entry.key, entry.value);
		}
		return retval;
	}
}
