/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.provision.service.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;

class Phase {
    private LifeCycle m_lifecycle;
    private String m_name;
    private Object[] m_providers;
        
    public Phase(LifeCycle lifecycle, String name, Object[] providers) {
        m_lifecycle = lifecycle;
        m_name = name;
        m_providers = providers;

    }

    public String getName() {
        return m_name;
    }
    
    public void run() {
        for(Object provider : m_providers) {
            PhaseMethod[] methods = findPhaseMethods(provider);
            for(PhaseMethod method : methods) {
                method.invoke(m_lifecycle);
            }
            
        }
        
    }
    
    private PhaseMethod[] findPhaseMethods(Object provider) {
        List<PhaseMethod> methods = new ArrayList<PhaseMethod>();
        for(Method method : provider.getClass().getMethods()) {
            if (isPhaseMethod(method)) {
                methods.add(new PhaseMethod(provider, method));
            }
        }
        return methods.toArray(new PhaseMethod[methods.size()]);
    }

    private boolean isPhaseMethod(Method method) {
        Activity activity = method.getAnnotation(Activity.class);
        return (activity != null && activity.phase().equals(m_name) && activity.lifecycle().equals(m_lifecycle.getName()));
    }
    
    PhaseMethod createPhaseMethod(Object provider, Method method) {
        return new PhaseMethod(provider, method);
    }
    
    public static class PhaseMethod {
        private Object m_target;
        private Method m_method;
        
        public PhaseMethod(Object target, Method method) {
            m_target = target;
            m_method = method;
        }
        
        public void invoke(LifeCycle lifeCycle) {
            try {
                doInvoke(lifeCycle);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private void doInvoke(LifeCycle lifeCycle) throws IllegalAccessException, InvocationTargetException {
            m_method.invoke(m_target, lifeCycle);
        }
        
    }
}