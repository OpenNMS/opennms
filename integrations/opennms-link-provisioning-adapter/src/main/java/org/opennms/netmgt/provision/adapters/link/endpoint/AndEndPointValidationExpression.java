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
 * <p>AndEndPointValidationExpression class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

@XmlRootElement(name="and")
public class AndEndPointValidationExpression extends EndPointValidationExpressionImpl {
    @XmlElementRef
    private List<EndPointValidationExpressionImpl> m_validators = Collections.synchronizedList(new ArrayList<EndPointValidationExpressionImpl>());

    /**
     * <p>Constructor for AndEndPointValidationExpression.</p>
     */
    public AndEndPointValidationExpression() {
    }

    /**
     * <p>Constructor for AndEndPointValidationExpression.</p>
     *
     * @param validators an array of {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} objects.
     */
    public AndEndPointValidationExpression(EndPointValidationExpressionImpl[] validators) {
        for (EndPointValidationExpressionImpl e : validators) {
            m_validators.add(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void validate(EndPoint endPoint) throws EndPointStatusException {
        for(EndPointValidationExpression validator : m_validators) {
            validator.validate(endPoint);
        }
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("and(");
        boolean first = true;
        for(EndPointValidationExpression validator : m_validators) {
            if(first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(validator.toString());
        }
        
        return sb.toString();
    }
}
