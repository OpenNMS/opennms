//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.svclayer;

import java.awt.Color;

public class AggregateStatusColor extends Color {

    private static final long serialVersionUID = -2058613386988070833L;
    
    private static final Color RED = new Color(255, 0, 0);
    private static final Color YELLOW = new Color(255, 255, 0);
    private static final Color GREEN = new Color(0, 255, 0);
    
    public static final Color NODES_ARE_DOWN = RED;
    public static final Color ONE_SERVICE_DOWN = YELLOW;
    public static final Color ALL_NODES_UP = GREEN;
    
    /**
     * Creates a Color with an opaque sRGB with red, green and blue values in 
     * range 0-255.
     *
     * @param r  the red component in range 0x00-0xFF.
     * @param g  the green component in range 0x00-0xFF.
     * @param b  the blue component in range 0x00-0xFF.
     */
    public AggregateStatusColor(int r, int g, int b) {
        super(r, g, b);
        // TODO Auto-generated constructor stub
    }


}
