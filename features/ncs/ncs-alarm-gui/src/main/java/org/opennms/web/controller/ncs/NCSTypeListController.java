/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.ncs;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("ncs/ncs-type.htm")
public class NCSTypeListController {
    
    @Autowired
    NCSComponentRepository m_componentDao;
    
    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String type = request.getParameter("type");
        String foreignSource = request.getParameter("foreignSource");
        String foreignId = request.getParameter("foreignId");
        
        NCSComponent component = null;
        String treeView = "<br/><p>No Components To View, Please check the type, foreign source and foreign id are correct</p>";
        if(!type.equals("null") && !foreignSource.equals("null") && !foreignId.equals("null")) {
            component = m_componentDao.findByTypeAndForeignIdentity(type, foreignSource, foreignId);
            treeView = "<ul class=\"TreeView\" id=\"TreeView\">\n" + getComponentHTML(component) + "</ul>";
        }
        
        
        ModelAndView modelAndView = new ModelAndView("ncs/ncs-type");
        modelAndView.addObject("treeView", treeView);
        return modelAndView;
    }
    
    private String getComponentHTML(NCSComponent component) {
        final StringBuilder buffer = new StringBuilder();
        
        if(component != null) {
            Set<NCSComponent> subcomponents = component.getSubcomponents();
            if(subcomponents.size() > 0) {
                buffer.append("<li class=\"Expanded\">");
                buffer.append(component.getName());
                
                buffer.append("<ul>\n");
                for(NCSComponent c : subcomponents) {
                    buffer.append(getComponentHTML(c));
                }
                buffer.append("</ul>");
            }else {
                buffer.append("<li>");
                buffer.append(component.getName());
            }
            
            buffer.append("</li>\n");
        }
        return buffer.toString();
        
    }
}
