/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.api.reporting.parameter;

import java.io.Serializable;

/**
 * <p>ReportIntParm class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportIntParm extends ReportParm implements Serializable {
    
    private static final long serialVersionUID = 4587512112540000518L;
    
    int m_value;
    String m_type;
    
    /**
     * <p>Constructor for ReportIntParm.</p>
     */
    public ReportIntParm() {
      super();
    }
    
    /**
     * <p>getValue</p>
     *
     * @return a int.
     */
    public int getValue() {
        return m_value;
    }
    
    /**
     * <p>setValue</p>
     *
     * @param value a int.
     */
    public void setValue(int value) {
        m_value = value;
    }
    
    /**
     * <p>getInputType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInputType() {
        return m_type;
    }
    /**
     * <p>setInputType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setInputType(String type) {
        m_type = type;
    }

}
