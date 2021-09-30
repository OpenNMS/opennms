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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Query request attributes.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
@XmlRootElement(name = "query-request")
@XmlAccessorType(XmlAccessType.NONE)
public class QueryRequest {

    /**
     * Timestamp in ms.
     */
    private long start;

    /**
     * Timestamp in ms.
     */
    private long end;

    /**
     * Step size in ms.
     */
    private long step;

    /**
     * Maximum number of rows (i.e. pixel width of resulting graph)
     */
    private int maxrows = 0;

    /**
     * Interval in ms.
     */
    private Long interval;

    /**
     * Heartbeat in ms.
     */
    private Long heartbeat;

    /**
     * If relaxed is false a missing source attribute results in a 404.
     * If set to true, a missing source attribute does not result in a 404, but it filled with {@link Double#NaN}.
     */
    private boolean relaxed = false;

    private List<Source> sources = Lists.newArrayListWithCapacity(0);

    private List<Expression> expressions = Lists.newArrayListWithCapacity(0);

    private List<FilterDef> filters = Lists.newArrayListWithCapacity(0);

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

    @XmlAttribute(name = "step")
    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    @XmlAttribute(name = "maxrows")
    public int getMaxRows() {
        return maxrows;
    }

    public void setMaxRows(int maxrows) {
        this.maxrows = maxrows;
    }

    @XmlAttribute(name = "interval")
    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    @XmlAttribute(name = "heartbeat")
    public Long getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Long heartbeat) {
        this.heartbeat = heartbeat;
    }

    @XmlElement(name = "source")
    @JsonProperty("source")
    public List<Source> getSources() {
        return sources;
    }

    public void setSources(final List<Source> sources) {
        this.sources = sources;
    }

    @XmlElement(name = "expression")
    @JsonProperty("expression")
    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(final List<Expression> expressions) {
        this.expressions = expressions;
    }

    @XmlElement(name = "filter")
    public List<FilterDef> getFilters() {
        return filters;
    }

    public void setFilters(final List<FilterDef> filters) {
        this.filters = filters;
    }

    @XmlAttribute(name = "relaxed", required = false)
    public void setRelaxed(boolean relaxed) {
        this.relaxed = relaxed;
    }

    public boolean isRelaxed() {
        return relaxed;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final QueryRequest other = (QueryRequest) obj;

       return   com.google.common.base.Objects.equal(this.step, other.step)
             && com.google.common.base.Objects.equal(this.start, other.start)
             && com.google.common.base.Objects.equal(this.end, other.end)
             && com.google.common.base.Objects.equal(this.maxrows, other.maxrows)
             && com.google.common.base.Objects.equal(this.interval, other.interval)
             && com.google.common.base.Objects.equal(this.heartbeat, other.heartbeat)
             && com.google.common.base.Objects.equal(this.sources, other.sources)
             && com.google.common.base.Objects.equal(this.expressions, other.expressions)
             && com.google.common.base.Objects.equal(this.filters, other.filters)
             && com.google.common.base.Objects.equal(this.relaxed, other.relaxed);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.step, this.start, this.end, this.relaxed, this.maxrows, this.interval,
                 this.heartbeat ,this.sources, this.expressions, this.filters);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("Step", this.step)
                 .add("Start", this.start)
                 .add("End", this.end)
                 .add("Relaxed", this.relaxed)
                 .add("Max Rows", this.maxrows)
                 .add("Interval", this.interval)
                 .add("Heartbeat", this.heartbeat)
                 .add("Sources", this.sources)
                 .add("Expressions", this.expressions)
                 .add("Filters", this.filters)
                 .toString();
    }
}
