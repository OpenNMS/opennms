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
