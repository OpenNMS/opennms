/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/**
 * <p>EndPointTypeValidator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;

@XmlRootElement(name="endpoint-types")
public class EndPointTypeValidator {
    @XmlAttribute(name="endpoint-service-name")
    String m_endPointServiceName = "EndPoint";
    
    @XmlElement(name="endpoint-type")
    List<EndPointType> m_endPointConfigs = Collections.synchronizedList(new ArrayList<EndPointType>());
    
    /**
     * <p>Constructor for EndPointTypeValidator.</p>
     */
    public EndPointTypeValidator() {
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_endPointServiceName;
    }

    /**
     * <p>setServiceName</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void setServiceName(String serviceName) {
        m_endPointServiceName = serviceName;
    }

    /**
     * <p>getConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<EndPointType> getConfigs() {
        return m_endPointConfigs;
    }
    
    /**
     * <p>setConfigs</p>
     *
     * @param configs a {@link java.util.List} object.
     */
    public void setConfigs(List<EndPointType> configs) {
        synchronized(m_endPointConfigs) {
            if (m_endPointConfigs == configs) return;
            m_endPointConfigs.clear();
            m_endPointConfigs.addAll(configs);
        }
    }

    /**
     * <p>hasMatch</p>
     *
     * @param ep a {@link org.opennms.netmgt.provision.adapters.link.EndPoint} object.
     * @return a boolean.
     */
    public boolean hasMatch(EndPoint ep) {
        for (EndPointType config : m_endPointConfigs) {
            if (config.matches(ep)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>validate</p>
     *
     * @param ep a {@link org.opennms.netmgt.provision.adapters.link.EndPoint} object.
     * @throws org.opennms.netmgt.provision.adapters.link.EndPointStatusException if any.
     */
    public void validate(EndPoint ep) throws EndPointStatusException {
        for (EndPointType config : m_endPointConfigs) {
            if (config.matches(ep)) {
                config.validate(ep);
                return;
            }
        }
        throw new EndPointStatusException(String.format("unable to find matching endpoint type config for endpoint %s", ep));
    }
}
