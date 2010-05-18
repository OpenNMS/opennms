/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 13, 2005
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.spring.xmlrpc;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.MethodInvoker;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class XmlRpcServiceExporter extends RemoteExporter implements InitializingBean, DisposableBean, XmlRpcHandler {
    
    private WebServer webServer;
    private Object proxy;
    private String serviceName;

    public WebServer getWebServer() {
        return this.webServer;
    }
    
    public void setWebServer(WebServer webServer) {
        this.webServer = webServer;
    }
    
    public String getServiceName() {
        return this.serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public void afterPropertiesSet() throws Exception {
        if (this.webServer == null)
            throw new IllegalArgumentException("webServer is required");
        checkService();
        checkServiceInterface();
        this.proxy = getProxyForService();
     
        if (serviceName == null || "".equals(serviceName)) {
            this.webServer.addHandler("$default", this);
        } else {
            this.webServer.addHandler(serviceName, this);
        }
        
    }

    public void destroy() throws Exception {
        if (serviceName == null || "".equals(serviceName)) {
            this.webServer.removeHandler("$default");
        } else {
            this.webServer.removeHandler(serviceName);
        }
    }
    
    public static class MsgPreservingXmlRpcException extends XmlRpcException {

        public MsgPreservingXmlRpcException(int code, String message) {
            super(code, message);
        }
        
        public String toString() {
            return getMessage();
        }
        
    }

    public Object execute(String method, Vector params) throws Exception {
        
        log().debug("calling: "+method+'('+toArgList(params)+')');
        
        MethodInvoker invoker = new ArgumentConvertingMethodInvoker();
        invoker.setTargetObject(this.proxy);
        invoker.setTargetMethod(getMethodName(method));
        invoker.setArguments(params.toArray());
        invoker.prepare();
        
        try {
        Object returnValue =  invoker.invoke();
        
        if (returnValue == null && invoker.getPreparedMethod().getReturnType() == Void.TYPE) {
            returnValue = "void";
        }
        
        else if (returnValue instanceof Map && !(returnValue instanceof Hashtable)) {
            returnValue = new Hashtable((Map)returnValue);
        }
        
        else if (returnValue instanceof Collection && !(returnValue instanceof Vector)) {
            returnValue = new Vector((Collection)returnValue);
        }
        
        log().debug("returning from: "+method+'('+toArgList(params)+") result = "+returnValue);
        return returnValue;
        
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof IllegalArgumentException) {
                throw new MsgPreservingXmlRpcException(XmlRpcConstants.FAULT_INVALID_DATA, targetException.getMessage());
            } else if (targetException instanceof MalformedURLException) {
                throw new MsgPreservingXmlRpcException(XmlRpcConstants.FAULT_INVALID_URL, targetException.getMessage());
            }
            else if (targetException instanceof Exception && targetException.toString() != null) { 
                throw (Exception)targetException;
            }
            
            String msg = targetException.toString();
            if (msg == null)
                msg = targetException.getClass().getName();
            
            Exception ex = new Exception(msg, targetException);
            ex.setStackTrace(targetException.getStackTrace());
            throw ex;
        }

    }

    private String toArgList(Vector params) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < params.size(); i++) {
            if (i != 0) sb.append(", ");
            sb.append(params.get(i));
        }
        return sb.toString();
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass()); 
    }

    private String getMethodName(String method) {
        if (this.serviceName == null || "".equals(serviceName))
            return method;
        else
            return method.substring(serviceName.length());
    }


    
    
}
