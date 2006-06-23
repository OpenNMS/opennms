//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.eventconf.masks;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.web.eventconf.bobject.Event;
import org.opennms.web.eventconf.bobject.MaskElement;

/**
 * A servlet that handles saving Mask Elements to an Event's mask
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SaveMaskElementsServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession user = request.getSession(false);

        if (user != null) {
            Event event = (Event) user.getAttribute("event.modify.jsp");

            List maskElements = event.getMask();

            String[] names = request.getParameterValues("mask");

            maskElements.clear();
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    MaskElement newElement = new MaskElement();

                    newElement.setElementName(names[i]);

                    String values = request.getParameter("mask" + i + "Values");

                    if (values != null && !values.trim().equals("")) {
                        parseValues(values, newElement);
                        maskElements.add(newElement);
                    }
                }
            }
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward(request, response);
    }

    /**
     */
    private void parseValues(String valuesBuffer, MaskElement element) {
        StringTokenizer tokenizer = new StringTokenizer(valuesBuffer, "\n");

        while (tokenizer.hasMoreTokens()) {
            String value = tokenizer.nextToken();
            element.addElementValue(value);
        }
    }
}
