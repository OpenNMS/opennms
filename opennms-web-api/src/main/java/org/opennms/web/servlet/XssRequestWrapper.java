/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        for (final Entry<String, String[]> entry : original_parameters.entrySet()) {
            final String key = entry.getKey();
            final String[] rawVals = entry.getValue();
            final String[] snzVals = sanitized_parameters.get(key);
            if (rawVals != null && rawVals.length > 0) {
                for (int i = 0; i < rawVals.length; i++) {
                    final String value = key.toLowerCase().contains("pass") ? "<output omitted>" : snzVals[i];

                    if (rawVals[i].equals(snzVals[i])) {
                        LOG.debug("Sanitization. Param seems safe: {}[{}]={}", key, i, value);
                    } else {
                        LOG.debug("Sanitization. Param modified: {}[{}]={}", key, i, value);
                    }
                }
            }
        }
    }
}
