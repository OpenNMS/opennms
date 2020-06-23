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

package org.opennms.netmgt.telemetry.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.config.api.PackageDefinition;
import org.opennms.netmgt.telemetry.config.api.RrdDefinition;

import com.google.common.base.MoreObjects;

@XmlRootElement(name="package")
@XmlAccessorType(XmlAccessType.NONE)
public class PackageConfig implements PackageDefinition {
    @XmlRootElement(name="filter")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Filter {
        @XmlValue
        private String content;

        public Filter() {
        }

        public Filter(final String content) {
            this.content = content;
        }

        public String getContent() {
            return this.content;
        }

        public void setContent(final String content) {
            this.content = content;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Filter that = (Filter) o;
            return Objects.equals(this.content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.content);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .addValue(this.content)
                    .toString();
        }
    }

    @XmlRootElement(name="rrd")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Rrd implements RrdDefinition {

        private static final String DEFAULT_BASE_DIRECTORY = Paths.get(System.getProperty("opennms.home"),"share","rrd","snmp").toString();

        /**
         * Step size for the RRD, in seconds.
         */
        @XmlAttribute(name="step")
        private Integer step;

        /**
         * Round Robin Archive definitions
         */
        @XmlElement(name="rra")
        private List<String> rras = new ArrayList<>();

        @XmlAttribute(name="base-directory")
        private String baseDir;

        public Integer getStep() {
            return this.step;
        }

        public void setStep(final Integer step) {
            this.step = step;
        }

        public List<String> getRras() {
            return this.rras;
        }

        public void setRras(final List<String> rras) {
            this.rras = rras;
        }

        public String getBaseDir() {
            if (this.baseDir == null) {
                return DEFAULT_BASE_DIRECTORY;
            }
            return this.baseDir;
        }

        public void setBaseDir(final String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Rrd that = (Rrd) o;
            return Objects.equals(this.step, that.step) &&
                    Objects.equals(this.rras, that.rras) &&
                    Objects.equals(this.baseDir, that.baseDir);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.step, this.rras, this.baseDir);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("step", this.step)
                    .add("rras", this.rras)
                    .add("baseDir", this.baseDir)
                    .toString();
        }
    }

    /**
     * Name or identifier for this package.
     */
    @XmlAttribute(name="name")
    private String name;

    /**
     * A rule which addresses belonging to this package must pass. This
     * package is applied only to addresses that pass this filter.
     */
    @XmlElement(name="filter")
    private Filter filter;

    /**
     * RRD parameters for metrics belonging to this package.
     */
    @XmlElement(name="rrd")
    private Rrd rrd;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setFilter(final Filter filter) {
        this.filter = filter;
    }

    @Override
    public String getFilterRule() {
        if (this.filter == null) {
            return null;
        }
        return this.filter.getContent();
    }

    @Override
    public Rrd getRrd() {
        return this.rrd;
    }

    public void setRrd(final Rrd rrd) {
        this.rrd = rrd;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PackageConfig that = (PackageConfig) o;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.filter, that.filter) &&
                Objects.equals(this.rrd, that.rrd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.filter, this.rrd);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("filter", this.filter)
                .add("rrd", this.rrd)
                .toString();
    }

}
