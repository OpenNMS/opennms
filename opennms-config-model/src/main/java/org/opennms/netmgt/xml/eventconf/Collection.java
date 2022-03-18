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

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.collection.api.AttributeType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
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
    private String target = "node";

    @XmlAttribute(name = "step")
    private int step = 300;

    @XmlAttribute(name = "heartBeat")
    private int heartBeat = -1;

    @XmlElement(name = "rra", required = true)
    private List<String> rras = new ArrayList<>();

    @XmlElement(name = "paramValues")
    private List<String> paramValues = new ArrayList<>();

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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getHeartBeat() {
        return heartBeat == -1 ? this.getStep() * 2 : heartBeat;
    }

    public void setHeartBeat(int heartBeat) {
        this.heartBeat = heartBeat;
    }

    public List<String> getRras() {
        return rras;
    }

    public void setRras(List<String> rras) {
        this.rras = rras;
    }

    public List<String> getParamValues() {
        return paramValues;
    }

    public void setParamValues(List<String> paramValues) {
        this.paramValues = paramValues;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, target, step, rras);
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
                    Objects.equals(this.step, that.step) &&
                    Objects.equals(this.rras, that.rras);
        }
        return false;
    }

}
