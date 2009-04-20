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
//       Massimiliano Dess“
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.util;

/**
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public class Constants {

    public static final String OS_SEPARATOR = System.getProperty("file.separator");
    public static final String LOG_NAME = "logfile_application";

    public static String ID = "id";
    public static String STRATEGIES = "strategies";
    public static String COMMA = ",";

    public static String UI_USERS = "users";
    public static String UI_USER = "user";
    public static String UI_PAGE = "pg";
    public static String UI_ITEMS = "items";
    public static String USER_SID = "sid";
    public static String AUTHORITY_ID = "aid";
    public static String GROUP_ID = "gid";
    public static String UI_MODE = "mode";
    public static String UI_USER_AUTHORITIES = "userAuthorities";
    public static String UI_USER_GROUPS = "userGroups";

    public static final String PAGE = "page";
    public static final String PAGER = "pager";
    public static String PAGE_NUMBER = "pg";
    public static String USER = "user";

    public static String GROUPS = "groups";
    public static String GROUP = "group";
    public static String AUTHORITIES = "authorities";
    public static String AUTHORITY = "authority";
    public static String AUTHORITY_ITEMS = "authorityItems";
    public static String GROUP_AUTHORITIES = "groupAuthorities";
    public static String AUTHORITY_DETAIL = "authorityDetail";

    public static String INCLUDE_HIDDEN = "includedHidden";

    public static String MESSAGE = "msg";
    public static String DELETE = "delete";

    public static String MSG_AUTHORITY_DELETE_SUCCESS = "authority.delete.success";
    public static String MSG_AUTHORITY_DELETE_FAILURE = "authority.delete.failure";

    public static String REDIRECT_AUTHORITY_LIST = "redirect:authority.list.page";
    public static String REDIRECT_GROUP_LIST = "redirect:group.list.page";
    public static String REDIRECT_USER_LIST = "redirect:user.list.page";
    public static String REDIRECT_USER_AUTHORITIES = "redirect:user.authorities.page";

    public static String CONFIG_ACL_CACHE = "aclCacheName";

    public static String ROLE_ADMIN = "ROLE_ADMIN";
    public static String ROLE_VIEW_USER = "ROLE_VIEW_USER";
    public static String AUTHORITIES_ITEMS = "authItems";

    public final static String ALGORITHM_SHA = "SHA-1";
}
