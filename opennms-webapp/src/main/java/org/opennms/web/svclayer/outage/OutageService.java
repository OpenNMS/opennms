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

package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>OutageService interface.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional(readOnly = true)
public interface OutageService {

    /**
     * <p>getCurrentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getCurrentOutages();
    
    /**
     * <p>getOutagesByRange</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @param orderProperty a {@link java.lang.String} object.
     * @param direction a {@link java.lang.String} object.
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, OnmsCriteria criteria);

    /**
     * <p>getOutagesByRange</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @param orderProperty a {@link java.lang.String} object.
     * @param direction a {@link java.lang.String} object.
     * @param filter a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);
    
    /**
     * <p>getSuppressedOutagesByRange</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @param orderProperty a {@link java.lang.String} object.
     * @param direction a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getSuppressedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction);    
    
    /**
     * <p>getSuppressedOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getSuppressedOutages();

    /**
     * <p>getCurrentOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getCurrentOutageCount();
    
    /**
     * <p>getOutageCount</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link java.lang.Integer} object.
     */
    Integer getOutageCount(OnmsCriteria criteria);

    /**
     * <p>getSuppressedOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getSuppressedOutageCount();

    /**
     * <p>getCurrentOutagesForNode</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getCurrentOutagesForNode(int nodeId);

    /**
     * <p>getNonCurrentOutagesForNode</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getNonCurrentOutagesForNode(int nodeId);

    /**
     * <p>getOutagesForNode</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getOutagesForNode(int nodeId);

    /**
     * <p>getOutagesForNode</p>
     *
     * @param nodeId a int.
     * @param time a java$util$Date object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getOutagesForNode(int nodeId, Date time);

    /**
     * <p>getOutagesForInterface</p>
     *
     * @param nodeId a int.
     * @param ipInterface a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipInterface);

    /**
     * <p>getOutagesForInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipAddr, Date time);

    /**
     * <p>getOutagesForService</p>
     *
     * @param nodeId a int.
     * @param ipInterface a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getOutagesForService(int nodeId, String ipInterface, int serviceId);
    
    /**
     * <p>getOutagesForService</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param time a java$util$Date object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage>  getOutagesForService(int nodeId, String ipAddr, int serviceId, Date time);

    /**
     * <p>getCurrentOutages</p>
     *
     * @param orderProperty a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getCurrentOutages(String orderProperty);

    /**
     * <p>load</p>
     *
     * @param outageid a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsOutage} object.
     */
    OnmsOutage load(Integer outageid);

    /**
     * <p>update</p>
     *
     * @param outage a {@link org.opennms.netmgt.model.OnmsOutage} object.
     */
    void update(OnmsOutage outage);

    /**
     * <p>getOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getOutageCount();

    /**
     * <p>outageCountFiltered</p>
     *
     * @param filter a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    Integer outageCountFiltered(String filter);

    /**
     * <p>getResolvedOutagesByRange</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @param orderProperty a {@link java.lang.String} object.
     * @param direction a {@link java.lang.String} object.
     * @param filter a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);

    /**
     * <p>outageResolvedCountFiltered</p>
     *
     * @param searchFilter a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    Integer outageResolvedCountFiltered(String searchFilter);
	
    // This we may have to define 
    /*
    OutageSummary[] getCurrentOutageSummaries() ;

    OutageSummary[] getCurrentSDSOutageSummaries() ;
    */
    
}
