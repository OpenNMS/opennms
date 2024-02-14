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
package org.opennms.features.vaadin.dashboard.dashlets;

import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * This class implements a {@link Dashlet} for displaying an image.
 */
public class ImageDashlet extends AbstractDashlet {
    /**
     * the image URL
     */
    private String m_imageUrl = null;

    /**
     * the wallboard view
     */
    private DashletComponent m_dashletComponent;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public ImageDashlet(String name, DashletSpec dashletSpec) {
        super(name, dashletSpec);
    }

    @Override
    public DashletComponent getWallboardComponent(final UI ui) {
        if (m_dashletComponent == null) {
            m_dashletComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setSizeFull();
                }

                @Override
                public void refresh() {
                    String newImage = getDashletSpec().getParameters().get("imageUrl");

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
    public DashletComponent getDashboardComponent(final UI ui) {
        return getWallboardComponent(ui);
    }
}
