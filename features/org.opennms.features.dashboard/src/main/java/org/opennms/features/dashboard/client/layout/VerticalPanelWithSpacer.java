/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.dashboard.client.layout;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link VerticalPanel} which has a permanent spacer at the end to prevent CSS collapse of the
 * panel and its parent.
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public class VerticalPanelWithSpacer extends VerticalPanel {
  private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_SPACER = "demo-gridview-spacer";

  public VerticalPanelWithSpacer() {
    Label spacerLabel = new Label("");
    spacerLabel.setStylePrimaryName(CSS_DEMO_INSERT_PANEL_EXAMPLE_SPACER);
    super.add(spacerLabel);
  }

  @Override
  public void add(Widget w) {
    super.insert(w, getWidgetCount() - 1);
  }

  @Override
  public void insert(Widget w, int beforeIndex) {
    if (beforeIndex == getWidgetCount()) {
      beforeIndex--;
    }
    super.insert(w, beforeIndex);
  }

}