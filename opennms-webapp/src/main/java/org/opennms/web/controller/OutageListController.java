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
 * 2007 Dec 09: Pass CategoryDao to OutagesFilteringView. - dj@opennms.org
 * 2007 Feb 01: Standardize on successView for the view name, cleanup unused code, deduplicate code, and use OnmsCriteria for filtering. - dj@opennms.org
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
 */
package org.opennms.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.extremecomponents.table.context.Context;
import org.extremecomponents.table.context.HttpServletRequestContext;
import org.extremecomponents.table.limit.Limit;
import org.extremecomponents.table.limit.LimitFactory;
import org.extremecomponents.table.limit.TableLimit;
import org.extremecomponents.table.limit.TableLimitFactory;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.outage.CurrentOutageParseResponse;
import org.opennms.web.svclayer.outage.OutageListBuilder;
import org.opennms.web.svclayer.outage.OutageService;
import org.opennms.web.svclayer.outage.OutagesFilteringView;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class OutageListController extends AbstractController implements InitializingBean {
    private static final int ROW_LIMIT = 25;

    private OutageService m_outageService;

    private OutageListBuilder m_cview = new OutageListBuilder();

    private String m_successView;

    private int m_defaultRowsDisplayed = ROW_LIMIT;

    private CategoryDao m_categoryDao;

    public void setOutageService(OutageService service) {
        m_outageService = service;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse reply) throws Exception {
        Context context = new HttpServletRequestContext(request);
        LimitFactory limitFactory = new TableLimitFactory(context, "tabledata");
        Limit limit = new TableLimit(limitFactory);

        CurrentOutageParseResponse.findSelectedOutagesIDs(request,m_outageService);

        Map<String, Object> myModel = new HashMap<String, Object>();

        myModel.put("request", limit.toString());

        myModel.put("all_params", request.getParameterNames().toString());
        
        Integer rowstart;
        Integer rowend;
        if (limit.getPage() == 1) {
            // no offset set
            rowstart = 0;
            rowend = getDefaultRowsDisplayed();

            context.setRequestAttribute("rowStart", rowstart);
            context.setRequestAttribute("rowEnd", rowend);
        } else {
            //quirky situation... - as we started on 0 (zero)
            rowstart = ((limit.getPage() * getDefaultRowsDisplayed() +1 ) - getDefaultRowsDisplayed());
            rowend = getDefaultRowsDisplayed();
        }
        
        myModel.put("rowStart", rowstart);
        myModel.put("rowEnd", rowend);
        myModel.put("begin", rowstart);
        myModel.put("end", rowend);

        String orderProperty;
        String sortOrder;
        
        if (limit.getSort().getProperty() == null) {
            orderProperty = null;
            sortOrder = "asc";
        } else {
            orderProperty = limit.getSort().getProperty();
            sortOrder = limit.getSort().getSortOrder();
        }
        
        OutagesFilteringView filterView = new OutagesFilteringView();
        filterView.setCategoryDao(m_categoryDao);
        
        OnmsCriteria criteria = filterView.buildCriteria(request);
        OnmsCriteria countCriteria = filterView.buildCriteria(request);
        
        Integer totalRows = m_outageService.getOutageCount(countCriteria);
        Collection<OnmsOutage> foundOutages = m_outageService.getOutagesByRange(rowstart, rowend, orderProperty, sortOrder, criteria);

        // Pretty smart to build the collection after any suppressions..... 
        Collection theTable = m_cview.theTable(foundOutages);

        myModel.put("tabledata", theTable);
        myModel.put("totalRows", totalRows);

        myModel.put("selected_outages", CurrentOutageParseResponse.findSelectedOutagesIDs(request,m_outageService));
        return new ModelAndView(getSuccessView(), myModel);
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    public String getSuccessView() {
        return m_successView;
    }

    public void setDefaultRowsDisplayed(int defaultRowsDisplayed) {
        m_defaultRowsDisplayed = defaultRowsDisplayed;
    }
    
    public int getDefaultRowsDisplayed() {
        return m_defaultRowsDisplayed;
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_categoryDao, "categoryDao property must be set");
    }

}
