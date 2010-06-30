/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 6, 2006
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

import java.util.Date;

import org.opennms.web.command.DistributedStatusDetailsCommand;
import org.opennms.web.svclayer.support.DistributedStatusHistoryModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

/**
 * <p>DistributedStatusService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
@Transactional(readOnly=true)
public interface DistributedStatusService {
    /**
     * <p>createStatusTable</p>
     *
     * @param command a {@link org.opennms.web.command.DistributedStatusDetailsCommand} object.
     * @param errors a {@link org.springframework.validation.Errors} object.
     * @return a {@link org.opennms.web.svclayer.SimpleWebTable} object.
     */
    public SimpleWebTable createStatusTable(DistributedStatusDetailsCommand command, Errors errors); 
    
    /**
     * <p>createFacilityStatusTable</p>
     *
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @return a {@link org.opennms.web.svclayer.SimpleWebTable} object.
     */
    public SimpleWebTable createFacilityStatusTable(Date startDate, Date endDate);

    /**
     * <p>createHistoryModel</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @param monitorId a {@link java.lang.String} object.
     * @param applicationName a {@link java.lang.String} object.
     * @param timeSpan a {@link java.lang.String} object.
     * @param previousLocation a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DistributedStatusHistoryModel} object.
     */
    public DistributedStatusHistoryModel createHistoryModel(String locationName,
            String monitorId, String applicationName, String timeSpan,
            String previousLocation);

    /**
     * <p>getApplicationCount</p>
     *
     * @return a int.
     */
    public int getApplicationCount();
}
