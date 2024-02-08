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
