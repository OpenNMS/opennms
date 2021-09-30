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

package org.opennms.core.rpc.utils.mate;

import java.util.Objects;

import com.google.common.collect.ComparisonChain;

public final class ContextKey implements Comparable<ContextKey> {
    public final String context;
    public final String key;

    public ContextKey(final String context, final String key) {
        this.context = Objects.requireNonNull(context);
        this.key = Objects.requireNonNull(key);
    }

    public ContextKey(final String contextKey) {
        Objects.requireNonNull(contextKey , "contextKey must not be null");
        final String arr[] = contextKey.split(":");

        if (arr.length != 2) {
            throw new IllegalArgumentException("contextKey '" + contextKey + "' must be in the format 'context:key'");
        }

        this.context = arr[0];
        this.key = arr[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContextKey)) {
            return false;
        }
        final ContextKey that = (ContextKey) o;
        return Objects.equals(this.context, that.context) &&
                Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, key);
    }

    public String getContext() {
        return this.context;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public int compareTo(final ContextKey that) {
        return ComparisonChain.start()
                .compare(this.context, that.context)
                .compare(this.key, that.key)
                .result();
    }
}
