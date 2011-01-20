
/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Dec 09: Format code, add getCriterionForGeolocationSetsUnion. - dj@opennms.org
 * 2007 Jul 03: Organize imports. - dj@opennms.org
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */
package org.opennms.netmgt.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;
import org.opennms.netmgt.dao.GeolocationDao;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsCriteria;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>GeolocationDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GeolocationDaoHibernate extends AbstractDaoHibernate<OnmsGeolocation, Integer> implements GeolocationDao {

    /**
     * <p>Constructor for GeolocationDaoHibernate.</p>
     */
    public GeolocationDaoHibernate() {
        super(OnmsGeolocation.class);
    }
    
    /** {@inheritDoc} */
    public OnmsGeolocation findByLocation(Double lat, Double lon) {
       return findUnique("from OnmsGeolocation as node_geolocation where node_geolocation.geolocationlatitude = ? and node_geolocation.geolocationLongitude = lon", lat, lon);
    }

   
   /** {@inheritDoc} */
   // @Override
   // protected String getKey(OnmsGeolocation geoloc) {
   //     return geoloc.getId().toString();
    //}
    


    
}
