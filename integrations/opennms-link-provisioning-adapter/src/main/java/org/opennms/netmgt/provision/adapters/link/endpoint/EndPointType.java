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
 * <p>EndPointType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

@XmlRootElement(name="endpoint-type")
@XmlAccessorType(value=XmlAccessType.FIELD)
public class EndPointType {
    private static final Logger LOG = LoggerFactory.getLogger(EndPointType.class);
    @XmlAttribute(name="name")
    private String m_name;
    
    @XmlElement(name="sysoid-mask")
    private String m_sysOid;
    
    @XmlElementRef
    private EndPointValidationExpressionImpl m_validator;

    /**
     * <p>Constructor for EndPointType.</p>
     */
    public EndPointType() {
    }

    /**
     * <p>Constructor for EndPointType.</p>
     *
     * @param sysOid a {@link java.lang.String} object.
     * @param validator a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public EndPointType(String sysOid, EndPointValidationExpressionImpl validator) {
        setSysOid(sysOid);
        setValidator(validator);
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getValidator</p>
     *
     * @return a {@link org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression} object.
     */
    public EndPointValidationExpression getValidator() {
        return m_validator;
    }


    /**
     * <p>setValidator</p>
     *
     * @param validator a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public void setValidator(EndPointValidationExpressionImpl validator) {
        m_validator = validator;
    }


    /**
     * <p>getSysOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysOid() {
        return m_sysOid;
    }


    /**
     * <p>setSysOid</p>
     *
     * @param sysOid a {@link java.lang.String} object.
     */
    public void setSysOid(String sysOid) {
        m_sysOid = sysOid;
    }


    /**
     * <p>matches</p>
     *
     * @param ep a {@link org.opennms.netmgt.provision.adapters.link.EndPoint} object.
     * @return a boolean.
     */
    public boolean matches(EndPoint ep) {
        if (ep == null) {
            LOG.debug("EndPoint is null!");
            return false;
        }
        if (ep.getSysOid() == null) {
            LOG.debug("sysObjectId for endpoint {} is null", ep);
            return false;
        }
        if (ep.getSysOid().startsWith(getSysOid())) {
            return true;
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
        m_validator.validate(ep);
    }


}
