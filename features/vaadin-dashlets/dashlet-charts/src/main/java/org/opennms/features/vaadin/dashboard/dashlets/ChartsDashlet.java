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

package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.model.*;

/**
 * This class implements a {@link Dashlet} for displaying charts.
 */
public class ChartsDashlet extends AbstractDashlet {
    /**
     * the image URL
     */
    private String m_imageUrl = null;
    /**
     * wallboard view
     */
    DashletComponent m_dashletComponent = null;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public ChartsDashlet(String name, DashletSpec dashletSpec) {
        super(name, dashletSpec);
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_dashletComponent == null) {
            m_dashletComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setSizeFull();
                }

                @Override
                public void refresh() {
                    String newImage = "/opennms/charts?chart-name=" + getDashletSpec().getParameters().get("chart");

                    String maximizeHeightString = getDashletSpec().getParameters().get("maximizeHeight");
                    String maximizeWidthString = getDashletSpec().getParameters().get("maximizeWidth");

                    boolean maximizeHeight = ("true".equals(maximizeHeightString) || "yes".equals(maximizeHeightString) || "1".equals(maximizeHeightString));
                    boolean maximizeWidth = ("true".equals(maximizeWidthString) || "yes".equals(maximizeWidthString) || "1".equals(maximizeWidthString));

                    if (!newImage.equals(m_imageUrl)) {
                        m_imageUrl = newImage;
                        m_verticalLayout.removeAllComponents();
                        Image image = new Image(null, new ExternalResource(m_imageUrl));
                        if (maximizeHeight && maximizeWidth) {
                            image.setSizeFull();
                        } else {
                            if (maximizeHeight) {
                                image.setHeight(100, Sizeable.Unit.PERCENTAGE);
                            }
                            if (maximizeWidth) {
                                image.setWidth(100, Sizeable.Unit.PERCENTAGE);
                            }
                        }
                        m_verticalLayout.addComponent(image);
                        m_verticalLayout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
                    }
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }

        return m_dashletComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        return getWallboardComponent();
    }
}
