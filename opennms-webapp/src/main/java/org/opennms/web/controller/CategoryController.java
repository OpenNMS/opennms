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
// Modifications:
//
// 2007 Jul 24: Remove unused code. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.svclayer.AdminCategoryService;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.NodeEditModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class CategoryController extends AbstractController {

    private AdminCategoryService m_adminCategoryService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String removeCategoryIdString = request.getParameter("removeCategoryId");
        String newCategoryName = request.getParameter("newCategoryName");
        String categoryIdString = request.getParameter("categoryid");
        String editString = request.getParameter("edit");
        String nodeIdString = request.getParameter("node");
        
        if (removeCategoryIdString != null) {
            m_adminCategoryService.removeCategory(removeCategoryIdString);
            
            return new ModelAndView(new RedirectView("/admin/categories.htm", true));
        }
        
        if (newCategoryName != null) {
            OnmsCategory cat = m_adminCategoryService.getCategoryWithName(newCategoryName);
            if (cat == null) {
                m_adminCategoryService.addNewCategory(newCategoryName);
            }
            
            /*
             * We could be smart and take the user straight to the edit page
             * for this new category, which would be great, however it's
             * not so great if the site has a huge number of available
             * category and they need to edit category member nodes
             * from the node pages.  So, we don't do it.
             */
            return new ModelAndView(new RedirectView("/admin/categories.htm", true));
        }
        
        if (categoryIdString != null && editString != null) {
            String editAction = request.getParameter("action");
            if (editAction != null) {
                String[] toAdd = request.getParameterValues("toAdd");
                String[] toDelete = request.getParameterValues("toDelete");

                m_adminCategoryService.performEdit(categoryIdString,
                                                      editAction,
                                                      toAdd,
                                                      toDelete);

                ModelAndView modelAndView = 
                    new ModelAndView(new RedirectView("/admin/categories.htm", true));
                modelAndView.addObject("categoryid", categoryIdString);
                modelAndView.addObject("edit", null);
                return modelAndView;
            }

            EditModel model =
                m_adminCategoryService.findCategoryAndAllNodes(categoryIdString);

            return new ModelAndView("/admin/editCategory",
                                    "model",
                                    model);
        }
        
        if (categoryIdString != null) {
            return new ModelAndView("/admin/showCategory",
                                    "model",
                                    m_adminCategoryService.getCategory(categoryIdString));
        }
        
        if (nodeIdString != null && editString != null) {
            String editAction = request.getParameter("action");
            if (editAction != null) {
                String[] toAdd = request.getParameterValues("toAdd");
                String[] toDelete = request.getParameterValues("toDelete");

                m_adminCategoryService.performNodeEdit(nodeIdString,
                                                       editAction,
                                                       toAdd,
                                                       toDelete);

                ModelAndView modelAndView = 
                    new ModelAndView(new RedirectView("/admin/categories.htm", true));
                modelAndView.addObject("node", nodeIdString);
                modelAndView.addObject("edit", null);
                return modelAndView;
            }

            NodeEditModel model =
                m_adminCategoryService.findNodeCategories(nodeIdString);

            return new ModelAndView("/admin/editNodeCategories",
                                    "model",
                                    model);
        }


        List<OnmsCategory> sortedCategories
            = m_adminCategoryService.findAllCategories();
        
        return new ModelAndView("/admin/categories",
                                "categories",
                                sortedCategories);
    }

    public AdminCategoryService getAdminCategoryService() {
        return m_adminCategoryService;
    }

    public void setAdminCategoryService(
            AdminCategoryService adminCategoryService) {
        m_adminCategoryService = adminCategoryService;
    }

}
