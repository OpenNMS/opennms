//
//  $Id$
//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All
// rights reserved.
// OpenNMS(R) is a derivative work, containing both original code,
// included code and modified code that was published under the GNU
// General Public License. Copyrights for modified and included code
// are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 May 10: Eliminate the need for the ServletContext. - dj@opennms.org
// 2008 May 10: Use log4j for logging, not the servlet context. - dj@opennms.org
// 2007 Jul 24: Suppress warnings on unused code. - dj@opennms.org
// 2004 Oct 16: Created CategoryList class with most of guts of the code
//              from category-box.jsp.
// 2004 Oct 01: Added a color change when disconnected from OpenNMS.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Oct 24: Added a mouse over for last update times. Bug #517.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights
// reserved.
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

package org.opennms.web.category;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;

public class CategoryList {

    protected CategoryModel m_model;

    /**
     * Display rules from viewsdisplay.xml. If null, then just show all known
     * categories under the header "Category". (See the getSections method.)
     */
    protected Section[] m_sections;

    private int m_disconnectTimeout;

    public CategoryList() throws ServletException {
        try {
            m_model = CategoryModel.getInstance();
        } catch (Exception e) {
            log().error("failed to instantiate the category model: " + e, e);
            throw new ServletException("failed to instantiate the category model: " + e, e);
        }

        try {
            ViewsDisplayFactory.init();
            ViewsDisplayFactory viewsDisplayFactory = ViewsDisplayFactory.getInstance();

            View view = viewsDisplayFactory.getDefaultView();

            if (view != null) {
                m_sections = view.getSection();
                m_disconnectTimeout  = viewsDisplayFactory.getDisconnectTimeout();
                log().debug("found display rules from viewsdisplay.xml");
            } else {
                log().debug("did not find display rules from viewsdisplay.xml");
            }
        } catch (Exception e) {
            log().error("Couldn't open viewsdisplay factory on categories box: " + e, e);
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * For the given map of category names to Category objects, organize the
     * categories into the currently active display rules.
     * 
     * <p>
     * If there are no display rules, a single section named <em>Category</em>
     * will be returned. It will include all the categories in the category map,
     * in alphabetical order by category name.
     * </p>
     */
    public List<Section> getSections(Map<String, Category> categoryMap) throws IOException {
        if (m_sections != null) {
            // Just return the display rules as a list.
            return Arrays.asList(m_sections);
        }

        List<Section> sectionList = null;

        Section section = new Section();
        section.setSectionName("Category");

        // Put the categories in a TreeMap to sort them alphabetically.
        TreeMap<String, Category> orderedMap = new TreeMap<String, Category>(categoryMap);

        // Iterate over the categories, adding each to the name list.
        for (Iterator i = orderedMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Category category = (Category) entry.getValue();

            section.addCategory(category.getName());
        }

        // Add our one section to the sections list.
        sectionList = new ArrayList<Section>();
        sectionList.add(section);

        return sectionList;
    }

    public Map<String, List<Category>> getCategoryData() throws IOException, MarshalException, ValidationException {

        Map<String, Category> categoryMap = m_model.getCategoryMap();
        List<Section> sectionList = getSections(categoryMap);

        Map<String, List<Category>> categoryData = new LinkedHashMap<String, List<Category>>();

        for (Section section : sectionList) {
            List<Category> categories = new LinkedList<Category>();

            String[] categoryNames = section.getCategory();

            for (int j = 0; j < categoryNames.length; j++) {
                String categoryName = categoryNames[j];
                Category category = (Category) categoryMap.get(categoryName);

                if (category == null) {
                    categories.add(new Category(categoryName));
                } else {
                    categories.add(category);
                }
            }

            categoryData.put(section.getSectionName(), categories);
        }

        return categoryData;
    }

    /**
     * Returns the earliest update time for the categories in categoryData.
     * 
     * @param categoryData
     *            category data to evaluate. From getCategoryData().
     * @returns the earliest update time. If one of the categories has no RTC
     *          data, -1 is returned. If no categories exist in categoryData, 0
     *          is returned.
     */
    public long getEarliestUpdate(Map categoryData) {
        long earliestUpdate = 0;

        for (Iterator i = categoryData.keySet().iterator(); i.hasNext();) {
            String sectionName = (String) i.next();
            List categories = (List) categoryData.get(sectionName);

            for (Iterator j = categories.iterator(); j.hasNext();) {
                Category category = (Category) j.next();

                if (category.getLastUpdated() == null) {
                    return -1;
                } else if (earliestUpdate == 0 || earliestUpdate > category.getLastUpdated().getTime()) {
                    earliestUpdate = category.getLastUpdated().getTime();
                }
            }
        }

        return earliestUpdate;
    }

    public boolean isDisconnected() throws IOException, MarshalException, ValidationException {
        return isDisconnected(getEarliestUpdate(getCategoryData()));
    }

    public boolean isDisconnected(long earliestUpdate) {
        if (earliestUpdate < 1 || (earliestUpdate + m_disconnectTimeout) < System.currentTimeMillis()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * FIXME: This isn't used. This functionality is in category-box.jsp.
     *        It is marked private so that no one can use it unless they fix
     *        its accessability and this comment. :-P
     */
    @SuppressWarnings("unused")
    private void printBox(JspWriter out, HttpServletResponse response) throws IOException, MarshalException, ValidationException {

        Map categoryData = getCategoryData();

        out.println("<table width=\"100%\" border=\"1\" cellspacing=\"0\" " + "cellpadding=\"2\" bordercolor=\"black\" " + "bgcolor=\"#cccccc\">");

        long earliestUpdate = getEarliestUpdate(categoryData);
        boolean opennmsDisconnect = isDisconnected(earliestUpdate);

        for (Iterator i = categoryData.keySet().iterator(); i.hasNext();) {
            String sectionName = (String) i.next();

            out.println("<tr bgcolor=\"#999999\">");
            out.println("<td width=\"50%\"><b>" + sectionName + "</b></td>");
            out.println("<td width=\"20%\" align=\"right\">" + "<b>Outages</b></td>");
            out.println("<td width=\"30%\" align=\"right\">" + "<b>24hr Avail</b></td>");
            out.println("</tr>");

            List categories = (List) categoryData.get(sectionName);

            String title;
            String lastUpdated;
            long lastUpdatedTime;
            String outageText;
            String outageColor;
            String availText;
            String availColor;

            for (Iterator j = categories.iterator(); j.hasNext();) {
                Category category = (Category) j.next();
                String categoryName = category.getName();

                title = category.getTitle();

                outageColor = (opennmsDisconnect ? "lightblue" : category.getOutageColor());
                availColor = (opennmsDisconnect ? "lightblue" : category.getAvailColor());

                lastUpdated = (category.getLastUpdated() == null ? "Never" : category.getLastUpdated().toString());
                lastUpdatedTime = (category.getLastUpdated() == null ? -1 : category.getLastUpdated().getTime());

                outageText = category.getOutageText();

                availText = "<b>" + category.getAvailText() + "</b>";

                out.println("<tr>");

                out.println("<td><a href=\"rtc/category.jsp?category=" + URLEncoder.encode(response.encodeURL(categoryName), "UTF-8") + "\" title=\"" + title + "\">" + categoryName + "</a></td>");
                out.println("<td bgcolor=\"" + outageColor + "\" align=\"right\" title=\"Updated: " + lastUpdated + "\">" + outageText + "</td>");
                out.println("<td bgcolor=\"" + availColor + "\" align=\"right\" title=\"Updated: " + lastUpdated + "\">" + availText + "</td>");
                out.println("<!-- Last updated " + lastUpdated + " -->");
                out.println("<!-- Epoch time:  " + lastUpdatedTime + " -->");

                out.println("</tr>");
            }
        }

        out.println("<tr bgcolor=\"#999999\">");
        if (opennmsDisconnect) {
            out.println("<td colspan=\"3\"><font color=\"#bb1111\">" + "OpenNMS Disconnect -- is the OpenNMS daemon " + "running?<br/>Last update: " + (earliestUpdate > 0 ? new Date(earliestUpdate).toString() : "one or more categories have never been updated.") + "</font></td>");
        } else {
            out.println("<td colspan=\"3\">Percentage over last " + "24 hours</td>");
        }

        out.println("</tr>");
        out.println("</table>");
    }
}
