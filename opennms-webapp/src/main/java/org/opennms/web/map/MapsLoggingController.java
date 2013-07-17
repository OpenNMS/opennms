/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 */
import java.util.concurrent.Callable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.logging.Logging;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * <p>AddMapsController class.</p>
 *
 * @author mmigliore
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class MapsLoggingController implements Controller, ServletContextAware {

    private ServletContext m_servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        m_servletContext = servletContext;
    }
    
    public ServletContext getServletContext() {
        return m_servletContext;
    }


    /** {@inheritDoc} */
    @Override
    final public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return Logging.withPrefix(MapsConstants.LOG4J_CATEGORY, new Callable<ModelAndView>() {

            @Override
            public ModelAndView call() throws Exception {
                return handleRequestInternal(request, response);
            }

        });

    }

    abstract protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
