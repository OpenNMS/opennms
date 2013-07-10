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

package org.opennms.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.surveillanceViews.Category;
import org.opennms.netmgt.config.surveillanceViews.ColumnDef;
import org.opennms.netmgt.config.surveillanceViews.Columns;
import org.opennms.netmgt.config.surveillanceViews.RowDef;
import org.opennms.netmgt.config.surveillanceViews.Rows;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.config.surveillanceViews.Views;
import org.opennms.netmgt.dao.api.SurveillanceViewConfigDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.svclayer.AdminCategoryService;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.NodeEditModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>CategoryController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CategoryController extends AbstractController {

    private AdminCategoryService m_adminCategoryService;
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String removeCategoryIdString = request.getParameter("removeCategoryId");
        String newCategoryName = request.getParameter("newCategoryName");
        String categoryIdString = request.getParameter("categoryid");
        String editString = request.getParameter("edit");
        String nodeIdString = request.getParameter("node");

        RedirectView redirect = new RedirectView("/admin/categories.htm", true);

        // delete a category
        if (removeCategoryIdString != null) {
            m_adminCategoryService.removeCategory(removeCategoryIdString);
            
            return new ModelAndView(redirect);
        }

        // add a category
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
            return new ModelAndView(redirect);
        }

        // high-level category edit (add or remove nodes from a category)
        if (categoryIdString != null && editString != null) {
            String editAction = request.getParameter("action");
            if (editAction != null) {
                String[] toAdd = request.getParameterValues("toAdd");
                String[] toDelete = request.getParameterValues("toDelete");

                m_adminCategoryService.performEdit(categoryIdString, editAction, toAdd, toDelete);

                ModelAndView modelAndView = new ModelAndView(redirect);
                modelAndView.addObject("categoryid", categoryIdString);
                modelAndView.addObject("edit", "edit");
                return modelAndView;
            }

            EditModel model = m_adminCategoryService.findCategoryAndAllNodes(categoryIdString);

            return new ModelAndView("/admin/editCategory", "model", model);
        }

        // if we don't have an edit string, we just show the category
        if (categoryIdString != null) {
            return new ModelAndView("/admin/showCategory", "model", m_adminCategoryService.getCategory(categoryIdString));
        }

        // if we have a nodeId and we're in edit mode, edit the categories for a specific node
        if (nodeIdString != null && editString != null) {
            String editAction = request.getParameter("action");
            
            // if we've specified an action, perform that action
            if (editAction != null) {
                String[] toAdd = request.getParameterValues("toAdd");
                String[] toDelete = request.getParameterValues("toDelete");

                m_adminCategoryService.performNodeEdit(nodeIdString, editAction, toAdd, toDelete);

                ModelAndView modelAndView = new ModelAndView(redirect);
                modelAndView.addObject("node", nodeIdString);
                modelAndView.addObject("edit", "edit");
                return modelAndView;
            }

            // otherwise, display the edit page for adding and removing categories from a node
            NodeEditModel model = m_adminCategoryService.findNodeCategories(nodeIdString);

            return new ModelAndView("/admin/editNodeCategories", "model", model);
        }

        // otherwise, just show the category editor
        List<OnmsCategory> sortedCategories = m_adminCategoryService.findAllCategories();
        List<String> surveillanceCategories = getAllSurveillanceViewCategories();

        ModelAndView modelAndView = new ModelAndView("/admin/categories");
        modelAndView.addObject("categories", sortedCategories);
        modelAndView.addObject("surveillanceCategories",surveillanceCategories);
        
        return modelAndView; //new ModelAndView("/admin/categories", "categories", sortedCategories);
    }

    private List<String> getAllSurveillanceViewCategories() {
        List<String> categoryNames = new ArrayList<String>();
        Views views = getSurveillanceViewConfigDao().getViews();
        
        for(View view : views.getViewCollection()) {
            Rows rows = view.getRows();
            for(RowDef row : rows.getRowDefCollection()) {
                List<Category> categoryCollection = row.getCategoryCollection();
                addCategoryNames(categoryNames, categoryCollection);
            }
            
            Columns columns = view.getColumns();
            for(ColumnDef column : columns.getColumnDefCollection()) {
                List<Category> categoryCollection = column.getCategoryCollection();
                addCategoryNames(categoryNames, categoryCollection);
            }
            
        }
        
        return categoryNames;
    }

    private void addCategoryNames(List<String> categoryNames, List<Category> categoryCollection) {
        for(Category category : categoryCollection) {
            if(!categoryNames.contains(category.getName())) {
                categoryNames.add(category.getName());
            }
        }
    }

    /**
     * <p>getAdminCategoryService</p>
     *
     * @return a {@link org.opennms.web.svclayer.AdminCategoryService} object.
     */
    public AdminCategoryService getAdminCategoryService() {
        return m_adminCategoryService;
    }

    /**
     * <p>setAdminCategoryService</p>
     *
     * @param adminCategoryService a {@link org.opennms.web.svclayer.AdminCategoryService} object.
     */
    public void setAdminCategoryService(AdminCategoryService adminCategoryService) {
        m_adminCategoryService = adminCategoryService;
    }

    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao surveillanceConfigDao) {
        m_surveillanceViewConfigDao = surveillanceConfigDao;
    }

    public SurveillanceViewConfigDao getSurveillanceViewConfigDao() {
        return m_surveillanceViewConfigDao;
    }

}
