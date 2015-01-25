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

package org.opennms.netmgt.jasper.analytics;

import java.awt.Point;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import net.sf.jasperreports.data.cache.ColumnValues;
import net.sf.jasperreports.data.cache.ColumnValuesDataSource;
import net.sf.jasperreports.data.cache.DoubleArrayValues;
import net.sf.jasperreports.data.cache.ObjectArrayValues;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;

/**
 * Helper class for converting RRD-based data sources to and from
 * table representations.
 *
 * @author jwhite
 */
public class DataSourceUtils {
    public static ColumnValuesDataSource toDs(Table<Integer, String, Double> table) {
        String columnNames[] = table.columnKeySet().toArray(new String[0]);
        List<ColumnValues> columnValues = Lists.newLinkedList();
        int numRows = table.rowKeySet().size();

        // Rebuild all of the columns
        for(String columnName : columnNames) {
            if ("Timestamp".equalsIgnoreCase(columnName)) {
                // Always convert the Timestamp column to Date objects
                Object[] values = new Date[numRows];
                for (int i = 0; i < numRows; i++) {
                    values[i] = new Date(table.get(i, columnName).longValue());
                }
                columnValues.add(new ObjectArrayValues(values));
            } else {
                // Leave every other column as doubles
                double[] values = new double[numRows];
                for (int i = 0; i < numRows; i++) {
                    Double value = table.get(i, columnName);
                    // Convert any nulls to NaNs, avoids NPEs
                    if (value != null) {
                        values[i] = value;
                    } else {
                        values[i] = Double.NaN;
                    }
                }
                columnValues.add(new DoubleArrayValues(values));
            }
        }

        // Join the columns into a data source
        return new ColumnValuesDataSource(
                columnNames,
                numRows,
                columnValues.toArray(new ColumnValues[0])
        );
    }

    public static RowSortedTable<Integer, String, Double> fromDs(JRRewindableDataSource ds,
            String... fieldNames) throws JRException {
        RowSortedTable<Integer, String, Double> table = TreeBasedTable.create();
        int rowIndex = 0;
        // Build the table, row by row
        while(ds.next()) {
            for (String fieldName : fieldNames) {
                JRDesignField field = new JRDesignField();
                field.setDescription(fieldName);
                field.setName(fieldName);
                // Some data-source implementation check the value class
                field.setValueClass(Object.class);
                table.put(rowIndex, fieldName, getValueAsDouble(ds.getFieldValue(field)));
            }
            rowIndex++;
        }
        ds.moveFirst();
        return table;
    }

    private static Double getValueAsDouble(Object o) {
        if (o instanceof Date) {
            return (double) ((Date)o).getTime();
        }
        try {
            return (Double) o;
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid value type. "
                    + "Does the datasource originate from an RRD or JRB file?", e);
        }
    }

    public static Point getRowsWithValues(Table<Integer, String, Double> table, String... columnNames) {
        int firstRowWithValues = -1, lastRowWithValues = -1;
        for (int k : table.rowKeySet()) {
            for (String columnName : columnNames) {
                Double value = table.get(k, columnName);
                
                if (value != null && !Double.isNaN(value)) {
                    if (firstRowWithValues < 0) {
                        firstRowWithValues = k;
                    }
                    lastRowWithValues = k;
                }
            }
        }

        return new Point(firstRowWithValues, lastRowWithValues);
    }
}
