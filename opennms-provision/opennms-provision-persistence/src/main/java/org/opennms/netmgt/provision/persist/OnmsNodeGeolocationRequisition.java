/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.persist;

import org.opennms.netmgt.provision.persist.requisition.RequisitionGeolocation;

/**
 * OnmsGeolocationRequisition
 *
 * @author brozow
 * @version $Id: $
 */
public class OnmsNodeGeolocationRequisition {

    private RequisitionGeolocation m_geolocation;

    /**
     * <p>Constructor for OnmsNodeGeolocationRequisition.</p>
     *
     * @param geolocation a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionGeolocation} object.
     */
    public OnmsNodeGeolocationRequisition(RequisitionGeolocation geolocation) {
        m_geolocation = geolocation;
    }

    /**
     * @return the geolocation
     */
    RequisitionGeolocation getGeolocation() {
        return m_geolocation;
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.provision.persist.RequisitionVisitor} object.
     */
    public void visit(RequisitionVisitor visitor) {
        visitor.visitNodeGeolocation(this);
        visitor.completeNodeGeolocation(this);
    }

    /**
     * <p>getLat</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public Double getLat() {
        return m_geolocation.getLat();
    }
   
   public Double getLon() {
        return m_geolocation.getLon();
    }
    
    

}
