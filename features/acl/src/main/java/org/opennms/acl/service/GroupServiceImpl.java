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
package org.opennms.acl.service;

import java.util.List;

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.model.Pager;
import org.opennms.acl.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Service("groupService")
public class GroupServiceImpl implements GroupService {

    public List<GroupDTO> getUserGroupsWithAutorities(String username) {
        return repository.getUserGroupsWithAutorities(username);
    }

    public Boolean deleteUserGroups(String username) {
        return repository.deleteUserGroups(username);
    }

    public List<GroupDTO> getFreeGroups(String username) {
        return repository.getFreeGroups(username);
    }

    public GroupDTO getGroup(Integer id) {
        return repository.getGroup(id);
    }

    public List<GroupDTO> getGroups() {
        return repository.getGroups();
    }

    public List<GroupDTO> getGroups(Pager pager) {
        return repository.getGroups(pager);
    }

    public List<GroupDTO> getUserGroups(String username) {
        return repository.getUserGroups(username);
    }

    public Boolean removeGroup(Integer id) {
        return repository.removeGroup(id);
    }

    public Boolean save(GroupDTO group) {
        return repository.save(group);
    }

    public Boolean saveGroups(String username, List<Integer> groups) {
        return repository.saveGroups(username, groups);
    }

    public Integer getTotalItemsNumber() {
        return repository.getGroupsNumber();
    }

    public Boolean hasUsers(Integer id) {
        return repository.hasUsers(id);
    }

    @Autowired
    private GroupRepository repository;

}