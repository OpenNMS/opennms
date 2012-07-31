/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.views.Categories;
import org.opennms.netmgt.config.views.Header;
import org.opennms.netmgt.config.views.Member;
import org.opennms.netmgt.config.views.Membership;
import org.opennms.netmgt.config.views.View;
import org.opennms.netmgt.config.views.Viewinfo;
import org.opennms.netmgt.config.views.Views;
import org.springframework.core.io.FileSystemResource;

/**
 * <p>ViewFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ViewFactory {
    /**
     * The static singleton instance of the ViewFactory
     */
    private static ViewFactory instance;

    /**
     * File path of views.xml
     */
    protected static File usersFile;

    /**
     * A mapping of views ids to the View objects
     */
    protected static HashMap<String, View> m_views;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    private static Header oldHeader;

    /**
     * Initializes the factory
     */
    ViewFactory() {
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (!initialized) {
            reload();
        }
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * ViewFactory
     *
     * @return the single view factory instance
     */
    static synchronized public ViewFactory getInstance() {
        if (!initialized)
            return null;

        if (instance == null) {
            instance = new ViewFactory();
        }

        return instance;
    }

    /**
     * Parses the views.xml via the Castor classes
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        Viewinfo viewinfo = CastorUtils.unmarshal(Viewinfo.class, new FileSystemResource(ConfigFileConstants.getFile(ConfigFileConstants.VIEWS_CONF_FILE_NAME)));
        Views views = viewinfo.getViews();
        oldHeader = viewinfo.getHeader();
        Collection<View> viewsList = views.getViewCollection();
        m_views = new HashMap<String, View>();

        for (View curView : viewsList) {
            m_views.put(curView.getName(), curView);
        }

        initialized = true;
    }

    /**
     * Adds a new user and overwrites the "users.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @param details a {@link org.opennms.netmgt.config.views.View} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void saveView(String name, View details) throws Exception {
        if (name == null || details == null) {
            throw new Exception("UserFactory:saveUser  null");
        } else {
            m_views.put(name, details);
        }

        // Saves into "views.xml" file
        Views views = new Views();
        Collection<View> viewList = (Collection<View>) m_views.values();
        views.setView(new ArrayList<View>(viewList));
        saveViews(views);
    }

    /**
     * Removes the user from the list of users. Then overwrites to the
     * "users.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void deleteUser(String name) throws Exception {
        // Check if the user exists
        if (name != null) {
            // Remove the user in the view.
            Set<String> viewKeys = (Set<String>) m_views.keySet();
            Map<String, View> map = new HashMap<String, View>();

            View view;
            Iterator<String> iter = viewKeys.iterator();
            while (iter.hasNext()) {
                View newView = new View();
                view = m_views.get(iter.next());
                newView = view;

                Membership membership = new Membership();
                Membership viewmembers = view.getMembership();
                if (viewmembers != null) {
                    for (Member member : viewmembers.getMemberCollection()) {
                        if (member.getType().equals("user")) {
                            if (!member.getContent().equals(name)) {
                                membership.addMember(member);
                            }
                        } else
                            membership.addMember(member);
                    }
                }
                newView.setMembership(membership);
                map.put(newView.getName(), newView);
            }
            // Saves into "views.xml" file
            m_views.clear();
            Views views = new Views();
            views.setView(new ArrayList<View>(map.values()));
            saveViews(views);
        } else {
            throw new Exception("ViewFactory: attempt to delete null user name.");
        }
    }

    /**
     * When this method is called users name is changed, so also is the username
     * belonging to the group and the view. Also overwrites the "users.xml" file
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void renameUser(String oldName, String newName) throws Exception {
        // Get the old data
        if (oldName == null || oldName == "") {
            throw new Exception("ViewFactory:renameUser Invalid old name");
        }
        if (newName == null || newName == "") {
            throw new Exception("ViewFactory:renameUser Invalid new name");
        }
        Collection<View> coll = m_views.values();
        Iterator<View> iter = coll.iterator();
        Map<String, View> map = new HashMap<String, View>();

        while (iter.hasNext()) {
            View view = iter.next();
            Membership membership = view.getMembership();
            if (membership != null) {
                Collection<Member> memberColl = membership.getMemberCollection();
                if (memberColl != null) {
                    Iterator<Member> iterMember = memberColl.iterator();
                    while (iterMember != null && iterMember.hasNext()) {
                        Member member = iterMember.next();
                        if (member.getType().equals("user")) {
                            String name = member.getContent();
                            if (name.equals(oldName)) {
                                member.setContent(newName);
                            }
                        }
                    }
                }
            }
            view.setMembership(membership);
            map.put(view.getName(), view);
        }
        m_views.clear();
        Views views = new Views();
        views.setView(new ArrayList<View>(map.values()));
        saveViews(views);
    }

    /**
     * When this method is called users name is changed. Also overwrites the
     * "views.xml" file
     */
    /*
     * public synchronized void renameGroup(String oldName, String newName)
     * throws Exception {
     *  // Check if the user exists if(oldName != null || !oldName.equals("")) { //
     * Rename the group in the view. Enumeration viewKeys =
     * (Enumeration)m_views.values(); View view;
     * while(viewKeys.hasMoreElements()) { view =
     * (View)m_views.get((String)viewKeys.nextElement()); Membership membership =
     * view.getMembership(); Enumeration enummember =
     * membership.enumerateMember(); while(enummember.hasMoreElements()) {
     * Member member = (Member)enummember.nextElement();
     * if(member.getContent().equals(oldName)) {
     * if(member.getType().equals("group")) { membership.removeMember(member);
     * member.setContent(newName); membership.addMember(member); break; } } } } }
     * else { throw new Exception("ViewFactory:rename Invalid view name:" +
     * oldName ); } // Saves into "views.xml" file Collection coll =
     * (Collection) m_views.values(); Views views = new Views();
     * views.setViewCollection(coll); saveViews(views); }
     */

    /**
     * Removes the group from the list of groups. Then overwrites to the
     * "views.xml"
     */
    /*
     * public synchronized void deleteGroup(String name) throws Exception { //
     * Check if the user exists if(name != null || !name.equals("")) { // Remove
     * the user in the view. Enumeration viewKeys =
     * (Enumeration)m_views.values(); View view;
     * while(viewKeys.hasMoreElements()) { view =
     * (View)m_views.get((String)viewKeys.nextElement()); Membership membership =
     * view.getMembership(); Enumeration enummember =
     * membership.enumerateMember(); while(enummember.hasMoreElements()) {
     * Member member = (Member)enummember.nextElement();
     * if(member.getContent().equals(name)) {
     * if(member.getType().equals("group")) { membership.removeMember(member);
     * break; } } } } } else { throw new Exception("ViewFactory:delete Invalid
     * group name:" + name ); }
     *  // Saves into "views.xml" file Collection coll = (Collection)
     * m_views.values(); Views views = new Views();
     * views.setViewCollection(coll); saveViews(views); }
     */

    /**
     * When this method is called views name is changed. Also overwrites the
     * "views.xml" file
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void renameView(String oldName, String newName) throws Exception {
        View view;

        // Check if the group exists
        if (oldName == null || "".equals(oldName)) {
            throw new Exception("ViewFactory:rename Invalid old view name");
        }
        if (newName == null || "".equals(newName)) {
            throw new Exception("ViewFactory:rename Invalid new view name");
        }
        if (m_views.containsKey(oldName)) {
            view = m_views.get(oldName);

            // Remove the view.
            m_views.remove(oldName);
            view.setName(newName);
            m_views.put(newName, view);
        }
        // Saves into "views.xml" file
        Collection<View> coll = m_views.values();
        Views views = new Views();
        views.setView(new ArrayList<View>(coll));
        saveViews(views);
    }

    /**
     * When this method is called view is to be deleted. Also overwrites the
     * "views.xml" file
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void deleteView(String name) throws Exception {
        // Check if the view exists
        if (name == null || name.equals("")) {
            throw new Exception("ViewFactory:deleteView  " + name);
        } else if (!m_views.containsKey(name)) {
            throw new Exception("ViewFactory:deleteView  View:" + name + " not found ");
        } else {
            m_views.remove(name);
        }
        // Saves into "views.xml" file
        Views views = new Views();
        Collection<View> viewList = (Collection<View>) m_views.values();
        views.setView(new ArrayList<View>(viewList));
        saveViews(views);
    }

    /**
     * <p>saveViews</p>
     *
     * @param views a {@link org.opennms.netmgt.config.views.Views} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void saveViews(Views views) throws IOException, MarshalException, ValidationException {
        // make a backup and save to xml
        Viewinfo vinfo = new Viewinfo();
        Header header = oldHeader;
        vinfo.setViews(views);
        header.setCreated(EventConstants.formatToString(new Date()));
        vinfo.setHeader(header);

        // Saves into "views.xml" file
        //
        File ofile = ConfigFileConstants.getFile(ConfigFileConstants.VIEWS_CONF_FILE_NAME);

        // marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(vinfo, stringWriter);
        if (stringWriter.toString() != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(ofile), "UTF-8");
            fileWriter.write(stringWriter.toString());
            fileWriter.flush();
            fileWriter.close();
        }

        // clear out the internal structure and reload it
        m_views.clear();

        Enumeration<View> en = views.enumerateView();
        while (en.hasMoreElements()) {
            View curView = en.nextElement();
            m_views.put(curView.getName(), curView);
        }
    }

    /**
     * <p>getCategoryComments</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @param categoryName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getCategoryComments(String viewName, String categoryName) {
        View view = m_views.get(viewName);
        Categories categories = view.getCategories();
        Collection<org.opennms.netmgt.config.views.Category> category = categories.getCategoryCollection();
        Iterator<org.opennms.netmgt.config.views.Category> iter = category.iterator();
        org.opennms.netmgt.config.views.Category cat;
        while (iter.hasNext()) {
            cat = iter.next();
            String name = cat.getLabel();
            if (name != null && name.equals(categoryName)) {
                return stripWhiteSpace(cat.getCategoryComment());
            }
        }

        return null;
    }

    private String stripWhiteSpace(String comment) {
        StringBuffer buffer = new StringBuffer(comment);

        try {
            RE whiteSpaceRE = new RE("[:space:]");

            for (int i = 0; i < buffer.length(); i++) {
                int start = i;
                int end = start + 1;
                boolean foundWhiteSpace = false;
                while (end < buffer.length() && whiteSpaceRE.match(buffer.substring(end - 1, end))) {
                    foundWhiteSpace = true;
                    end++;
                }

                if (foundWhiteSpace) {
                    buffer.replace(start, end - 1, " ");
                }
            }
        } catch (RESyntaxException e) {
            return comment;
        }

        return buffer.toString();
    }

    /**
     * Return a <code>Map</code> of usernames to user instances.
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.views.View} object.
     */
    public View getView(String name) {
        return m_views.get(name);
    }

    /**
     * Return a <code>Map</code> of usernames to user instances.
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, View> getViews() {
        return Collections.unmodifiableMap(m_views);
    }
}
