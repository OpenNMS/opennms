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
package org.opennms.netmgt.events.api;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

public class EventParameterUtilsTest {

    private List<Parm> parameters;

    @Before
    public void setUp(){
        parameters = new ArrayList<>();
        parameters.add(new Parm("A", "A.1"));
        parameters.add(new Parm("A", "A.2"));
        parameters.add(new Parm("B", "B.1"));
        parameters.add(new Parm("B", "B.2"));
        parameters.add(new Parm("C", "C.1"));
    }

    @Test
    public void normalizePreserveOrderShouldRetainLastParameterInList() {
        List<Parm> filtered = EventParameterUtils.normalizePreserveOrder(parameters);
        assertEquals(new String[] {"A.2", "B.2", "C.1"}, filtered.stream().map(Parm::getValue).map(Value::getContent).toArray());
    }

    @Test
    public void bothNormalizeMethodsShouldFilterTheSameWay() {
        Map<String, Parm> filtered = EventParameterUtils.normalizePreserveOrder(parameters).stream()
                .collect(Collectors.toMap(Parm::getParmName, Function.identity()));
        Map<String, Parm> filteredOld = EventParameterUtils.normalize(parameters);
        assertEquals(filteredOld, filtered);
    }
}
