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

package org.opennms.spring.xmlrpc;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.MethodInvoker;

/**
 * <p>XmlRpcServiceExporter class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class XmlRpcServiceExporter extends RemoteExporter implements InitializingBean, DisposableBean, XmlRpcHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(XmlRpcServiceExporter.class);
    
    private WebServer webServer;
    private Object proxy;
    private String serviceName;

    /**
     * <p>Getter for the field <code>webServer</code>.</p>
     *
     * @return a {@link org.apache.xmlrpc.WebServer} object.
     */
    public WebServer getWebServer() {
        return this.webServer;
    }
    
    /**
     * <p>Setter for the field <code>webServer</code>.</p>
     *
     * @param webServer a {@link org.apache.xmlrpc.WebServer} object.
     */
    public void setWebServer(WebServer webServer) {
        this.webServer = webServer;
    }
    
    /**
     * <p>Getter for the field <code>serviceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return this.serviceName;
    }
    
    /**
     * <p>Setter for the field <code>serviceName</code>.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
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

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() throws Exception {
        if (serviceName == null || "".equals(serviceName)) {
            this.webServer.removeHandler("$default");
        } else {
            this.webServer.removeHandler(serviceName);
        }
    }
    
    public static class MsgPreservingXmlRpcException extends XmlRpcException {

        /**
         * 
         */
        private static final long serialVersionUID = -4693127622262382452L;

        public MsgPreservingXmlRpcException(int code, String message) {
            super(code, message);
        }
        
        @Override
        public String toString() {
            return getMessage();
        }
        
    }

    /** {@inheritDoc} */
    @Override
    public Object execute(String method, @SuppressWarnings("unchecked") Vector params) throws Exception {
        
        LOG.debug("calling: {}({})", method, toArgList(params));
        
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
        
        else if (returnValue instanceof Map<?,?> && !(returnValue instanceof Hashtable<?,?>)) {
            returnValue = new Hashtable<Object, Object>((Map<?, ?>)returnValue);
        }
        
        else if (returnValue instanceof Collection<?> && !(returnValue instanceof Vector<?>)) {
            returnValue = new Vector<Object>((Collection<?>)returnValue);
        }
        
        LOG.debug("returning from: {}({}) result = {}", method, toArgList(params), returnValue);
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

    private String toArgList(@SuppressWarnings("unchecked") Vector params) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < params.size(); i++) {
            if (i != 0) sb.append(", ");
            sb.append(params.get(i));
        }
        return sb.toString();
    }

    private String getMethodName(String method) {
        if (this.serviceName == null || "".equals(serviceName))
            return method;
        else
            return method.substring(serviceName.length());
    }


    
    
}
