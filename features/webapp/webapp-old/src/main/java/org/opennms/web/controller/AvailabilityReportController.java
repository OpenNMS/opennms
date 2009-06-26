// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.report.availability.svclayer.AvailabilityReportCriteria;
import org.opennms.report.availability.svclayer.AvailabilityReportService;
import org.opennms.web.svclayer.CategoryConfigService;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class AvailabilityReportController extends SimpleFormController {

    private AvailabilityReportService m_reportService;

    private CategoryConfigService m_categoryConfigService;
    
    private String m_logo;

    public AvailabilityReportController() {
        setCommandName("availabilityReportCriteria");
        setCommandClass(AvailabilityReportCriteria.class);
        setFormView("report/availability/report");
        setSuccessView("report/availability/running");
    }

    @Override
    protected Map referenceData(HttpServletRequest req) throws Exception {
        Map<String, List<String>> data = new HashMap<String, List<String>>();
        List<String> categories = new ArrayList<String>();
        Collection<Category> m_categories = m_categoryConfigService.getCategories();
        Iterator<Category> i = m_categories.iterator();
        while (i.hasNext()) {
            categories.add(i.next().getLabel());
        }
        data.put("categories", categories);
        return data;

    }

    @Override
    protected void initBinder(HttpServletRequest req,
            ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(
                                    Date.class,
                                    new CustomDateEditor(
                                                         new SimpleDateFormat(
                                                                              "yyyy-MM-dd"),
                                                         true));
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request)
            throws Exception {
        AvailabilityReportCriteria criteria = new AvailabilityReportCriteria();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        //cal.add(Calendar.YEAR, -1);
        criteria.setPeriodEndDate(new Date(cal.getTimeInMillis()));
        criteria.setFormat("SVG");
        criteria.setMonthFormat("classic");
        UserFactory.init();
        UserManager userFactory = UserFactory.getInstance();
        criteria.setEmail(userFactory.getEmail(request.getRemoteUser()));
        criteria.setPersist(true);
        // TODO: Set this up properly
        criteria.setLogo(m_logo);

        return criteria;

    }

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        AvailabilityReportCriteria criteria = (AvailabilityReportCriteria) command;
        m_reportService.runReport(criteria);
        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject("availabilityReportCriteria", criteria);
        return mav;
    }

    public void setAvailabilityReportService(
            AvailabilityReportService reportService) {
        this.m_reportService = reportService;
    }

    public void setCategoryConfigService(CategoryConfigService configService) {
        m_categoryConfigService = configService;
    }

    public void setLogo(String logo) {
        m_logo = logo;
    }

}
