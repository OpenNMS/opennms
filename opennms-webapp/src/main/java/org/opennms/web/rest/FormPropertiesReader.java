package org.opennms.web.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLDecoder;
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
            result.put(URLDecoder.decode(parmName, "UTF-8"), URLDecoder.decode(parmValue[0],"UTF-8"));

		}

//		BufferedReader in = new BufferedReader(new InputStreamReader(
//				entityStream));

//		StringBuffer buffer = new StringBuffer();
//		String line = null;
//		while ((line = in.readLine()) != null) {
//			buffer.append(line);
//		}
//		
//		String postBody = buffer.toString();
//		System.out.println("postBody: " + postBody);
//		for (String item : postBody.split("&")) {
//      String[] kv = item.split("=");
//      result.add(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1],"UTF-8"));
//  }
		
		

		return result;
	}

}
