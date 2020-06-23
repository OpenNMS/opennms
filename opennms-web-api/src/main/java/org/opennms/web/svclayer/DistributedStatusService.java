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

package org.opennms.web.svclayer;

import java.util.Date;

import org.opennms.web.svclayer.model.DistributedStatusDetailsCommand;
import org.opennms.web.svclayer.model.DistributedStatusHistoryModel;
import org.opennms.web.svclayer.model.SimpleWebTable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

/**
 * <p>DistributedStatusService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly=true)
public interface DistributedStatusService {
    /**
     * <p>createStatusTable</p>
     *
     * @param command a {@link org.opennms.web.command.DistributedStatusDetailsCommand} object.
     * @param errors a {@link org.springframework.validation.Errors} object.
     * @return a {@link org.opennms.web.svclayer.model.SimpleWebTable} object.
     */
    public SimpleWebTable createStatusTable(DistributedStatusDetailsCommand command, Errors errors); 
    
    /**
     * <p>createFacilityStatusTable</p>
     *
     * @param startDate a java$util$Date object.
     * @param endDate a java$util$Date object.
     * @return a {@link org.opennms.web.svclayer.model.SimpleWebTable} object.
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
     * @return a {@link org.opennms.web.svclayer.model.DistributedStatusHistoryModel} object.
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
