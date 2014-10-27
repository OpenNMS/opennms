/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
    @Override
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
