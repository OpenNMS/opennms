/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.impl;

import org.jrobin.core.RrdException;
import org.junit.Test;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.Source;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JRrd2FetchStrategyTest {
    @Test
    public void checkLongDataSourceName() throws RrdException {
        final JRrd2FetchStrategy fetch = new JRrd2FetchStrategy();
        final Map<Source, String> rrdsBySource = new HashMap<>();

        String rrdFile = new File("src/test/resources/TlmRecordEnrichmentErrors.rrd").getAbsolutePath();
        rrdsBySource.put(new Source("TlmRecordEnrichmentErrors","nodeSource[Minion:node1]",
                "TlmRecordEnrichmentErrors", null, false), rrdFile);

        final Map<String, Object> constants = new HashMap<>();
        final QueryMetadata metadata = new QueryMetadata();

        fetch.fetchMeasurements(1600000500000L, 1600000800000L, 363, 864,
                rrdsBySource, constants, metadata);
    }
}
