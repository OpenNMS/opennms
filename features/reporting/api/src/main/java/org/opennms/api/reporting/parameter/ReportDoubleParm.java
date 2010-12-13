//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: Devember 13th jonathan@opennms.org
//
// Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.api.reporting.parameter;

import java.io.Serializable;

/**
 * <p>ReportFloatParm class.</p>
 *
 * @author jonathan@opennms.org
 * @version $Id: $
 */
public class ReportDoubleParm extends ReportParm implements Serializable {
    
    private static final long serialVersionUID = 5242917854258286117L;
    Double m_value;
    String m_type;
    
    /**
     * <p>Constructor for ReportFloatParm.</p>
     */
    public ReportDoubleParm() {
      super();
    }
    
    /**
     * <p>getValue</p>
     *
     * @return {@link java.lang.Float} object.
     */
    public Double getValue() {
        return m_value;
    }
    
    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.Float} object.
     */
    public void setValue(Double value) {
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
