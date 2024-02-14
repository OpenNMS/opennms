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