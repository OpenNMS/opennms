/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.api;

import org.opennms.netmgt.telemetry.config.api.TelemetryBeanDefinition;

/**
 * The {@link TelemetryBeanFactory} is used to create a {@link TelemetryBeanDefinition}.
 *
 * @param <T> the type of the bean which is created by this factory
 * @param <B> The type of the bean definition which defines the bean to create.
 *
 * @author mvrueden
 */
public interface TelemetryBeanFactory<T, B extends TelemetryBeanDefinition> {
    Class<? extends T> getBeanClass();
    T createBean(B beanDefinition);
}
