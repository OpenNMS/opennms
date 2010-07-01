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
/**
 * <p>FormPropertiesReader class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class FormPropertiesReader implements MessageBodyReader<MultivaluedMapImpl> {	
    @Context private HttpServletRequest m_httpServletRequest;
    
    Map<String, List<String>> params=new HashMap<String,List<String>>();
	
    /** {@inheritDoc} */
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaTypes) {
        return type.isAssignableFrom(MultivaluedMapImpl.class);
    }

	
    /** {@inheritDoc} */
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
