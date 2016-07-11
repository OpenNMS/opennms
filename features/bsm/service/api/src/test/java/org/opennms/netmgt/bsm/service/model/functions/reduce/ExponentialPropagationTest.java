/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.model.functions.reduce;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.opennms.netmgt.bsm.service.model.Status;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ExponentialPropagationTest {

    @Test
    public void testEmpty() {
        ExponentialPropagation reduceFunction = new ExponentialPropagation();
        reduceFunction.setBase(2.0);

        assertEquals(Optional.empty(),
                     reduceFunction.reduce(Lists.newArrayList()));

    }

    @Test
    public void testSingleInput() {
        ExponentialPropagation reduceFunction = new ExponentialPropagation();
        reduceFunction.setBase(2.0);

        assertEquals(Optional.of(Status.NORMAL),
                     reduceFunction.reduce(Lists.newArrayList(Status.NORMAL)));

        assertEquals(Optional.of(Status.WARNING),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING)));
    }

    @Test
    public void testIndeterminate() {
        ExponentialPropagation reduceFunction = new ExponentialPropagation();
        reduceFunction.setBase(2.0);

        assertEquals(Optional.of(Status.INDETERMINATE),
                     reduceFunction.reduce(Lists.newArrayList(Status.INDETERMINATE,
                                                              Status.INDETERMINATE,
                                                              Status.INDETERMINATE)));

    }

    @Test
    public void testBaseTwo() {
        ExponentialPropagation reduceFunction = new ExponentialPropagation();
        reduceFunction.setBase(2.0);

        assertEquals(Optional.of(Status.NORMAL),
                     reduceFunction.reduce(Lists.newArrayList(Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL)));

        assertEquals(Optional.of(Status.WARNING),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL)));

        assertEquals(Optional.of(Status.MINOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.WARNING, Status.NORMAL, Status.NORMAL)));

        assertEquals(Optional.of(Status.MINOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL)));

        assertEquals(Optional.of(Status.MAJOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING)));


        assertEquals(Optional.of(Status.MINOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.MINOR, Status.WARNING, Status.NORMAL)));

        assertEquals(Optional.of(Status.MAJOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.MINOR, Status.WARNING, Status.WARNING)));
    }

    @Test
    public void testBaseThree() {
        ExponentialPropagation reduceFunction = new ExponentialPropagation();
        reduceFunction.setBase(3.0);

        assertEquals(Optional.of(Status.NORMAL),
                     reduceFunction.reduce(Lists.newArrayList(Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL)));

        assertEquals(Optional.of(Status.WARNING),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL)));

        assertEquals(Optional.of(Status.MINOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL)));

        assertEquals(Optional.of(Status.MINOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL)));

        assertEquals(Optional.of(Status.MAJOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING)));


        assertEquals(Optional.of(Status.MINOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.MINOR, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL)));

        assertEquals(Optional.of(Status.MAJOR),
                     reduceFunction.reduce(Lists.newArrayList(Status.MINOR, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING)));
    }
}
