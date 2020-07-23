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
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.hibernate.collection.PersistentBag;
import org.junit.Test;

public class OnmsEventTest {

    @Test
    public void shouldSetPositionOnEventParameters() {
        List<OnmsEventParameter> params = Arrays.asList(
                param("A"),
                param("B"),
                param("C"),
                param("D"),
                param("E")
        );
        OnmsEvent event = new OnmsEvent();
        event.setEventParameters(params);
        for(int i=0; i<5; i++) {
            assertEquals(i, event.getEventParameters().get(i).getPosition());
        }
        checkOrder(event.getEventParameters(), "A", "B", "C", "D", "E");
    }

	@Test
	public void shouldPreserveParameterOrderFromDatabase() {
		List<OnmsEventParameter> params = new ArrayList<>(Arrays.asList(
				param("A"),
				param("B"),
				param("C"),
				param("D"),
				param("E"))
		);
		OnmsEvent event = new OnmsEvent();
		event.setEventParameters(params);
		params = event.getEventParameters();

		// assume we are writing now to database and retrieve parameters out of order
        List<OnmsEventParameter> shuffledParams = new ArrayList<>(params);
        Collections.shuffle(shuffledParams, new Random(41));
        params.clear();
        params.addAll(shuffledParams);

		// but the order should be ok again when sorting by position
        checkOrder(event.getEventParameters(), "A", "B", "C", "D", "E");
	}

    @Test
    public void shouldBeResilientAgainstParameterNullList() {
        OnmsEvent event = new OnmsEvent();
        event.setEventParameters(null);
        assertNull(event.getEventParameters());
    }

    private void checkOrder(final List<OnmsEventParameter> params, final String ... expected) {
        assertEquals(expected, params.stream().map(OnmsEventParameter::getName).toArray());
    }

    private OnmsEventParameter param(String name) {
        OnmsEventParameter param = new OnmsEventParameter();
        param.setName(name);
        return param;
    }
}
