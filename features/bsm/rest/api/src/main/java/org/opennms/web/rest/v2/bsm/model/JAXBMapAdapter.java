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
