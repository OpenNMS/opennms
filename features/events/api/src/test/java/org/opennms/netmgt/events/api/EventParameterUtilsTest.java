/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
