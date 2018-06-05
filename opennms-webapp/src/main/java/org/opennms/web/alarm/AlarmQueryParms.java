/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.alarm;

import org.opennms.web.filter.Filter;
import org.opennms.web.filter.NormalizedAcknowledgeType;
import org.opennms.web.filter.QueryParameters;

import java.util.List;

/**
 * Convenience data structure for holding the arguments to an event query.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @deprecated use {@link QueryParameters} instead.
 */
@Deprecated
public class AlarmQueryParms implements QueryParameters {
    public SortStyle sortStyle;

    public AcknowledgeType ackType;

    public List<Filter> filters;

    public int limit;

    public int multiple;
    
    public String display;

    @Override
    public String getSortStyleShortName() {
        return sortStyle != null  ? sortStyle.getShortName() : null;
    }

    @Override
    public NormalizedAcknowledgeType getAckType() {
        return ackType != null ? ackType.toNormalizedAcknowledgeType() : null;
    }

    @Override
    public List<Filter> getFilters() {
        return this.filters;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getMultiple() {
        return multiple;
    }

    @Override
    public String getDisplay() {
        return display;
    }
}
