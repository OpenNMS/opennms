//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
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
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public class WebUtils {

    public static int getIntId(HttpServletRequest req) {
        return ServletRequestUtils.getIntParameter(req, Constants.ID, 0);
    }

    public static int getIntParam(HttpServletRequest req, String name) {
        return ServletRequestUtils.getIntParameter(req, name, 0);
    }

    public static boolean addSessionAttribute(HttpSession session, String name, Object value) {
        if (value != null) {
            session.setAttribute(name, value);
            return true;
        } else {
            return false;
        }
    }

    public static int getNumPage(HttpServletRequest req) {
        return ServletRequestUtils.getIntParameter(req, Constants.UI_PAGE, 0);
    }

    public static Pager getPager(HttpServletRequest req) {
        return (Pager) req.getAttribute(Constants.PAGER);
    }

    public static GenericUser getUser(HttpServletRequest req) {
        return (GenericUser) req.getAttribute(Constants.USER);
    }

    public static Authority getAuthority(HttpServletRequest req) {
        return (Authority) req.getAttribute(Constants.AUTHORITY);
    }

    public static Group getGroup(HttpServletRequest req) {
        return (Group) req.getAttribute(Constants.GROUP);
    }

    public static List<Integer> extractIdGrantedAuthorityFromString(String line, String separator) {
        String[] fields = line.split(separator);
        List<Integer> ids = new ArrayList<Integer>();
        for (Integer i = 0; i < fields.length; i++) {
            ids.add(new Integer(fields[i]));
        }
        return ids;
    }

    public static Pager getPager(HttpServletRequest req, Integer totalItemsNumber, Integer numberItemsOnPage) {
        int page = ServletRequestUtils.getIntParameter(req, Constants.PAGE_NUMBER, 0);
        int numeroPagineMax = getMaxPageNumber(totalItemsNumber, numberItemsOnPage);
        if (page > numeroPagineMax) {
            page = numeroPagineMax;
        }
        return new Pager(page, numeroPagineMax, numberItemsOnPage);
    }

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
