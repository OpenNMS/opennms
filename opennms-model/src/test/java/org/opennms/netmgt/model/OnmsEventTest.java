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

package org.opennms.netmgt.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class OnmsEventTest {

    @Test
    public void shouldOrderEventParamsWithNullPositions() {
        List<OnmsEventParameter> params = Arrays.asList(
                param("A", 0),
                param("B", 0),
                param("C", 0),
                param("D", 0),
                param("E", 0)
        );
        shouldOrderEventParams(params, "A", "B", "C", "D", "E");
    }

    @Test
    public void shouldOrderEventParamsWithFilledPositions() {
        List<OnmsEventParameter> params = Arrays.asList(
                param("C", 2),
                param("A", 0),
                param("E", 4),
                param("D", 3),
                param("B", 1)
        );
        shouldOrderEventParams(params, "A", "B", "C", "D", "E");
    }

    @Test
    public void shouldOrderEventParamsWithDoublePositions() {
        List<OnmsEventParameter> params = Arrays.asList(
                param("C", 2),
                param("A", 0),
                param("E", 0),
                param("D", 3),
                param("B", 1),
                param("F", 1)
        );
        shouldOrderEventParams(params, "A", "E", "B", "F", "C", "D");
    }

    private void shouldOrderEventParams(final List<OnmsEventParameter> params, final String ... expected) {
        OnmsEvent event = new OnmsEvent();
        event.setPositionsOnParameters(params);
        assertEquals(expected, params.stream().map(OnmsEventParameter::getName).toArray());
    }

    private OnmsEventParameter param(String name, int position) {
        OnmsEventParameter param = new OnmsEventParameter();
        param.setName(name);
        param.setPosition(position);
        return param;
    }
}
