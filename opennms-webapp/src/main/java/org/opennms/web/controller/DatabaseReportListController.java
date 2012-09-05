/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DatabaseReportListService;
import org.opennms.web.svclayer.support.DatabaseReportDescription;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>DatabaseReportListController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DatabaseReportListController extends AbstractController {

    private DatabaseReportListService m_reportListService;
    private int m_pageSize;
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("report/database/list"); 
        //TODO Tak: insert logic to build "sub-model" for each repository
        PagedListHolder<DatabaseReportDescription> pagedListHolder = new PagedListHolder<DatabaseReportDescription>(m_reportListService.getAll());
        pagedListHolder.setPageSize(m_pageSize);
        int page = ServletRequestUtils.getIntParameter(request, "p", 0);
        pagedListHolder.setPage(page); 
        modelAndView.addObject("pagedListHolder", pagedListHolder);  

        return modelAndView;
    }

    /**
     * <p>getDatabaseReportListService</p>
     *
     * @return a {@link org.opennms.web.svclayer.DatabaseReportListService} object.
     */
    public DatabaseReportListService getDatabaseReportListService() {
        return m_reportListService;
    }

    /**
     * <p>setDatabaseReportListService</p>
     *
     * @param listService a {@link org.opennms.web.svclayer.DatabaseReportListService} object.
     */
    public void setDatabaseReportListService(DatabaseReportListService listService) {
        m_reportListService = listService;
    }

    /**
     * <p>getPageSize</p>
     *
     * @return a int.
     */
    public int getPageSize() {
        return m_pageSize;
    }

    /**
     * <p>setPageSize</p>
     *
     * @param pageSize a int.
     */
    public void setPageSize(int pageSize) {
        m_pageSize = pageSize;
    }

}
