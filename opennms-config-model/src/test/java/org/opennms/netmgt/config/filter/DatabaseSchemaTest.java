/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
