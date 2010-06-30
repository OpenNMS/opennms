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
package org.opennms.acl.util;

/**
 * <p>Constants class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class Constants {

    /** Constant <code>OS_SEPARATOR="System.getProperty(file.separator)"</code> */
    public static final String OS_SEPARATOR = System.getProperty("file.separator");
    /** Constant <code>LOG_NAME="logfile_application"</code> */
    public static final String LOG_NAME = "logfile_application";

    /** Constant <code>ID="id"</code> */
    public static String ID = "id";
    /** Constant <code>STRATEGIES="strategies"</code> */
    public static String STRATEGIES = "strategies";
    /** Constant <code>COMMA=","</code> */
    public static String COMMA = ",";

    /** Constant <code>UI_USERS="users"</code> */
    public static String UI_USERS = "users";
    /** Constant <code>UI_USER="user"</code> */
    public static String UI_USER = "user";
    /** Constant <code>UI_PAGE="pg"</code> */
    public static String UI_PAGE = "pg";
    /** Constant <code>UI_ITEMS="items"</code> */
    public static String UI_ITEMS = "items";
    /** Constant <code>USER_SID="sid"</code> */
    public static String USER_SID = "sid";
    /** Constant <code>AUTHORITY_ID="aid"</code> */
    public static String AUTHORITY_ID = "aid";
    /** Constant <code>GROUP_ID="gid"</code> */
    public static String GROUP_ID = "gid";
    /** Constant <code>UI_MODE="mode"</code> */
    public static String UI_MODE = "mode";
    /** Constant <code>UI_USER_AUTHORITIES="userAuthorities"</code> */
    public static String UI_USER_AUTHORITIES = "userAuthorities";
    /** Constant <code>UI_USER_GROUPS="userGroups"</code> */
    public static String UI_USER_GROUPS = "userGroups";

    /** Constant <code>PAGE="page"</code> */
    public static final String PAGE = "page";
    /** Constant <code>PAGER="pager"</code> */
    public static final String PAGER = "pager";
    /** Constant <code>PAGE_NUMBER="pg"</code> */
    public static String PAGE_NUMBER = "pg";
    /** Constant <code>USER="user"</code> */
    public static String USER = "user";

    /** Constant <code>GROUPS="groups"</code> */
    public static String GROUPS = "groups";
    /** Constant <code>GROUP="group"</code> */
    public static String GROUP = "group";
    /** Constant <code>AUTHORITIES="authorities"</code> */
    public static String AUTHORITIES = "authorities";
    /** Constant <code>AUTHORITY="authority"</code> */
    public static String AUTHORITY = "authority";
    /** Constant <code>AUTHORITY_ITEMS="authorityItems"</code> */
    public static String AUTHORITY_ITEMS = "authorityItems";
    /** Constant <code>GROUP_AUTHORITIES="groupAuthorities"</code> */
    public static String GROUP_AUTHORITIES = "groupAuthorities";
    /** Constant <code>AUTHORITY_DETAIL="authorityDetail"</code> */
    public static String AUTHORITY_DETAIL = "authorityDetail";

    /** Constant <code>INCLUDE_HIDDEN="includedHidden"</code> */
    public static String INCLUDE_HIDDEN = "includedHidden";

    /** Constant <code>MESSAGE="msg"</code> */
    public static String MESSAGE = "msg";
    /** Constant <code>DELETE="delete"</code> */
    public static String DELETE = "delete";

    /** Constant <code>MSG_AUTHORITY_DELETE_SUCCESS="authority.delete.success"</code> */
    public static String MSG_AUTHORITY_DELETE_SUCCESS = "authority.delete.success";
    /** Constant <code>MSG_AUTHORITY_DELETE_FAILURE="authority.delete.failure"</code> */
    public static String MSG_AUTHORITY_DELETE_FAILURE = "authority.delete.failure";

    /** Constant <code>REDIRECT_AUTHORITY_LIST="redirect:authority.list.page"</code> */
    public static String REDIRECT_AUTHORITY_LIST = "redirect:authority.list.page";
    /** Constant <code>REDIRECT_GROUP_LIST="redirect:group.list.page"</code> */
    public static String REDIRECT_GROUP_LIST = "redirect:group.list.page";
    /** Constant <code>REDIRECT_USER_LIST="redirect:user.list.page"</code> */
    public static String REDIRECT_USER_LIST = "redirect:user.list.page";
    /** Constant <code>REDIRECT_USER_AUTHORITIES="redirect:user.authorities.page"</code> */
    public static String REDIRECT_USER_AUTHORITIES = "redirect:user.authorities.page";

    /** Constant <code>CONFIG_ACL_CACHE="aclCacheName"</code> */
    public static String CONFIG_ACL_CACHE = "aclCacheName";

    /** Constant <code>ROLE_ADMIN="ROLE_ADMIN"</code> */
    public static String ROLE_ADMIN = "ROLE_ADMIN";
    /** Constant <code>ROLE_VIEW_USER="ROLE_VIEW_USER"</code> */
    public static String ROLE_VIEW_USER = "ROLE_VIEW_USER";
    /** Constant <code>AUTHORITIES_ITEMS="authItems"</code> */
    public static String AUTHORITIES_ITEMS = "authItems";

    /** Constant <code>ALGORITHM_SHA="SHA-1"</code> */
    public final static String ALGORITHM_SHA = "SHA-1";
}
