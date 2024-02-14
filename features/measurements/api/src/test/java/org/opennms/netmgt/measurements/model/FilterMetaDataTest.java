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
