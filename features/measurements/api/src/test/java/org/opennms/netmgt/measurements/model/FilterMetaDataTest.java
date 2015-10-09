/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.model;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.collect.RowSortedTable;

public class FilterMetaDataTest extends XmlTestNoCastor<FilterMetaData> {

    @FilterInfo(name="noop", description="does nothing", backend="java")
    private static class MyNoOpFilter implements Filter {

        @FilterParam(key="column", required=true, displayName="Column", description="use a column")
        private String column;

        @FilterParam(key="toggle", value="false", displayName="Toggle", description="toggle something")
        private boolean toggle;

        @Override
        public void filter(RowSortedTable<Long, String, Double> qrAsTable) {
            // noop
        }
    }

    public FilterMetaDataTest(FilterMetaData sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        FilterMetaData metadata = new FilterMetaData(MyNoOpFilter.class);
        return Arrays.asList(new Object[][]{{
            metadata,
            "<filter canonicalName=\"org.opennms.netmgt.measurements.model.FilterMetaDataTest.MyNoOpFilter\" name=\"noop\" description=\"does nothing\" backend=\"java\">" +
                "<parameter key=\"column\" type=\"string\" displayName=\"Column\" description=\"use a column\" required=\"true\"/>" +
                "<parameter key=\"toggle\" type=\"boolean\" displayName=\"Toggle\" description=\"toggle something\" default=\"false\" required=\"false\"/>" +
            "</filter>",
            null
        }});
    }
}
