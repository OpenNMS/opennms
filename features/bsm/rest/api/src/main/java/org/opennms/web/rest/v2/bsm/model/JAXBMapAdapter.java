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
package org.opennms.web.rest.v2.bsm.model;

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
public class JAXBMapAdapter extends XmlAdapter<JAXBMapAdapter.JAXBMap, Map<String, String>> {

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlRootElement(name = "attributes")
    public static class JAXBMap {
        @XmlElement(name = "attribute", required = true)
        private final List<JAXBMapEntry> a = new ArrayList<>();

        public List<JAXBMapEntry> getA() {
            return this.a;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlRootElement(name = "attribute")
    public static class JAXBMapEntry {

        @XmlElement(name = "key", required = true)
        private final String key;

        @XmlElement(name = "value", required = true)
        private final String value;

        public JAXBMapEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public JAXBMapEntry() {
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
    public JAXBMap marshal(Map<String, String> v) throws Exception {
        JAXBMap myMap = new JAXBMap();
        List<JAXBMapEntry> aList = myMap.getA();
        for (Map.Entry<String, String> e : v.entrySet()) {
            aList.add(new JAXBMapEntry(e.getKey(), e.getValue()));
        }
        return myMap;
    }

    @Override
    public Map<String, String> unmarshal(JAXBMap v) throws Exception {
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (JAXBMapEntry e : v.getA()) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }
}
