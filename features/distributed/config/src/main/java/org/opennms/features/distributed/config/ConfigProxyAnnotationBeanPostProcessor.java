/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.distributed.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;

import org.opennms.core.xml.ConfigProxy;
import org.opennms.core.xml.JaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.distributed.core.api.RestClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * This bean processor wraps all @{@link ConfigProxy} annotated classes so that if running in a distributed container,
 * such as minion or sentinel all calls to {@link JaxbConfigDao#getConfig()} are performed via an HTTP request to
 * the OpenNMS ResT API allowing those containers to consume configuration files.
 *
 * At the moment only classes which implement {@link JaxbConfigDao} work, and will be proxied.
 *
 * Please note, that the proxied bean, must implement an interface defining the DAO methods, otherwise proxiing will not work.
 */
@Component
public class ConfigProxyAnnotationBeanPostProcessor implements BeanPostProcessor {

    // Easy way of determining if running within OPENNMS
    private boolean isRunningInOpennmsJvm = System.getProperty("opennms.home") != null;

    @Autowired
    private RestClient restClient;

    @Override
    public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
        if (isRunningInOpennmsJvm) {
            return bean;
        }
        final ConfigProxy annotation = bean.getClass().getAnnotation(ConfigProxy.class);
        if (annotation != null) {
            if (!(bean instanceof JaxbConfigDao)) {
                throw new IllegalStateException("Bean with name '" + beanName
                        + "' has @" + ConfigProxy.class.getSimpleName()
                        + " annotation, but does not implement " + JaxbConfigDao.class);
            }
            if (annotation.proxyMethod() == null || annotation.proxyMethod().isEmpty()) {
                throw new IllegalStateException("Bean with name '" + beanName
                        + "' has @" + ConfigProxy.class.getSimpleName()
                        + " annotation, but does not define proxied methods");
            }

            // Prevent loading of configuration from disk, as in this case we want to load it via the OpenNMS ReST API
            ((JaxbConfigDao) bean).setInitializeContainerOnInit(false);

            // Proxy call
            return Proxy.newProxyInstance(getClass().getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                // The call to loading the configuration is proxied, everything else is kept as is
                if (method.getName().equals(annotation.proxyMethod())) {
                    final Class configType = ((JaxbConfigDao) bean).getConfigType();
                    final String configuration = restClient.getConfiguration(configType.getSimpleName());
                    if (configuration == null) {
                        throw new RuntimeException("Could not retrieve configuration of type " + configType);
                    }
                    try (InputStream inputStream = new ByteArrayInputStream(configuration.getBytes())) {
                        return JaxbUtils.unmarshal(configType, inputStream);
                    }
                }
                return method.invoke(bean, args);
            });

        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }
}
