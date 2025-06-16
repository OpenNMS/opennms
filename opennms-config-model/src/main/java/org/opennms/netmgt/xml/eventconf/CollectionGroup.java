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
import javax.xml.bind.annotation.XmlType;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "collectionGroup")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder = {"rrd", "collection"})
public class CollectionGroup implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "resourceType")
    private String resourceType = "nodeSnmp";

    @XmlAttribute(name = "instance")
    private String instance;

    @XmlElement(name = "rrd")
    private Rrd rrd;

    @XmlElement(name = "collection")
    private List<Collection> collection = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Rrd getRrd() {
        return this.rrd;
    }

    public void setRrd(final Rrd rrd) {
        this.rrd = rrd;
    }

    public List<Collection> getCollection() {
        return collection;
    }

    public void setCollection(List<Collection> collection) {
        this.collection = collection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resourceType, rrd);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CollectionGroup) {
            final CollectionGroup that = (CollectionGroup) obj;
            return Objects.equals(this.name, that.name) &&
                    Objects.equals(this.resourceType, that.resourceType) &&
                    Objects.equals(this.rrd, that.rrd) &&
                    Objects.equals(this.collection, that.collection);
        }
        return false;
    }

    @XmlRootElement(name = "rrd")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Rrd {
        private static final File DEFAULT_BASE_DIRECTORY = new File(ResourceTypeUtils.DEFAULT_RRD_ROOT, ResourceTypeUtils.SNMP_DIRECTORY);

        /**
         * Step size for the RRD, in seconds.
         */
        @XmlAttribute(name = "step")
        private Integer step;

        /**
         * HeartBeat of the RRD, default is step * 2
         */
        @XmlAttribute(name = "heartBeat")
        private Integer heartBeat = -1;

        /**
         * Round Robin Archive definitions
         */
        @XmlElement(name = "rra")
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

    @XmlRootElement(name = "collection")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Collection {
        @XmlAttribute(name = "name", required = true)
        private String name;

        @XmlAttribute(name = "rename")
        private String rename;

        @XmlAttribute(name = "type")
        private AttributeType type;

        @XmlElement(name = "paramValue")
        private List<ParamValue> paramValue = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRename() {
            return rename;
        }

        public void setRename(String rename) {
            this.rename = rename;
        }

        public AttributeType getType() {
            return type;
        }

        public void setType(AttributeType type) {
            this.type = type;
        }

        public List<ParamValue> getParamValue() {
            return paramValue;
        }

        public void setParamValue(List<ParamValue> paramValue) {
            this.paramValue = paramValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Collection that = (Collection) o;
            return Objects.equals(this.name, that.name) &&
                    Objects.equals(this.type, that.type) &&
                    Objects.equals(this.paramValue, that.paramValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.type, this.paramValue);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", this.name)
                    .add("type", this.type)
                    .add("paramValue", this.paramValue)
                    .toString();
        }
    }

    @XmlRootElement(name = "paramValue")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ParamValue {
        @XmlAttribute(name = "key", required = true)
        private String name;

        @XmlAttribute(name = "value", required = true)
        private Double value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ParamValue that = (ParamValue) o;
            return Objects.equals(this.name, that.name) &&
                    Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.value);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", this.name)
                    .add("type", this.value)
                    .toString();
        }
    }
}
