/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class MetricValidator {

    private final Set<Tag> tags;
    private final Set<Tag> metaTags;

    public MetricValidator(final Set<Tag> tags, final Set<Tag> metaTags) {
        this.tags = tags;
        this.metaTags = metaTags;
    }

    public Set<Tag> getTagsByKey(final String key) {
        return tags.stream().filter(t -> Objects.equals(t.getKey(), key)).collect(Collectors.toSet());
    }

    public void validate() {
        requireNonNullTagSets();
        requireAllMandatoryTagsArePresent();
        requireMtypeToHaveValidValue();
    }

    private void requireNonNullTagSets() {
        Objects.requireNonNull(tags);
        Objects.requireNonNull(metaTags);
    }

    private void requireAllMandatoryTagsArePresent() {
        for(Metric.MandatoryTag tag: Metric.MandatoryTag.values()) {
            if(getTagsByKey(tag.name()).size() <1) {
                throw new IllegalArgumentException(String.format("Mandatory tag %s missing", tag.name()));
            }
            if(getTagsByKey(tag.name()).size() >1) {
                throw new IllegalArgumentException(String.format("Mandatory tag %s can be defined only once", tag.name()));
            }
        }
    }

    private void requireMtypeToHaveValidValue() {
        final Tag tag = getTagsByKey(Metric.MandatoryTag.mtype.name()).iterator().next();
        if (Arrays.stream(Metric.Mtype.values()).noneMatch(mtype -> mtype.name().equals(tag.getValue()))) {
            throw new IllegalArgumentException(String.format("Tag value=%s for key=%s is not valid. Valid values: %s",
                    tag.getValue(), tag.getKey(), Arrays.toString(Metric.Mtype.values())));
        }
    }
}
