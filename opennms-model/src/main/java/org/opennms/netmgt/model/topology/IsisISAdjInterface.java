/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;


import org.apache.commons.lang.builder.ToStringBuilder;

public class IsisISAdjInterface {
	
    private final String m_isisISAdjNeighSysId;
    private final Integer m_isisLocalIfIndex;
    private final Integer m_isisISAdjIndex;
    private final String m_isisISAdjNeighSNPAAddress;


    public IsisISAdjInterface(String isisISAdjSysId, Integer isisLocalIfIndex, String isisISAdjNeighSNPAAddress, Integer isisISAdjIndex) {
        super();
        m_isisISAdjNeighSysId = isisISAdjSysId;
        m_isisLocalIfIndex =isisLocalIfIndex;
        m_isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
        m_isisISAdjIndex = isisISAdjIndex;
    }
	
    /**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
    @Override
	public String toString() {
	    return new ToStringBuilder(this)
	    .append("isisISAdjNeighSysID", m_isisISAdjNeighSysId)
	    .append("isisLocalIfIndex", m_isisLocalIfIndex)
	    .append("isisISAdjNeighSNPAAddress", m_isisISAdjNeighSNPAAddress)
            .append("isisISAdjIndex", m_isisISAdjIndex)
	    .toString();
	}

    public String getIsisISAdjNeighSysId() {
        return m_isisISAdjNeighSysId;
    }

    public Integer getIsisLocalIfIndex() {
        return m_isisLocalIfIndex;
    }

    public String getIsisISAdjNeighSNPAAddress() {
        return m_isisISAdjNeighSNPAAddress;
    }

    public Integer getIsisISAdjIndex() {
        return m_isisISAdjIndex;
    }
}

