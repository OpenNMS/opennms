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
// Created: November 11th, 2009 jonathan@opennms.org
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.web.command.ManageDatabaseReportCommand;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ManageDatabaseReportController extends SimpleFormController {

    private int m_pageSize;
    private ReportStoreService m_reportStoreService;
    
    public ManageDatabaseReportController() {
        setFormView("report/database/manage");
    }

    public void setReportStoreService(ReportStoreService reportStoreService) {
        m_reportStoreService = reportStoreService;
    }
    
    public void setPageSize(int pageSize) {
        m_pageSize = pageSize;
    }
    
    @Override
    protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        PagedListHolder pagedListHolder = new PagedListHolder(m_reportStoreService.getAll());
        pagedListHolder.setPageSize(m_pageSize);
        int page = ServletRequestUtils.getIntParameter(req, "p", 0);
        pagedListHolder.setPage(page); 
        data.put("pagedListHolder", pagedListHolder);
        return data;

    }
    
    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        ManageDatabaseReportCommand manageCommand = (ManageDatabaseReportCommand) command;
        m_reportStoreService.delete(manageCommand.getIds());
        ModelAndView mav = new ModelAndView(getSuccessView());
        return mav;
    }
    
    
}