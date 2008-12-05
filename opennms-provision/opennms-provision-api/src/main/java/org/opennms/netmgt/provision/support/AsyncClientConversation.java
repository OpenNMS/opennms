/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.support;

import java.util.ArrayList;
import java.util.List;



/**
 * @author Donald Desloge
 *
 */
public class AsyncClientConversation<Request> {
    
    public static interface ResponseValidator{
        boolean validate(Object message);
    }
    
    public static interface AsyncExchange{

        /**
         * @param message
         * @return
         */
        boolean validateResponse(Object message);

        /**
         * @return
         */
        Object getRequest();
        
       
        
    }
    
    public static class AsyncExchangeImpl implements AsyncExchange{
        
        private Object m_request;
        private ResponseValidator m_responseValidator;

        public AsyncExchangeImpl(Object request, ResponseValidator responseValidator) {
            m_request = request;
            m_responseValidator = responseValidator;
        }
        
        public Object getRequest() {

            return m_request;
        }

        public boolean validateResponse(Object message) {
            return m_responseValidator.validate(message);
        }
        
    }
    
    private List<AsyncExchange> m_conversation = new ArrayList<AsyncExchange>();
    private boolean m_isComplete = false;
    private ResponseValidator m_bannerValidator;
    
    /**
     * 
     */
    public void addExchange(AsyncExchange request) {
        m_conversation.add(request);
    }

    /**
     * @return
     */
    public boolean hasBannerValidator() {
        return m_bannerValidator != null;
    }

    /**
     * @param message
     * @return
     */
    public boolean validateBanner(Object message) {
        boolean retVal = m_bannerValidator.validate(message);
        m_bannerValidator = null;
        return retVal;
    }

    /**
     * @return
     */
    public boolean hasExchanges() {
        return !m_conversation.isEmpty();
    }

    /**
     * @return
     */
    public boolean isComplete() {
        return m_isComplete;
    }

    /**
     * @param message
     * @return
     */
    public Object validate(Object message) {
        AsyncExchange ex = m_conversation.remove(0);
        
        if(m_conversation.isEmpty()) {
            m_isComplete = true;
        }
        
        return ex.validateResponse(message) ? ex.getRequest() : null;
    }

    /**
     * @param bannerValidator
     */
    public void expectBanner(ResponseValidator bannerValidator) {
        m_bannerValidator = bannerValidator;        
    }

    /**
     * @return
     */
    public Object getRequest() {
        return m_conversation.remove(0).getRequest();
    }
    
   
}
