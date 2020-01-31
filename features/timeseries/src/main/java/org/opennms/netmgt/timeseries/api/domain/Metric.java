/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.api.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

/**
 * A piece of text that describes or identifies a metric. It is inspired by Metrics 2.0,
 * @link http://metrics20.org/spec/
 */
public class Metric {

    public enum MandatoryTag {
        /** See: https://github.com/metrics20/spec/blob/master/spec.md#tag-values-unit */
        unit,
        /** See: https://github.com/metrics20/spec/blob/master/spec.md#tag-values-mtype */
        mtype
    }

    /** See https://github.com/metrics20/spec/blob/master/spec.md#tag-values-mtype */
    public enum Mtype {
        rate, // 	a number per second (implies that unit ends on ‘/s’)
        count, // 	a number per a given interval (such as a statsd flushInterval)
        gauge, // 	values at each point in time
        counter, // 	keeps increasing over time (but might wrap/reset at some point) i.e. a gauge with the added notion of “i usually want to derive this to see the rate”
        timestamp//  	value represents a unix timestamp. so basically a gauge or counter but we know we can also render the “age” at each point.}
    }

    /** See: https://github.com/metrics20/spec/blob/master/spec.md#glossary */
    public enum TagType {
        /** Part of the metrics identity (key). Change a value and you get a different metric. */
        intrinsic,
        meta
    }

    private final String key;
    private final Set<Tag> tags;
    private final Set<Tag> metaTags;

    public Metric(final Set<Tag> tags) {
        this(tags, new HashSet<>());
    }

    public Metric(final Set<Tag> tags, final Set<Tag> metaTags) {
        new MetricValidator(tags, metaTags).validate();
        this.tags = ImmutableSet.copyOf(tags);
        this.metaTags = ImmutableSet.copyOf(metaTags);

        // create the key out of all tags => they form the composite key
        StringBuilder b = new StringBuilder();
        this.tags.stream().sorted().forEach(tag -> b.append("_").append(tag.toString()));
        this.key = b.substring(1);

    }

    public Set<Tag> getTagsByKey(final String key) {
        return tags.stream().filter(t -> Objects.equals(t.getKey(), key)).collect(Collectors.toSet());
    }

    public Tag getFirstTagByKey(final String key) {
        Set<Tag> tags = getTagsByKey(key);
        return (tags.size() > 0) ? tags.iterator().next() : null;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    /** Gets the composite key consisting of all tags. */
    public String getKey() {
        return this.key;
    }

    public Set<Tag> getMetaTags() {
        return metaTags;
    }

    // the metric (timeseries) identity is directly tied to the metric key (if any) and tags values (but not their order).
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return Objects.equals(tags, metric.tags);
    }

    // the metric (timeseries) identity is directly tied to the metric key (if any) and tags values (but not their order).
    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tags", tags)
                .add("metaTags", metaTags)
                .toString();
    }

    public static MetricBuilder builder() {
        return new MetricBuilder();
    }

    public final static class MetricBuilder {
        private final Set<Tag> tags = new HashSet<>();
        private final Set<Tag> metaTags = new HashSet<>();

        public MetricBuilder tag(Tag tag) {
            this.tags.add(tag);
            return this;
        }

        public MetricBuilder tag(String key, String value) {
            return this.tag(new Tag(key, value));
        }

        public MetricBuilder tag(MandatoryTag key, String value) {
            return this.tag(key.name(), value);
        }

        public MetricBuilder tag(String value) {
            return this.metaTag(new Tag(value));
        }

        public MetricBuilder metaTag(Tag tag) {
            this.metaTags.add(tag);
            return this;
        }

        public MetricBuilder metaTag(String key, String value) {
            return this.metaTag(new Tag(key, value));
        }

        public MetricBuilder metaTag(String value) {
            return this.metaTag(new Tag(value));
        }

        public Metric build() {
            return new Metric(this.tags, this.metaTags);
        }
    }

}
