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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.Task;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Attribute;

public class Phase extends BatchTask {
    private LifeCycleInstance m_lifecycle;
    private String m_name;
    private Object[] m_providers;
        
    public Phase(ContainerTask parent, LifeCycleInstance lifecycle, String name, Object[] providers) {
        super(lifecycle.getCoordinator(), parent);
        m_lifecycle = lifecycle;
        m_name = name;
        m_providers = providers;

        addPhaseMethods();
    }

    public String getName() {
        return m_name;
    }
    
    public LifeCycleInstance getLifeCycleInstance() {
        return m_lifecycle;
    }
    
    public LifeCycleInstance createNestedLifeCycle(String lifeCycleName) {
        return m_lifecycle.createNestedLifeCycle(this, lifeCycleName);
    }
    
    public void addPhaseMethods() {
        for(Object provider : m_providers) {
            addPhaseMethods(provider);
        }
    }
    
    public void addPhaseMethods(Object provider) {
        for(Method method : provider.getClass().getMethods()) {
            String schedulingHint = isPhaseMethod(method);
            if (schedulingHint != null) {
                //this.setPreferredExecutor(schedulingHint);
                add(createPhaseMethod(provider, method, schedulingHint));
            }
        }
    }
    
//    public void run() {
//        for(Object provider : m_providers) {
//            PhaseMethod[] methods = findPhaseMethods(provider);
//            for(PhaseMethod method : methods) {
//                method.invoke();
//            }
//            
//        }
//        
//    }
//    
//    private PhaseMethod[] findPhaseMethods(Object provider) {
//        List<PhaseMethod> methods = new ArrayList<PhaseMethod>();
//        for(Method method : provider.getClass().getMethods()) {
//            if (isPhaseMethod(method)) {
//                methods.add(new PhaseMethod(this, provider, method));
//            }
//        }
//        return methods.toArray(new PhaseMethod[methods.size()]);
//    }

    private String isPhaseMethod(Method method) {
        Activity activity = method.getAnnotation(Activity.class);
        if (activity != null && activity.phase().equals(m_name) && activity.lifecycle().equals(m_lifecycle.getName())) {
            return activity.schedulingHint();
        }
        return null;
    }
    
    PhaseMethod createPhaseMethod(Object provider, Method method, String schedulingHint) {
        return new PhaseMethod(this, provider, method, schedulingHint);
    }
    
    public static class PhaseMethod extends BatchTask {
        private Phase m_phase;
        private Object m_target;
        private Method m_method;
        
        public PhaseMethod(Phase phase, Object target, Method method, String schedulingHint) {
            super(phase.getCoordinator(), phase);
            m_phase = phase;
            m_target = target;
            m_method = method;
            add(phaseRunner(), schedulingHint);
        }
        
        private Runnable phaseRunner() {
            return new Runnable() {
                public void run() {
                    try {
                        doInvoke(m_phase.getLifeCycleInstance());
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
                public String toString() {
                    return "Runner for "+m_phase.toString();
                }
            };
        }

        private void doInvoke(LifeCycleInstance lifeCycle) throws IllegalAccessException, InvocationTargetException {
            
            lifeCycle.setAttribute("currentPhase", m_phase);
            
            Object[] args = findArguments(lifeCycle);
            
            Object retVal = m_method.invoke(m_target, args);
            Attribute retValAttr = m_method.getAnnotation(Attribute.class);
            if (retValAttr != null) {
                lifeCycle.setAttribute(retValAttr.value(), retVal);
            }
            else if (retVal instanceof Task) {
                add((Task)retVal);
            } else if (retVal != null) {
                lifeCycle.setAttribute(retVal.getClass().getName(), retVal);
            }
        }

        private Object[] findArguments(LifeCycleInstance lifeCycle) {
            
            Type[] types = m_method.getGenericParameterTypes();

            Object[] args = new Object[types.length];
            for(int i = 0; i < types.length; i++) {
                Attribute annot = getParameterAnnotation(m_method, i, Attribute.class);
                if (annot != null) {
                    args[i] = lifeCycle.getAttribute(annot.value());
                } else {
                    Type type = types[i];
                    if (type instanceof Class<?>) {
                        Class<?> clazz = (Class<?>)type;
                        args[i] = lifeCycle.findAttributeByType(clazz);
                    } else {
                        args[i] = null;
                    }
                }
                
            }

            return args;
        }


        private <T extends Annotation> T getParameterAnnotation(Method method, int parmIndex, Class<T> annotationClass) {
            Annotation[] annotations = method.getParameterAnnotations()[parmIndex];
            
            for(Annotation a : annotations) {
                if (annotationClass.isInstance(a)) {
                    return annotationClass.cast(a);
                }
            }
            
            return null;
        }

        public String toString() {
            return String.format("%s.%s(%s)", m_target.getClass().getSimpleName(), m_method.getName(), m_phase.getLifeCycleInstance());
        }
        
    }
    
    
    public String toString() {
        return String.format("Phase %s of lifecycle %s", getName(), m_lifecycle.getName());
    }
}