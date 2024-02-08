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
package org.opennms.netmgt.config.filter;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DatabaseSchemaTest extends XmlTestNoCastor<DatabaseSchema> {

    public DatabaseSchemaTest(final DatabaseSchema sampleObject,
            final String sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/database-schema.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
                {
                    getSchema(),
                    "<database-schema xmlns=\"http://xmlns.opennms.org/xsd/config/filter\">\n" + 
                    "   <table name=\"categories\">\n" + 
                    "      <join type=\"left\" column=\"categoryID\" table=\"category_node\" table-column=\"categoryID\"/>\n" + 
                    "      <column name=\"categoryID\"/>\n" + 
                    "   </table>\n" + 
                    "</database-schema>",
                },
                {
                    new DatabaseSchema(),
                    "<database-schema/>",
                }
            });
    }

    private static DatabaseSchema getSchema() {
        final DatabaseSchema schema = new DatabaseSchema();

        Table table = new Table();
        table.setName("categories");
        schema.addTable(table);

        Join join = new Join();
        join.setType("left");
        join.setColumn("categoryID");
        join.setTable("category_node");
        join.setTableColumn("categoryID");
        table.addJoin(join);

        Column column = new Column();
        column.setName("categoryID");
        table.addColumn(column);

        return schema;
    }
}
