/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.Marshaller;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.web.svclayer.RrdSummaryService;
import org.opennms.web.svclayer.SummarySpecification;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractFormController;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class RrdSummaryController extends AbstractFormController implements InitializingBean {
	
	static class MarshalledView implements View {

		public String getContentType() {
			return "text/xml";
		}

		public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			Assert.notNull(model.get("summary"), "summary must not be null.. unable to marshall xml");
			Marshaller.marshal(model.get("summary"), response.getWriter());
		}
		
	}
	
	private RrdSummaryService m_rrdSummaryService;
	

	public RrdSummaryController() {
	    super();
	    setCommandClass(SummarySpecification.class);
	}

    private ModelAndView getSummary(SummarySpecification spec) {
        Summary summary = m_rrdSummaryService.getSummary(spec);
        return new ModelAndView(new MarshalledView(), "summary", summary);
    }
	
	


	public void afterPropertiesSet() throws Exception {
		Assert.state(m_rrdSummaryService != null, "rrdSummaryService must be set");
	}


	public void setRrdSummaryService(RrdSummaryService rrdSummaryService) {
		m_rrdSummaryService = rrdSummaryService;
	}

    @Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception {
        return getSummary((SummarySpecification)command);
        
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request,
            HttpServletResponse response, BindException errors)
            throws Exception {
        throw new UnsupportedOperationException("RrdSummaryController.showForm is not yet implemented");
    }



}
