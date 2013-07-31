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
package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

/**
 * This class represents a "undefined" {@link Dashlet} used for error conditions when the
 * required {@link Dashlet} cannot be found.
 *
 * @author Christian Pape
 */
public class UndefinedDashlet extends VerticalLayout implements Dashlet {
    /**
     * the dashlet's name
     */
    private String m_name;

    /**
     * Constructor for instantiating this {@link Dashlet}
     *
     * @param dashletSpec the {@link DashletSpec} to use
     */
    public UndefinedDashlet(String name, DashletSpec dashletSpec) {
        /**
         * Setting the name
         */
        m_name = name;
        /**
         * Setting error message
         */
        Label label = new Label("The defined dashlet could not be found!");

        addComponent(label);
        setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        setCaption(getName());
    }

    /**
     * This method returns the name of the {@link Dashlet}
     *
     * @return the dashlet's name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Checks whether this {@link Dashlet} is boosted.
     *
     * @return true, if boosted, false otherwise
     */
    @Override
    public boolean isBoosted() {
        return false;
    }

    /**
     * Updates the dashlet contents and computes new boosted state
     */
    @Override
    public void update() {
        /**
         * do nothing
         */
    }
}
