/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Created: February 28, 2007
 * Modifications:
 * 
 *
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
 */
package org.opennms.web.svclayer;

import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.web.svclayer.support.RtcNodeModel;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>RtcService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
@Transactional(readOnly = true)
public interface RtcService {
    /**
     * <p>getNodeList</p>
     *
     * @return a {@link org.opennms.web.svclayer.support.RtcNodeModel} object.
     */
    public RtcNodeModel getNodeList();
    /**
     * <p>getNodeListForCriteria</p>
     *
     * @param serviceCriteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @param outageCriteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link org.opennms.web.svclayer.support.RtcNodeModel} object.
     */
    public RtcNodeModel getNodeListForCriteria(OnmsCriteria serviceCriteria, OnmsCriteria outageCriteria);
    /**
     * <p>createServiceCriteria</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createServiceCriteria();
    /**
     * <p>createOutageCriteria</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createOutageCriteria();
}
