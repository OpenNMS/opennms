/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Provider
public class FormPropertiesReader implements MessageBodyReader<MultivaluedMapImpl> {	
    @Context private HttpServletRequest m_httpServletRequest;
    
    Map<String, List<String>> params=new HashMap<String,List<String>>();
	
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaTypes) {
        return type.isAssignableFrom(MultivaluedMapImpl.class);
    }

	
    public MultivaluedMapImpl readFrom(java.lang.Class<MultivaluedMapImpl> type,
			java.lang.reflect.Type genericType,
			java.lang.annotation.Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<java.lang.String, java.lang.String> httpHeaders,
			java.io.InputStream entityStream) throws IOException,
			WebApplicationException {
	    
		MultivaluedMapImpl result = new MultivaluedMapImpl();
		
		Enumeration<String> en = m_httpServletRequest.getParameterNames();
		while(en.hasMoreElements()) {
		    String parmName = en.nextElement();
		    String[] parmValue = m_httpServletRequest.getParameterValues(parmName);
            result.put(parmName, parmValue);
		}
		
		return result;
	}

}
