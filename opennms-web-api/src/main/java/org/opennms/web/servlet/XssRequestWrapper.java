/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.servlet;

// from http://mc4j.org/confluence/display/stripes/XSS+filter

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.opennms.core.utils.WebSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>XssRequestWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class XssRequestWrapper extends HttpServletRequestWrapper
{
	
	private static final Logger LOG = LoggerFactory.getLogger(XssRequestWrapper.class);

    private Map<String, String[]> sanitized_parameters;
    private Map<String, String[]> original_parameters;
    
    /**
     * <p>Constructor for XssRequestWrapper.</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     */
    public XssRequestWrapper(HttpServletRequest req) 
    {
        super(req);
        original_parameters = req.getParameterMap();
        sanitized_parameters = getParameterMap();
        snzLogger();
    }       

    /** {@inheritDoc} */
    @Override
    public String getParameter(String name) 
    {       
        String[] vals = getParameterMap().get(name); 
        if (vals != null && vals.length > 0) 
            return vals[0];
        else        
            return null;        
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String[]> getParameterMap() 
    {   
        if (sanitized_parameters==null)
            sanitized_parameters = sanitizeParamMap(original_parameters);
        return sanitized_parameters;           

    }

    /** {@inheritDoc} */
    @Override
    public String[] getParameterValues(String name)
    {   
        return getParameterMap().get(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public void removeAttribute(String name) {
        super.getRequest().removeAttribute(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAttribute(String name, Object o) {
        super.getRequest().setAttribute(name, o);
    }
    
    /** {@inheritDoc} */
    @Override
    public Object getAttribute(String name) {
        return super.getRequest().getAttribute(name);
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        super.getRequest().setCharacterEncoding(enc);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getCharacterEncoding() {
        return super.getRequest().getCharacterEncoding();
    }

    private static Map<String, String[]> sanitizeParamMap(Map<String, String[]> raw) 
    {       
        Map<String, String[]> res = new HashMap<String, String[]>();
        if (raw==null) {
            return res;
        }
    
        for (final Entry<String, String[]> entry : raw.entrySet()) {
            final String key = entry.getKey();
            final String[] rawVals = entry.getValue();
            final String[] snzVals = new String[rawVals.length];
            for (int i=0; i < rawVals.length; i++) {
                snzVals[i] = WebSecurityUtils.sanitizeString(rawVals[i]);
            }
            res.put(key, snzVals);
        }           
        return res;
    }


    private void snzLogger() {
        for (final Entry<String,String[]> entry : original_parameters.entrySet()) {
            final String key = entry.getKey();
            final String[] rawVals = entry.getValue();
            final String[] snzVals = sanitized_parameters.get(key);
            if (rawVals !=null && rawVals.length>0)
            {
                for (int i=0; i < rawVals.length; i++) 
                {
                    if (rawVals[i].equals(snzVals[i]))                                                          
                        LOG.debug("Sanitization. Param seems safe: {}[{}]={}", key, i, snzVals[i]);
                    else
                        LOG.debug("Sanitization. Param modified: {}[{}]={}", key, i, snzVals[i]);
                }       
            }
        }
    }

}
