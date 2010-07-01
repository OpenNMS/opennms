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
 * 2007 Dec 12: More dependency injection and remove (seemingly) unneeded
 *              objects from the model. - dj@opennms.org
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
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.outage.OutageListBuilder;
import org.opennms.web.svclayer.outage.OutageService;
import org.opennms.web.svclayer.outage.OutagesFilteringView;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>OutageListController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class OutageListController extends AbstractController implements InitializingBean {
    private static final int ROW_LIMIT = 25;

    private OutageService m_outageService;

    private OutageListBuilder m_outageListBuilder;

    private String m_successView;

    private int m_defaultRowsDisplayed = ROW_LIMIT;

    private OutagesFilteringView m_filterView;

    /**
     * <p>setOutageService</p>
     *
     * @param service a {@link org.opennms.web.svclayer.outage.OutageService} object.
     */
    public void setOutageService(OutageService service) {
        m_outageService = service;
    }
    
    /**
     * <p>setFilterView</p>
     *
     * @param filterView a {@link org.opennms.web.svclayer.outage.OutagesFilteringView} object.
     */
    public void setFilterView(OutagesFilteringView filterView) {
        m_filterView = filterView;
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse reply) throws Exception {
        Context context = new HttpServletRequestContext(request);
        LimitFactory limitFactory = new TableLimitFactory(context, "tabledata");
        Limit limit = new TableLimit(limitFactory);

        Map<String, Object> myModel = new HashMap<String, Object>();

//        myModel.put("request", limit.toString());

//        myModel.put("all_params", request.getParameterNames().toString());
        
        Integer rowstart;
        Integer rowend;
        if (limit.getPage() == 1) {
            // no offset set
            rowstart = 0;
            rowend = getDefaultRowsDisplayed();
//
//            context.setRequestAttribute("rowStart", rowstart);
//            context.setRequestAttribute("rowEnd", rowend);
        } else {
            //quirky situation... - as we started on 0 (zero)
            rowstart = ((limit.getPage() * getDefaultRowsDisplayed() + 1) - getDefaultRowsDisplayed());
            rowend = getDefaultRowsDisplayed();
        }
        
//        myModel.put("rowStart", rowstart);
//        myModel.put("rowEnd", rowend);
//        myModel.put("begin", rowstart);
//        myModel.put("end", rowend);

        OnmsCriteria criteria = m_filterView.buildCriteria(request);
        OnmsCriteria countCriteria = m_filterView.buildCriteria(request);
        
        String orderProperty;
        String sortOrder;
        
        if (limit.getSort().getProperty() == null) {
            orderProperty = "outageid";
            sortOrder = "desc";
        } else {
            orderProperty = limit.getSort().getProperty();
            sortOrder = limit.getSort().getSortOrder();
        }
        
        Collection<OnmsOutage> foundOutages = m_outageService.getOutagesByRange(rowstart, rowend, orderProperty, sortOrder, criteria);

        myModel.put("tabledata", m_outageListBuilder.theTable(foundOutages));
        myModel.put("totalRows", m_outageService.getOutageCount(countCriteria)); // used by org.extremecomponents.table.callback.LimitCallback.retrieveRows

        //myModel.put("selected_outages", CurrentOutageParseResponse.findSelectedOutagesIDs(request,m_outageService));
        
        return new ModelAndView(getSuccessView(), myModel);
    }

    /**
     * <p>setSuccessView</p>
     *
     * @param successView a {@link java.lang.String} object.
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    /**
     * <p>getSuccessView</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuccessView() {
        return m_successView;
    }

    /**
     * <p>setDefaultRowsDisplayed</p>
     *
     * @param defaultRowsDisplayed a int.
     */
    public void setDefaultRowsDisplayed(int defaultRowsDisplayed) {
        m_defaultRowsDisplayed = defaultRowsDisplayed;
    }
    
    /**
     * <p>getDefaultRowsDisplayed</p>
     *
     * @return a int.
     */
    public int getDefaultRowsDisplayed() {
        return m_defaultRowsDisplayed;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_filterView, "filterView property must be set");
        Assert.notNull(m_outageListBuilder, "outageListBuilder property must be set");
    }

    /**
     * <p>setOutageListBuilder</p>
     *
     * @param outageListBuilder a {@link org.opennms.web.svclayer.outage.OutageListBuilder} object.
     */
    public void setOutageListBuilder(OutageListBuilder outageListBuilder) {
        m_outageListBuilder = outageListBuilder;
    }

}
