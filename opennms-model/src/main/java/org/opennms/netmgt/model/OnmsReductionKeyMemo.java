/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.netmgt.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>Specific memo which is attached to every alarm with a matching reduction
 * key.</p>
 *
 * @author <a href="mailto:Markus@OpenNMS.com">Markus Neumann</a>
 */
@XmlRootElement(name = "reductionKeyMemo")
@Entity
@DiscriminatorValue(value="ReductionKeyMemo")
public class OnmsReductionKeyMemo extends OnmsMemo {

    private static final long serialVersionUID = 7472348439687562161L;

    @Column(name = "reductionkey")
    private String m_reductionKey;

    public String getReductionKey() {
        return m_reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.m_reductionKey = reductionKey;
    }
    
}