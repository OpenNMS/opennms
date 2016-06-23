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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Maps;

/**
 * Query response.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
@XmlRootElement(name = "query-response")
public class QueryResponse {

    /**
     * Step size in ms.
     */
    private long step;

    /**
     * Timestamp in ms.
     */
    private long start;

    /**
     * Timestamp in ms.
     */
    private long end;

    /**
     * Row timestamps in ms.
     */
    private long[] timestamps;

    /**
     * Column names
     */
    private String[] labels;

    /**
     * Column values
     */
    private WrappedPrimitive[] columns;

    /**
     * String constants
     */
    private List<QueryConstant> constants;

    @XmlAttribute(name = "step")
    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    @XmlAttribute(name = "start")
    public long getStart() {
        return start;
    }

    public void setStart(final long start) {
        this.start = start;
    }

    @XmlAttribute(name = "end")
    public long getEnd() {
        return end;
    }

    public void setEnd(final long end) {
        this.end = end;
    }

    @XmlElement(name = "timestamps")
    public long[] getTimestamps() {
        return timestamps;
    }

    /**
     * Required by JAXB.
     */
    public void setTimestamps(final ArrayList<Long> timestamps) {
        final int N = timestamps.size();
        this.timestamps = new long[N];
        for (int i = 0; i < N; i++) {
            this.timestamps[i] = timestamps.get(i);
        }
    }

    public void setTimestamps(final long[] timestamps) {
        this.timestamps = timestamps;
    }

    @XmlElement(name="labels")
    public String[] getLabels() {
        return labels;
    }

    public void setLabels(final String[] labels) {
        this.labels = labels;
    }

    @XmlElement(name="columns")
    @JsonProperty("columns")
    public WrappedPrimitive[] getColumns() {
        return columns;
    }

    public void setColumns(final WrappedPrimitive[] columns) {
        this.columns = columns;
    }

    public void setColumns(final List<double[]> doubles) {
        final int N = doubles.size();
        this.columns = new WrappedPrimitive[N];
        for (int i = 0; i < N; i++) {
            this.columns[i] = new WrappedPrimitive(doubles.get(i));
        }
    }

    public void setColumns(final Map<String, double[]> columns) {
        final int N = columns.keySet().size();
        this.labels = new String[N];
        this.columns = new WrappedPrimitive[N];
        int k = 0;
        for (final Map.Entry<String, double[]> entry : columns.entrySet()) {
            this.labels[k] = entry.getKey();
            this.columns[k++] = new WrappedPrimitive(entry.getValue());
        }
    }

    @XmlElement(name="constants")
    public List<QueryConstant> getConstants() {
        return this.constants;
    }

    public void setConstants(final List<QueryConstant> constants) {
        this.constants = constants;
    }

    public void setConstants(final Map<String,Object> constants) {
        final List<QueryConstant> c = new ArrayList<>();
        for (final Map.Entry<String,Object> entry : constants.entrySet()) {
            c.add(new QueryConstant(entry.getKey(), entry.getValue().toString()));
        }
        this.constants = c;
    }

    /**
     * Convenience method.
     */
    public Map<String, double[]> columnsWithLabels() {
        final Map<String, double[]> mappedValues = Maps.newHashMap();
        for (int i = 0; i < labels.length; i++) {
            mappedValues.put(labels[i], columns[i].getList());
        }
        return mappedValues;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null)
       {
          return false;
       }
       if (getClass() != obj.getClass())
       {
          return false;
       }
       final QueryResponse other = (QueryResponse) obj;

       return   com.google.common.base.Objects.equal(this.step, other.step)
             && com.google.common.base.Objects.equal(this.start, other.start)
             && com.google.common.base.Objects.equal(this.end, other.end)
             && com.google.common.base.Objects.equal(this.constants, other.constants)
             && Arrays.equals(this.timestamps, other.timestamps)
             && Arrays.equals(this.labels, other.labels)
             && Arrays.equals(this.columns, other.columns);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.step, this.start, this.end, this.timestamps, this.labels, this.columns, this.constants);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("Step", this.step)
                 .add("Start", this.start)
                 .add("End", this.end)
                 .add("Timestamps", Arrays.toString(this.timestamps))
                 .add("Labels", Arrays.toString(this.labels))
                 .add("Columns", Arrays.toString(this.columns))
                 .add("Constants", this.constants)
                 .toString();
    }

    /**
     * Used to wrap an array of primitive doubles in order
     * to avoid boxing for marshaling.
     */
    @XmlRootElement
    public static class WrappedPrimitive {
        private double[] values;

        public WrappedPrimitive() {
        }

        public WrappedPrimitive(double[] values) {
            this.values = values;
        }

        @XmlElement(name="values")
        @JsonProperty("values")
        public double[] getList() {
            return values;
        }

        public void setList(double[] values) {
            this.values = values;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
            {
               return false;
            }
            if (getClass() != obj.getClass())
            {
               return false;
            }
            final WrappedPrimitive other = (WrappedPrimitive) obj;

            return Arrays.equals(this.values, other.values);
        }

        @Override
        public int hashCode() {
           return com.google.common.base.Objects.hashCode(this.values);
        }

        @Override
        public String toString() {
           return com.google.common.base.Objects.toStringHelper(this)
                     .add("Values", Arrays.toString(this.values))
                     .toString();
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlRootElement(name="constant")
    public static class QueryConstant {
        @XmlAttribute private final String key;
        @XmlValue     private final String value;

        public QueryConstant() {
            this.key = null;
            this.value = null;
        }
        public QueryConstant(final String key, final String value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return this.key;
        }
        public String getValue() {
            return this.value;
        }
    }
}
