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

import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    /**
     * Source resource metadata
     */
    private QueryMetadata metadata;

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
    @JsonProperty("timestamps")
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

    @com.fasterxml.jackson.annotation.JsonSetter
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

    @com.fasterxml.jackson.annotation.JsonSetter
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

    @com.fasterxml.jackson.annotation.JsonSetter
    public void setConstants(final Map<String,Object> constants) {
        final List<QueryConstant> c = new ArrayList<>();
        for (final Map.Entry<String,Object> entry : constants.entrySet()) {
            c.add(new QueryConstant(entry.getKey(), entry.getValue().toString()));
        }
        this.constants = c;
    }

    @XmlElement(name="metadata")
    public QueryMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(final QueryMetadata metadata) {
        this.metadata = metadata;
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
             && com.google.common.base.Objects.equal(this.metadata, other.metadata)
             && Arrays.equals(this.timestamps, other.timestamps)
             && Arrays.equals(this.labels, other.labels)
             && Arrays.equals(this.columns, other.columns);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.step, this.start, this.end, this.timestamps, this.labels, this.columns, this.constants, this.metadata);
    }

    @Override
    public String toString() {
       return com.google.common.base.MoreObjects.toStringHelper(this)
                 .add("Step", this.step)
                 .add("Start", this.start)
                 .add("End", this.end)
                 .add("Timestamps", Arrays.toString(this.timestamps))
                 .add("Labels", Arrays.toString(this.labels))
                 .add("Columns", Arrays.toString(this.columns))
                 .add("Constants", this.constants)
                 .add("Metadata",  this.metadata)
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
           return com.google.common.base.MoreObjects.toStringHelper(this)
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
