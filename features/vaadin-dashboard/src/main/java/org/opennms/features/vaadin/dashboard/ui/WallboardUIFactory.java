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
package org.opennms.features.vaadin.dashboard.ui;

import org.opennms.osgi.OnmsVaadinUIFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * A factory class responsible for constructing the wallboard application.
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class WallboardUIFactory extends OnmsVaadinUIFactory {
    /**
     * Constructor for instantiating a new factory.
     *
     * @param container the bean container to use
     * @param beanName  the beam name to use
     */
    public WallboardUIFactory(BlueprintContainer container, String beanName) {
        super(WallboardUI.class, container, beanName);
    }

}
