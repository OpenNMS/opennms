package org.opennms.web;

// from http://mc4j.org/confluence/display/stripes/XSS+filter

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class XssRequestWrapper extends HttpServletRequestWrapper
{

    private Map<String, String[]> sanitized;
    private Map<String, String[]> orig;
    
    @SuppressWarnings("unchecked")
    public XssRequestWrapper(HttpServletRequest req) 
    {
        super(req);
        orig = req.getParameterMap();   
        sanitized = getParameterMap();
        if (log().isDebugEnabled())
            snzLogger();
    }       

    @Override
    public String getParameter(String name) 
    {       
        String[] vals = getParameterMap().get(name); 
        if (vals != null && vals.length > 0) 
            return vals[0];
        else        
            return null;        
    }

    @Override
    public Map<String, String[]> getParameterMap() 
    {   
        if (sanitized==null)
            sanitized = sanitizeParamMap(orig);
        return sanitized;           

    }

    @Override
    public String[] getParameterValues(String name)
    {   
        return getParameterMap().get(name);
    }


    private  Map<String, String[]> sanitizeParamMap(Map<String, String[]> raw) 
    {       
        Map<String, String[]> res = new HashMap<String, String[]>();
        if (raw==null)
            return res;
    
        for (String key : (Set<String>) raw.keySet())
        {           
            String[] rawVals = raw.get(key);
            String[] snzVals = new String[rawVals.length];
            for (int i=0; i < rawVals.length; i++) 
            {
                snzVals[i] = SafeHtmlUtil.sanitize(rawVals[i]);
            }
            res.put(key, snzVals);
        }           
        return res;
    }


    private void snzLogger()
    {
        for (String key : (Set<String>) orig.keySet())
        {
            String[] rawVals = orig.get(key);
            String[] snzVals = sanitized.get(key);
            if (rawVals !=null && rawVals.length>0)
            {
                for (int i=0; i < rawVals.length; i++) 
                {
                    if (rawVals[i].equals(snzVals[i]))                                                          
                        log().debug("Sanitization. Param seems safe: " + key + "[" + i + "]=" + snzVals[i]);               
                    else
                        log().debug("Sanitization. Param modified: " + key + "[" + i + "]=" + snzVals[i]);
                }       
            }
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}