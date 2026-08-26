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

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(name = "QueryResponse",
        description = "Result of a measurements query. The response is column oriented: labels, columns and "
                + "timestamps are parallel arrays, so columns[i] holds the series named labels[i] and "
                + "columns[i].values[j] is that series' value at timestamps[j]. A sample with no data is rendered "
                + "as the JSON string \"NaN\", not as null and not as a number.")
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

    @Schema(name = "step",
            description = "Step size in milliseconds actually used, which may be coarser than the step requested "
                    + "when the underlying archives cannot supply the finer resolution.",
            example = "300000")
    @XmlAttribute(name = "step")
    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    @Schema(name = "start",
            description = "Start of the window as echoed back from the request, in milliseconds since the epoch. "
                    + "The first entry in timestamps is normally later than this, since timestamps are aligned to "
                    + "the step.",
            example = "1787728079000")
    @XmlAttribute(name = "start")
    public long getStart() {
        return start;
    }

    public void setStart(final long start) {
        this.start = start;
    }

    @Schema(name = "end",
            description = "End of the window as echoed back from the request, in milliseconds since the epoch.",
            example = "1787731679000")
    @XmlAttribute(name = "end")
    public long getEnd() {
        return end;
    }

    public void setEnd(final long end) {
        this.end = end;
    }

    @Schema(name = "timestamps",
            description = "Row timestamps in milliseconds since the epoch, aligned to step and ascending. Element j "
                    + "labels row j of every entry in columns.",
            example = "[1787728200000, 1787728500000, 1787728800000]")
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

    @Schema(name = "labels",
            description = "Series names, in the same order as columns. Holds the label of every non-transient source "
                    + "and expression in the request.")
    @XmlElement(name="labels")
    public String[] getLabels() {
        return labels;
    }

    public void setLabels(final String[] labels) {
        this.labels = labels;
    }

    @Schema(name = "columns",
            description = "One entry per series, positionally matching labels. Each entry wraps a values array of "
                    + "the same length as timestamps.")
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

    @Schema(name = "constants",
            description = "String properties of the queried resources, taken from their strings.properties. Empty "
                    + "when no queried resource carries any. Available to expressions as variables.")
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

    @Schema(name = "metadata",
            description = "Resources and nodes the request's sources resolved to.")
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
    @Schema(name = "WrappedPrimitive",
            description = "One series of the result table.")
    @XmlRootElement
    public static class WrappedPrimitive {
        private double[] values;

        public WrappedPrimitive() {
        }

        public WrappedPrimitive(double[] values) {
            this.values = values;
        }

        // Wire name is "values", not the bean name.
        @Schema(name = "values",
                description = "Values of this series, one per entry in the response's timestamps array and in the "
                        + "same order. A sample with no data appears as the JSON string \"NaN\" rather than a "
                        + "number or null, so a strict numeric decoder will reject it.",
                example = "[9.85957792, 11.960674583333336, \"NaN\"]")
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

    @Schema(name = "QueryConstant",
            description = "One string property of a queried resource.")
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
        @Schema(name = "key",
                description = "Property name, prefixed with the label of the source it was fetched for.",
                example = "resp.ifName")
        public String getKey() {
            return this.key;
        }
        // The bare Jackson 2 @JsonProperty only keeps the property in the generated OpenAPI document;
        // swagger's ModelResolver drops @XmlValue members of an XmlAccessType.NONE class. Wire name is "value".
        @com.fasterxml.jackson.annotation.JsonProperty
        @Schema(name = "value",
                description = "Property value.",
                example = "eth0")
        public String getValue() {
            return this.value;
        }
    }
}
