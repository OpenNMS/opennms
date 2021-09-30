/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api.model;

import org.opennms.core.utils.ImmutableCollections;
import org.opennms.core.utils.MutableCollections;

import java.util.List;
import java.util.Objects;

/**
 * An immutable implementation of '{@link ICorrelation}'.
 */
public final class ImmutableCorrelation implements ICorrelation {
    private final String state;
    private final String path;
    private final List<String> cueiList;
    private final String cmin;
    private final String cmax;
    private final String ctime;

    private ImmutableCorrelation(Builder builder) {
        state = builder.state;
        path = builder.path;
        cueiList = ImmutableCollections.newListOfImmutableType(builder.cueiList);
        cmin = builder.cmin;
        cmax = builder.cmax;
        ctime = builder.ctime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilderFrom(ICorrelation correlation) {
        return new Builder(correlation);
    }

    public static ICorrelation immutableCopy(ICorrelation correlation) {
        if (correlation == null || correlation instanceof ImmutableCorrelation) {
            return correlation;
        }
        return newBuilderFrom(correlation).build();
    }

    public static final class Builder {
        private String state;
        private String path;
        private List<String> cueiList;
        private String cmin;
        private String cmax;
        private String ctime;

        private Builder() {
        }

        public Builder(ICorrelation correlation) {
            state = correlation.getState();
            path = correlation.getPath();
            cueiList = MutableCollections.copyListFromNullable(correlation.getCueiCollection());
            cmin = correlation.getCmin();
            cmax = correlation.getCmax();
            ctime = correlation.getCtime();
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setCueiList(List<String> cueiList) {
            this.cueiList = cueiList;
            return this;
        }

        public Builder setCmin(String cmin) {
            this.cmin = cmin;
            return this;
        }

        public Builder setCmax(String cmax) {
            this.cmax = cmax;
            return this;
        }

        public Builder setCtime(String ctime) {
            this.ctime = ctime;
            return this;
        }

        public ImmutableCorrelation build() {
            return new ImmutableCorrelation(this);
        }
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<String> getCueiCollection() {
        return cueiList;
    }

    @Override
    public String getCmin() {
        return cmin;
    }

    @Override
    public String getCmax() {
        return cmax;
    }

    @Override
    public String getCtime() {
        return ctime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableCorrelation that = (ImmutableCorrelation) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(path, that.path) &&
                Objects.equals(cueiList, that.cueiList) &&
                Objects.equals(cmin, that.cmin) &&
                Objects.equals(cmax, that.cmax) &&
                Objects.equals(ctime, that.ctime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, path, cueiList, cmin, cmax, ctime);
    }

    @Override
    public String toString() {
        return "ImmutableCorrelation{" +
                "state='" + state + '\'' +
                ", path='" + path + '\'' +
                ", cueiList=" + cueiList +
                ", cmin='" + cmin + '\'' +
                ", cmax='" + cmax + '\'' +
                ", ctime='" + ctime + '\'' +
                '}';
    }
}
