/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
 * @version $Id: $
 */
public class RegistrationListenerBean<T> implements RegistrationListener<T>, InitializingBean {

    private Class<T> m_serviceInterface;
    private Object m_target;
    private String m_bindMethod;
    private String m_unbindMethod;
    


    /**
     * <p>getServiceInterface</p>
     *
     * @return the serviceInterface
     */
    public Class<?> getServiceInterface() {
        return m_serviceInterface;
    }

    /**
     * <p>setServiceInterface</p>
     *
     * @param serviceInterface the serviceInterface to set
     */
    public void setServiceInterface(Class<T> serviceInterface) {
        m_serviceInterface = serviceInterface;
    }

    /**
     * <p>getTarget</p>
     *
     * @return the target
     */
    public Object getTarget() {
        return m_target;
    }

    /**
     * <p>setTarget</p>
     *
     * @param target the target to set
     */
    public void setTarget(Object target) {
        m_target = target;
    }

    /**
     * <p>getBindMethod</p>
     *
     * @return the bindMethod
     */
    public String getBindMethod() {
        return m_bindMethod;
    }

    /**
     * <p>setBindMethod</p>
     *
     * @param bindMethod the bindMethod to set
     */
    public void setBindMethod(String bindMethod) {
        m_bindMethod = bindMethod;
    }

    /**
     * <p>getUnbindMethod</p>
     *
     * @return the unbindMethod
     */
    public String getUnbindMethod() {
        return m_unbindMethod;
    }

    /**
     * <p>setUnbindMethod</p>
     *
     * @param unbindMethod the unbindMethod to set
     */
    public void setUnbindMethod(String unbindMethod) {
        m_unbindMethod = unbindMethod;
    }
    
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
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
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e, "Unexexpected exception invoking method " + methodName);
        } finally {
            
        }
    }

    /** {@inheritDoc} */
    @Override
    public void providerRegistered(Registration registration, Object provider) {
        if (StringUtils.hasText(m_bindMethod)) {
            invokeMethod(m_bindMethod, registration);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void providerUnregistered(Registration registration, Object provider) {
        if (StringUtils.hasText(m_unbindMethod)) {
            invokeMethod(m_unbindMethod, registration);
        }
    }

}
