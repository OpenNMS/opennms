/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.catstatus.model.StatusSection;
import org.opennms.web.svclayer.catstatus.support.DefaultCategoryStatusService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>CategoryStatusController class.</p>
 *
 * @author fastjay
 * @version $Id: $
 * @since 1.8.1
 */
public class CategoryStatusController extends AbstractController {

	DefaultCategoryStatusService m_categorystatusservice;
	Collection <StatusSection>statusSections;
	
	/** {@inheritDoc} */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		
		statusSections = m_categorystatusservice.getCategoriesStatus(); 
		ModelAndView modelAndView = new ModelAndView("displayCategoryStatus","statusTree",statusSections);
		
		return modelAndView;
	}

	
	/**
	 * <p>setCategoryStatusService</p>
	 *
	 * @param categoryStatusService a {@link org.opennms.web.svclayer.catstatus.support.DefaultCategoryStatusService} object.
	 */
	public void setCategoryStatusService(DefaultCategoryStatusService categoryStatusService){
		
		m_categorystatusservice = categoryStatusService;
		
		
	}
	
}
