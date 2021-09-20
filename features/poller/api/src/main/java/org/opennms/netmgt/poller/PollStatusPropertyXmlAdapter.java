/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Used to marshal/unmarshal the property map while preserving the ordering. 
 *
 * @author jwhite
 */
public class PollStatusPropertyXmlAdapter extends XmlAdapter<PollStatusProperties, Map<String, Number>> {

    @Override
    public Map<String, Number> unmarshal(PollStatusProperties props) throws Exception {
        if (props == null) {
            return null;
        }
        final Map<String, Number> map = new LinkedHashMap<>();
        props.getProperties().stream().forEach(p -> map.put(p.getKey(), p.getValue()));
        return map;
    }

    @Override
    public PollStatusProperties marshal(Map<String, Number> map) throws Exception {
        if (map == null) {
            return null;
        }
        final List<PollStatusProperty> props = new ArrayList<>(map.size());
        map.entrySet().stream().forEach(e -> {
           props.add(new PollStatusProperty(e.getKey(), e.getValue()));
        });
        return new PollStatusProperties(props);
    }

}
