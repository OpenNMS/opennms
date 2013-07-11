/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Activity;
import org.opennms.netmgt.provision.service.lifecycle.annotations.Attribute;

/**
 * <p>Phase class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Phase extends BatchTask {
    private static final Logger LOG = LoggerFactory.getLogger(Phase.class);
    private LifeCycleInstance m_lifecycle;
    private String m_name;
    private Object[] m_providers;
        
    /**
     * <p>Constructor for Phase.</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param lifecycle a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance} object.
     * @param name a {@link java.lang.String} object.
     * @param providers an array of {@link java.lang.Object} objects.
     */
    public Phase(ContainerTask<?> parent, LifeCycleInstance lifecycle, String name, Object[] providers) {
        super(lifecycle.getCoordinator(), parent);
        m_lifecycle = lifecycle;
        m_name = name;
        m_providers = providers;

        addPhaseMethods();
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>getLifeCycleInstance</p>
     *
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance} object.
     */
    public LifeCycleInstance getLifeCycleInstance() {
        return m_lifecycle;
    }
    
    /**
     * <p>createNestedLifeCycle</p>
     *
     * @param lifeCycleName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance} object.
     */
    public LifeCycleInstance createNestedLifeCycle(String lifeCycleName) {
        return m_lifecycle.createNestedLifeCycle(this, lifeCycleName);
    }
    
    /**
     * <p>addPhaseMethods</p>
     */
    public void addPhaseMethods() {
        for(Object provider : m_providers) {
            addPhaseMethods(provider);
        }
    }
    
    /**
     * <p>addPhaseMethods</p>
     *
     * @param provider a {@link java.lang.Object} object.
     */
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
                @Override
                public void run() {
                    try {
                        doInvoke(m_phase.getLifeCycleInstance());
                    } catch (final Exception e) {
                        LOG.info("failed to invoke lifecycle instance", e);
                    }
                }
                @Override
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
                    } else if (type instanceof ParameterizedType) {
                        ParameterizedType paramType = (ParameterizedType)type;
                        args[i] = lifeCycle.findAttributeByType((Class<?>) paramType.getRawType());
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

        @Override
        public String toString() {
            return String.format("%s.%s(%s)", m_target.getClass().getSimpleName(), m_method.getName(), m_phase.getLifeCycleInstance());
        }
        
    }
    
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return String.format("Phase %s of lifecycle %s", getName(), m_lifecycle.getName());
    }
}
