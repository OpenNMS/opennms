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

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

/**
 * This class implements a {@link Dashlet} for displaying an image.
 */
public class ImageDashlet extends VerticalLayout implements Dashlet {
    /**
     * the dashlet's name
     */
    private String m_name;
    /**
     * the image URL
     */
    private String m_imageUrl = null;
    /**
     * The {@link DashletSpec} for this instance
     */
    private DashletSpec m_dashletSpec;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public ImageDashlet(String name, DashletSpec dashletSpec) {
        /**
         * Setting the member fields
         */
        m_name = name;
        m_dashletSpec = dashletSpec;

        /**
         * Setting up the layout
         */
        setCaption(getName());
        setSizeFull();

        update();
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isBoosted() {
        return false;
    }

    /**
     * Updates the dashlet contents and computes new boosted state
     */
    @Override
    public void update() {
        String newImage = m_dashletSpec.getParameters().get("imageUrl");

        if (!newImage.equals(m_imageUrl)) {
            m_imageUrl = newImage;
            removeAllComponents();
            Image image = new Image(null, new ExternalResource(m_imageUrl));
            image.setHeight(100,Unit.PERCENTAGE);
            addComponent(image);
            setComponentAlignment(image, Alignment.MIDDLE_CENTER);
        }
    }
}
