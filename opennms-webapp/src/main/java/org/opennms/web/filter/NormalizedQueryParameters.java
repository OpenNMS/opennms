/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.filter;

import java.util.ArrayList;
import java.util.List;

public class NormalizedQueryParameters implements QueryParameters {

    private String sortStyleShortName;
    private NormalizedAcknowledgeType ackType;
    private List<Filter> filters;
    private int limit;
    private int multiple;
    private String display;

    public NormalizedQueryParameters(QueryParameters queryParms) {
        this.sortStyleShortName = queryParms.getSortStyleShortName();
        this.ackType = queryParms.getAckType();
        this.display = queryParms.getDisplay();
        this.filters = new ArrayList<Filter>(queryParms.getFilters());
        this.limit = queryParms.getLimit();
        this.multiple = queryParms.getMultiple();
    }

    public NormalizedQueryParameters() {
        filters = new ArrayList<>();
    }

    @Override
    public String getSortStyleShortName() {
        return sortStyleShortName;
    }

    @Override
    public NormalizedAcknowledgeType getAckType() {
        return ackType;
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
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

    public void setSortStyleShortName(String sortStyleShortName) {
        this.sortStyleShortName = sortStyleShortName;
    }

    public void setAckType(NormalizedAcknowledgeType ackType) {
        this.ackType = ackType;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
