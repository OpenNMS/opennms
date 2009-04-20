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
package org.opennms.acl.ui;

import javax.servlet.http.HttpServletRequest;

import org.opennms.acl.domain.Authority;
import org.opennms.acl.exception.AuthorityNotFoundException;
import org.opennms.acl.model.Pager;
import org.opennms.acl.service.AuthorityService;
import org.opennms.acl.ui.util.WebUtils;
import org.opennms.acl.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Authority Controller
 * 
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Controller
public class AuthorityController {

    @RequestMapping("/authority.list.page")
    public ModelAndView list(HttpServletRequest req) {
        Pager pager = WebUtils.getPager(req, authorityService.getTotalItemsNumber(), 15);
        ModelAndView mav = new ModelAndView("authority/list");
        mav.addObject(Constants.AUTHORITIES, authorityService.getAuthorities(pager));
        mav.addObject(Constants.PAGER, pager);
        return mav;
    }

    @RequestMapping("/authority.detail.page")
    public ModelAndView detail(HttpServletRequest req) {
        Authority authority = WebUtils.getAuthority(req);
        return new ModelAndView("authority/detail", Constants.AUTHORITY, authority.getAuthorityView());
    }

    @RequestMapping("/authority.delete.page")
    public ModelAndView delete(HttpServletRequest req) {
        Authority authority = WebUtils.getAuthority(req);
        ModelAndView mav = new ModelAndView(Constants.REDIRECT_AUTHORITY_LIST);
        mav.addObject(Constants.MESSAGE, authority.remove() ? Constants.MSG_AUTHORITY_DELETE_SUCCESS : Constants.MSG_AUTHORITY_DELETE_FAILURE);
        return mav;
    }

    @RequestMapping("/authority.confirm.page")
    public ModelAndView confirmDelete(HttpServletRequest req) {
        Authority authority = WebUtils.getAuthority(req);
        ModelAndView mav = new ModelAndView("authority/detail");
        mav.addObject(Constants.AUTHORITY, authority.getAuthorityView());
        mav.addObject(Constants.UI_MODE, Constants.DELETE);
        return mav;
    }

    @RequestMapping("/authority.items.page")
    public ModelAndView items(HttpServletRequest req) {
        Authority authority = WebUtils.getAuthority(req);
        if (authority != null) {
            ModelAndView mav = new ModelAndView("authority/items");
            mav.addObject(Constants.AUTHORITY, authority.getAuthorityView());
            mav.addObject(Constants.UI_ITEMS, authority.getFreeItems());
            mav.addObject(Constants.AUTHORITY_ITEMS, authority.getItems());
            return mav;
        } else {
            throw new AuthorityNotFoundException("id not found");
        }
    }

    @RequestMapping("/authority.selection.page")
    public ModelAndView selection(@RequestParam("includedHidden") String ids, HttpServletRequest req) {
        Authority authority = WebUtils.getAuthority(req);
        if (ids != null && ids.length() > 0) {
            authority.setNewItems(WebUtils.extractIdGrantedAuthorityFromString(ids, Constants.COMMA));
            authority.save();
        }
        return new ModelAndView(new StringBuilder(Constants.REDIRECT_AUTHORITY_LIST).append("?").append(Constants.AUTHORITY_ID).append("=").append(authority.getId()).toString());
    }

    @Autowired
    public AuthorityController(@Qualifier("authorityService") AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private final AuthorityService authorityService;

}