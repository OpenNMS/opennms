/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.core.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@XmlTransient
public class JaxbMapAdapter extends XmlAdapter<JaxbMapAdapter.JaxbMap, Map<String,String>> {
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "entries")
	public static class JaxbMap {
	    @XmlElement(name = "entry", required = true)
	    private final List<JaxbMapEntry> a = new ArrayList<>();
	    public List<JaxbMapEntry> getA() {
	        return this.a;
	    }
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "entry")
	public static class JaxbMapEntry {

	    @XmlElement(name = "key", required = true)
	    private final String key;

	    @XmlElement(name = "value", required = true)
	    private final String value;

	    public JaxbMapEntry(String key, String value) {
	        this.key = key;
	        this.value = value;
	    }

	    public JaxbMapEntry() {
	        this.key = null;
	        this.value = null;
	    }

	    public String getKey() {
	        return key;
	    }

	    public String getValue() {
	        return value;
	    }
	}

    @Override
    public JaxbMap marshal(Map<String,String> v) throws Exception {
    	if (v.isEmpty()) return null;
        JaxbMap myMap = new JaxbMap();
        List<JaxbMapEntry> aList = myMap.getA();
        for ( Map.Entry<String,String> e : v.entrySet() ) {
            aList.add(new JaxbMapEntry(e.getKey(), e.getValue()));
        }
        return myMap;
    }

    @Override
    public Map<String,String> unmarshal(JaxbMap v) throws Exception {
        Map<String,String> map = new LinkedHashMap<String,String>();
        for ( JaxbMapEntry e : v.getA() ) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }
}