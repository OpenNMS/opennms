/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.config.ui;

import org.opennms.osgi.OnmsVaadinUIFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * A factory class responsible for constructing the configuration application.
 *
 * @author Christian Pape
 */
public class WallboardConfigUIFactory extends OnmsVaadinUIFactory {

    /**
     * Constructor for instantiating a new instance.
     *
     * @param container the {@link BlueprintContainer} to be set
     * @param beanName  the bean name to use
     */
    public WallboardConfigUIFactory(BlueprintContainer container, String beanName) {
        super(WallboardConfigUI.class, container, beanName);
    }
}
