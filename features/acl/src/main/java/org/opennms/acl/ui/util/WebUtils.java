/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.ui.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.opennms.acl.domain.Authority;
import org.opennms.acl.domain.GenericUser;
import org.opennms.acl.domain.Group;
import org.opennms.acl.model.Pager;
import org.opennms.acl.util.Constants;
import org.springframework.web.bind.ServletRequestUtils;

/**
 * <p>WebUtils class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public abstract class WebUtils {

    /**
     * <p>getIntId</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a int.
     */
    public static int getIntId(HttpServletRequest req) {
        return ServletRequestUtils.getIntParameter(req, Constants.ID, 0);
    }

    /**
     * <p>getIntParam</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @param name a {@link java.lang.String} object.
     * @return a int.
     */
    public static int getIntParam(HttpServletRequest req, String name) {
        return ServletRequestUtils.getIntParameter(req, name, 0);
    }

    /**
     * <p>addSessionAttribute</p>
     *
     * @param session a {@link javax.servlet.http.HttpSession} object.
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     * @return a boolean.
     */
    public static boolean addSessionAttribute(HttpSession session, String name, Object value) {
        if (value != null) {
            session.setAttribute(name, value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>getNumPage</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a int.
     */
    public static int getNumPage(HttpServletRequest req) {
        return ServletRequestUtils.getIntParameter(req, Constants.UI_PAGE, 0);
    }

    /**
     * <p>getPager</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.acl.model.Pager} object.
     */
    public static Pager getPager(HttpServletRequest req) {
        return (Pager) req.getAttribute(Constants.PAGER);
    }

    /**
     * <p>getUser</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.acl.domain.GenericUser} object.
     */
    public static GenericUser getUser(HttpServletRequest req) {
        return (GenericUser) req.getAttribute(Constants.USER);
    }

    /**
     * <p>getAuthority</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.acl.domain.Authority} object.
     */
    public static Authority getAuthority(HttpServletRequest req) {
        return (Authority) req.getAttribute(Constants.AUTHORITY);
    }

    /**
     * <p>getGroup</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.acl.domain.Group} object.
     */
    public static Group getGroup(HttpServletRequest req) {
        return (Group) req.getAttribute(Constants.GROUP);
    }

    /**
     * <p>extractIdGrantedAuthorityFromString</p>
     *
     * @param line a {@link java.lang.String} object.
     * @param separator a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Integer> extractIdGrantedAuthorityFromString(String line, String separator) {
        String[] fields = line.split(separator);
        List<Integer> ids = new ArrayList<Integer>();
        for (Integer i = 0; i < fields.length; i++) {
            ids.add(new Integer(fields[i]));
        }
        return ids;
    }

    /**
     * <p>getPager</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @param totalItemsNumber a {@link java.lang.Integer} object.
     * @param numberItemsOnPage a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.acl.model.Pager} object.
     */
    public static Pager getPager(HttpServletRequest req, Integer totalItemsNumber, Integer numberItemsOnPage) {
        int page = ServletRequestUtils.getIntParameter(req, Constants.PAGE_NUMBER, 0);
        int numeroPagineMax = getMaxPageNumber(totalItemsNumber, numberItemsOnPage);
        if (page > numeroPagineMax) {
            page = numeroPagineMax;
        }
        return new Pager(page, numeroPagineMax, numberItemsOnPage);
    }

    /**
     * <p>getPager</p>
     *
     * @param page a int.
     * @param totalItemsNumber a {@link java.lang.Integer} object.
     * @param numberItemsOnPage a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.acl.model.Pager} object.
     */
    public static Pager getPager(int page, Integer totalItemsNumber, Integer numberItemsOnPage) {
        int numeroPagineMax = getMaxPageNumber(totalItemsNumber, numberItemsOnPage);
        if (page > numeroPagineMax) {
            page = numeroPagineMax;
        }
        return new Pager(page, numeroPagineMax, numberItemsOnPage);
    }

    private static Integer getMaxPageNumber(Integer itemsNumberTotal, Integer itemsNumber) {
        Double numberPageMax = new Double(1);
        if (itemsNumberTotal > itemsNumber) {
            numberPageMax = Math.ceil(new Double(itemsNumberTotal) / new Double(itemsNumber));
        }
        return numberPageMax.intValue();
    }
}
