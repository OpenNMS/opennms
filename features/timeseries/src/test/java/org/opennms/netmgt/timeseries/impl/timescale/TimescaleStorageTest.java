/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.impl.timescale;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.opennms.netmgt.timeseries.api.domain.Tag;

public class TimescaleStorageTest {

    @Test
    public void shouldGenerateSqlForMetricSql() {
        assertEquals("select distinct fk_timescale_metric from timescale_tag;",
                new TimescaleStorage().createMetricsSQL(Collections.emptyList()));
        assertEquals("select distinct fk_timescale_metric from timescale_tag where 1=1 AND (key='a' AND value='b');",
                new TimescaleStorage().createMetricsSQL(Collections.singletonList(new Tag("a", "b"))));
        assertEquals("select distinct fk_timescale_metric from timescale_tag where 1=1 AND (key='a' AND value='b') AND (key='c' AND value='d');",
                new TimescaleStorage().createMetricsSQL(Arrays.asList(new Tag("a", "b"), new Tag("c", "d"))));
        assertEquals("select distinct fk_timescale_metric from timescale_tag where 1=1 AND (key is null AND value='b');",
                new TimescaleStorage().createMetricsSQL(Collections.singletonList(new Tag(null, "b"))));
    }
}
