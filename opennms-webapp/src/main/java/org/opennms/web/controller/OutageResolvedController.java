/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import org.opennms.web.svclayer.outage.OutageService;
import org.opennms.web.svclayer.outage.OutageTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

/**
 * <p>OutageResolvedController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class OutageResolvedController extends UrlFilenameViewController {
    
        private String m_successView;

        private OutageService m_outageService ;
        
        private OutageTable m_outageTable = new OutageTable();
        
        /**
         * <p>setOutageService</p>
         *
         * @param service a {@link org.opennms.web.svclayer.outage.OutageService} object.
         */
        public void setOutageService(OutageService service) {
                m_outageService = service;
        }

        
        /** {@inheritDoc} */
        @Override
        protected ModelAndView handleRequestInternal(HttpServletRequest request,
                HttpServletResponse reply) {
            
            
          return new ModelAndView(getSuccessView(),
                                  m_outageTable.getResolvedOutageTable(request, reply,m_outageService));
//          return new ModelAndView("displayResolvedOutages" + getSuffix(),
//          m_outageTable.getResolvedOutageTable(request, reply,m_outageService));
            
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

}
