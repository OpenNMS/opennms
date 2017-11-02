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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RRD parameters
 */
@XmlRootElement(name="rrd")
@XmlAccessorType(XmlAccessType.NONE)
public class Rrd implements org.opennms.netmgt.telemetry.config.api.Rrd {

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
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public List<String> getRras() {
        return rras;
    }

    public void setRras(List<String> rras) {
        this.rras = rras;
    }

    public String getBaseDir() {
        if (baseDir == null) {
            return DEFAULT_BASE_DIRECTORY;
        }
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rrd rrd = (Rrd) o;
        return Objects.equals(step, rrd.step) &&
                Objects.equals(rras, rrd.rras) &&
                Objects.equals(baseDir, rrd.baseDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(step, rras, baseDir);
    }

    @Override
    public String toString() {
        return "Rrd{" +
                "step=" + step +
                ", rras=" + rras +
                ", baseDir=" + baseDir +
                '}';
    }
}
