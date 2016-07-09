/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.camel.JaxbUtilsMarshalProcessor;
import org.opennms.core.camel.JaxbUtilsUnmarshalProcessor;

@XmlRootElement(name = "detector-response")
@XmlAccessorType(XmlAccessType.NONE)
public class DetectorResponseDTO {

    public static class Marshal extends JaxbUtilsMarshalProcessor {
        public Marshal() {
            super(DetectorResponseDTO.class);
        }
    }

    public static class Unmarshal extends JaxbUtilsUnmarshalProcessor {
        public Unmarshal() {
            super(DetectorResponseDTO.class);
        }
    }

    @XmlAttribute(name = "isDetected")
    private boolean isDetected;

    @XmlAttribute(name = "failureMessage")
    private String failureMesage;

    public boolean isDetected() {
        return isDetected;
    }

    public void setDetected(boolean isDetected) {
        this.isDetected = isDetected;
    }

    public String getFailureMesage() {
        return failureMesage;
    }

    public void setFailureMesage(String failureMesage) {
        this.failureMesage = failureMesage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((failureMesage == null) ? 0 : failureMesage.hashCode());
        result = prime * result + (isDetected ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetectorResponseDTO other = (DetectorResponseDTO) obj;
        if (failureMesage == null) {
            if (other.failureMesage != null)
                return false;
        } else if (!failureMesage.equals(other.failureMesage))
            return false;
        if (isDetected != other.isDetected)
            return false;
        return true;
    }

}
