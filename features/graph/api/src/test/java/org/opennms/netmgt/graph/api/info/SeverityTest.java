/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.info;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsSeverity;

public class SeverityTest {

    @Test
    public void verifyIsLessThan() {
        // Unknown
        assertThat(Severity.Unknown.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Unknown.isLessThan(Severity.Normal), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Warning), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Minor), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Unknown.isLessThan(Severity.Critical), Matchers.is(true));

        // Normal
        assertThat(Severity.Normal.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Normal.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Normal.isLessThan(Severity.Warning), Matchers.is(true));
        assertThat(Severity.Normal.isLessThan(Severity.Minor), Matchers.is(true));
        assertThat(Severity.Normal.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Normal.isLessThan(Severity.Critical), Matchers.is(true));

        // Warning
        assertThat(Severity.Warning.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Warning.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Warning.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Warning.isLessThan(Severity.Minor), Matchers.is(true));
        assertThat(Severity.Warning.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Warning.isLessThan(Severity.Critical), Matchers.is(true));

        // Minor
        assertThat(Severity.Minor.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Minor), Matchers.is(false));
        assertThat(Severity.Minor.isLessThan(Severity.Major), Matchers.is(true));
        assertThat(Severity.Minor.isLessThan(Severity.Critical), Matchers.is(true));

        // Major
        assertThat(Severity.Major.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Minor), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Major), Matchers.is(false));
        assertThat(Severity.Major.isLessThan(Severity.Critical), Matchers.is(true));

        // Critical
        assertThat(Severity.Critical.isLessThan(Severity.Unknown), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Normal), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Warning), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Minor), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Major), Matchers.is(false));
        assertThat(Severity.Critical.isLessThan(Severity.Critical), Matchers.is(false));
    }

    @Test
    public void verifyCreateFrom() {
        assertThat(Severity.createFrom(OnmsSeverity.INDETERMINATE), Matchers.is(Severity.Unknown));
        assertThat(Severity.createFrom(OnmsSeverity.NORMAL), Matchers.is(Severity.Normal));
        assertThat(Severity.createFrom(OnmsSeverity.WARNING), Matchers.is(Severity.Warning));
        assertThat(Severity.createFrom(OnmsSeverity.MINOR), Matchers.is(Severity.Minor));
        assertThat(Severity.createFrom(OnmsSeverity.MAJOR), Matchers.is(Severity.Major));
        assertThat(Severity.createFrom(OnmsSeverity.CRITICAL), Matchers.is(Severity.Critical));
    }
}
