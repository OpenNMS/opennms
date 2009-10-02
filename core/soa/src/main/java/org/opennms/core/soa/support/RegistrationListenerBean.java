/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.core.soa.support;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * RegistrationListenerBean
 *
 * @author brozow
 */
public class RegistrationListenerBean implements RegistrationListener<Object>, InitializingBean {

    private Class<?> m_serviceInterface;
    private Object m_target;
    private String m_bindMethod;
    private String m_unbindMethod;
    


    /**
     * @return the serviceInterface
     */
    public Class<?> getServiceInterface() {
        return m_serviceInterface;
    }

    /**
     * @param serviceInterface the serviceInterface to set
     */
    public void setServiceInterface(Class<?> serviceInterface) {
        m_serviceInterface = serviceInterface;
    }

    /**
     * @return the target
     */
    public Object getTarget() {
        return m_target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(Object target) {
        m_target = target;
    }

    /**
     * @return the bindMethod
     */
    public String getBindMethod() {
        return m_bindMethod;
    }

    /**
     * @param bindMethod the bindMethod to set
     */
    public void setBindMethod(String bindMethod) {
        m_bindMethod = bindMethod;
    }

    /**
     * @return the unbindMethod
     */
    public String getUnbindMethod() {
        return m_unbindMethod;
    }

    /**
     * @param unbindMethod the unbindMethod to set
     */
    public void setUnbindMethod(String unbindMethod) {
        m_unbindMethod = unbindMethod;
    }
    
    
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_serviceInterface, "serviceInterface must not be null");
        Assert.notNull(m_target, "target may not be null");
        Assert.isTrue(StringUtils.hasText(m_bindMethod) || StringUtils.hasText(m_unbindMethod), "at least one of the bind or unbind methods must be set");

        if (StringUtils.hasText(m_bindMethod)) {
            // verify this method exists
            getMethod(m_bindMethod);
        }
        
        if (StringUtils.hasText(m_unbindMethod)) {
            // verify this method exists
            getMethod(m_unbindMethod);
        }
        
    }
    
    private Method getMethod(String name) throws SecurityException, NoSuchMethodException {
        Method method = m_target.getClass().getMethod(name, m_serviceInterface, Map.class);
        Assert.notNull(name, "Unable to find method named " + name);
        return method;
    }

    private void invokeMethod(String methodName, Registration registration) {
        try {
          Method method = getMethod(methodName);
          method.invoke(m_target, registration.getProvider(m_serviceInterface), registration.getProperties());
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e, "Unexexpected exception invoking method " + methodName);
        } finally {
            
        }
    }

    public void providerRegistered(Registration registration, Object provider) {
        if (StringUtils.hasText(m_bindMethod)) {
            invokeMethod(m_bindMethod, registration);
        }
    }
    
    public void providerUnregistered(Registration registration, Object provider) {
        if (StringUtils.hasText(m_unbindMethod)) {
            invokeMethod(m_unbindMethod, registration);
        }
    }

}
