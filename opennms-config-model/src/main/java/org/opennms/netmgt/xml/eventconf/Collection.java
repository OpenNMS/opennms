/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import com.google.common.base.MoreObjects;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.model.ResourceTypeUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Collection implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "type", required = true)
    private AttributeType type;

    @XmlAttribute(name = "target")
    private String target = "nodeSnmp";

    @XmlAttribute(name = "instance")
    private String instance;

    @XmlElement(name = "paramValues")
    private List<String> paramValues = new ArrayList<>();

    @XmlElement(name = "rrd")
    private Rrd rrd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<String> getParamValues() {
        return paramValues;
    }

    public void setParamValues(List<String> paramValues) {
        this.paramValues = paramValues;
    }

    public Rrd getRrd() {
        return this.rrd;
    }

    public void setRrd(final Rrd rrd) {
        this.rrd = rrd;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, target, rrd);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Collection) {
            final Collection that = (Collection) obj;
            return Objects.equals(this.name, that.name) &&
                    Objects.equals(this.type, that.type) &&
                    Objects.equals(this.target, that.target) &&
                    Objects.equals(this.rrd, that.rrd);
        }
        return false;
    }

    @XmlRootElement(name="rrd")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Rrd {
        private static final File DEFAULT_BASE_DIRECTORY = new File(ResourceTypeUtils.DEFAULT_RRD_ROOT, ResourceTypeUtils.SNMP_DIRECTORY);

        /**
         * Step size for the RRD, in seconds.
         */
        @XmlAttribute(name="step")
        private Integer step;

        /**
         * HeartBeat of the RRD, default is step * 2
         */
        @XmlAttribute(name = "heartBeat")
        private Integer heartBeat = -1;

        /**
         * Round Robin Archive definitions
         */
        @XmlElement(name="rra")
        private List<String> rras = new ArrayList<>();

        public Integer getStep() {
            return this.step;
        }

        public void setStep(final Integer step) {
            this.step = step;
        }

        public int getHeartBeat() {
            return heartBeat == -1 ? this.getStep() * 2 : heartBeat;
        }

        public void setHeartBeat(int heartBeat) {
            this.heartBeat = heartBeat;
        }

        public List<String> getRras() {
            return this.rras;
        }

        public void setRras(final List<String> rras) {
            this.rras = rras;
        }

        public File getBaseDir() {
            return DEFAULT_BASE_DIRECTORY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Rrd that = (Rrd) o;
            return Objects.equals(this.step, that.step) &&
                    Objects.equals(this.rras, that.rras) &&
                    Objects.equals(this.heartBeat, that.heartBeat);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.step, this.rras, this.heartBeat);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("step", this.step)
                    .add("rras", this.rras)
                    .add("heartBeat", this.heartBeat)
                    .toString();
        }
    }

}
