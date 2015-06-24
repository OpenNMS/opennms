/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;


public class GWTBoundingBox extends JavaScriptObject {

    protected GWTBoundingBox() {}
    
    public static final native GWTBoundingBox create(int x, int y, int width, int height) /*-{
        return {"x":x, "y":y, "width":width, "height":height};
    }-*/;

    public final native int getX() /*-{
        return this.x;
    }-*/;

    public final native int getY() /*-{
        return this.y;
    }-*/;

    public final native int getWidth() /*-{
        return this.width;
    }-*/;

    public final native int getHeight() /*-{
        return this.height;
    }-*/;
    
}
