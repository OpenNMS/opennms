/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 30, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.AdminCategoryService;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class DefaultAdminCategoryService implements
        AdminCategoryService {
    
    private CategoryDao m_categoryDao;
    private NodeDao m_nodeDao;
    private EventProxy m_eventProxy;
    
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao dao) {
        m_categoryDao = dao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }

    public CategoryAndMemberNodes getCategory(String categoryIdString) {
        if (categoryIdString == null) {
            throw new IllegalArgumentException("categoryIdString must not be null");
        }

        OnmsCategory category = findCategory(categoryIdString);
        
        Collection<OnmsNode> memberNodes =
            m_nodeDao.findByCategory(category);
        // XXX does anything need to be initialized in each member node?
        
        return new CategoryAndMemberNodes(category, memberNodes);
    }
    
    private OnmsCategory findCategory(String name) {
        int categoryId = -1;
        try {
            categoryId = WebSecurityUtils.safeParseInt(name);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parameter 'categoryid' "
                                               + "with value '"
                                               + name
                                               + "' could not be parsed "
                                               + "as an integer");
        }

        OnmsCategory category = m_categoryDao.get(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Could not find category "
                                               + "with category ID "
                                               + categoryId);
        }

        return category;
    }

    public List<OnmsNode> findAllNodes() {
        List<OnmsNode> list =
            new ArrayList<OnmsNode>(m_nodeDao.findAll());
        Collections.sort(list);
        
        return list;
    }
    
    public EditModel findCategoryAndAllNodes(String categoryIdString) {
        CategoryAndMemberNodes cat = getCategory(categoryIdString); 
        List<OnmsNode> monitoredNodes = findAllNodes();
        return new EditModel(cat.getCategory(), monitoredNodes,
                             cat.getMemberNodes());
    }

    public void performEdit(String categoryIdString, String editAction,
            String[] toAdd, String[] toDelete) {
        if (categoryIdString == null) {
            throw new IllegalArgumentException("categoryIdString cannot be null");
        }
        if (editAction == null) {
            throw new IllegalArgumentException("editAction cannot be null");
        }
        
        OnmsCategory category = findCategory(categoryIdString);
       
        if (editAction.contains("Add")) { // @i18n
            if (toAdd == null) {
                return;
                //throw new IllegalArgumentException("toAdd cannot be null if editAction is 'Add'");
            }
           
            for (String idString : toAdd) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toAdd element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsNode node = m_nodeDao.get(id);
                if (node == null) {
                    throw new IllegalArgumentException("node with "
                                                       + "id of " + id
                                                       + "could not be found");
                }
                if (node.getCategories().contains(category)) {
                    throw new IllegalArgumentException("node with "
                                                       + "id of " + id
                                                       + "is already a member of "
                                                       + "category "
                                                       + category.getName());
                }
                node.addCategory(category);
                m_nodeDao.save(node);
            }
       } else if (editAction.contains("Remove")) { // @i18n
            if (toDelete == null) {
                return;
                //throw new IllegalArgumentException("toDelete cannot be null if editAction is 'Remove'");
            }
            
            for (String idString : toDelete) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toDelete element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsNode node = m_nodeDao.get(id);
                if (node == null) {
                    throw new IllegalArgumentException("node with "
                                                       + "id of " + id
                                                       + "could not be found");
                }
                if (!node.getCategories().contains(category)) {
                    throw new IllegalArgumentException("Node with "
                                                       + "id of " + id
                                                       + "is not a member of "
                                                       + "category "
                                                       + category.getName());
                }
                node.removeCategory(category);
                m_nodeDao.save(node);
            }
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }

    public OnmsCategory addNewCategory(final String name) {
        OnmsCategory category = new OnmsCategory();
        category.setName(name);
        m_categoryDao.save(category);
        return category;
    }

    public OnmsCategory getCategoryWithName(final String name) {
        return m_categoryDao.findByName(name);
    }

    public List<OnmsCategory> findAllCategories() {
        Collection<OnmsCategory> categories = m_categoryDao.findAll();
        List<OnmsCategory> sortedCategories =
            new ArrayList<OnmsCategory>(categories);
        Collections.sort(sortedCategories, new Comparator<OnmsCategory>() {
            public int compare(OnmsCategory o1, OnmsCategory o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return sortedCategories;
    }

    public void removeCategory(String categoryIdString) {
        OnmsCategory category = findCategory(categoryIdString);
        CategoryAndMemberNodes cat = getCategory(categoryIdString);
        for (OnmsNode adriftNode : cat.getMemberNodes()) {
        	notifyCategoryChange(adriftNode.getId());
        }
        m_categoryDao.delete(category);
    }

    public List<OnmsCategory> findByNode(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("node with "
                                               + "id of " + nodeId
                                               + "could not be found");
        }
        
        List<OnmsCategory> categories = new ArrayList<OnmsCategory>(node.getCategories());
        Collections.sort(categories);
        return categories;
    }
    
    public NodeEditModel findNodeCategories(String nodeIdString) {
        if (nodeIdString == null) {
            throw new IllegalArgumentException("nodeIdString must not be null");
        }

        OnmsNode node = findNode(nodeIdString);
        List<OnmsCategory> categories = findAllCategories();
        
        return new NodeEditModel(node, categories);
    }
    
    public void performNodeEdit(String nodeIdString, String editAction,
            String[] toAdd, String[] toDelete) {
        if (nodeIdString == null) {
            throw new IllegalArgumentException("nodeIdString cannot be null");
        }
        if (editAction == null) {
            throw new IllegalArgumentException("editAction cannot be null");
        }
        
        OnmsNode node = findNode(nodeIdString);
       
        if (editAction.contains("Add")) { // @i18n
            if (toAdd == null) {
                return;
                //throw new IllegalArgumentException("toAdd cannot be null if editAction is 'Add'");
            }
           
            for (String idString : toAdd) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toAdd element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsCategory category = m_categoryDao.get(id);
                if (node == null) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + "could not be found");
                }
                if (node.getCategories().contains(category)) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + "is already on node "
                                                       + node.getLabel());
                }
                node.getCategories().add(category);
            }
            
            m_nodeDao.save(node);
            notifyCategoryChange(WebSecurityUtils.safeParseInt(nodeIdString));
       } else if (editAction.contains("Remove")) { // @i18n
            if (toDelete == null) {
                return;
                //throw new IllegalArgumentException("toDelete cannot be null if editAction is 'Remove'");
            }
            
            for (String idString : toDelete) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toDelete element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsCategory category = m_categoryDao.get(id);
                if (node == null) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + "could not be found");
                }
                if (!node.getCategories().contains(category)) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + "is not on node "
                                                       + node.getLabel());
                }
                node.getCategories().remove(category);
            }

            m_nodeDao.save(node);
            notifyCategoryChange(WebSecurityUtils.safeParseInt(nodeIdString));
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }


    private OnmsNode findNode(String nodeIdString) {
        int nodeId;
        
        try {
            nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parameter 'node' "
                                               + "with value '"
                                               + nodeIdString
                                               + "' could not be parsed "
                                               + "as an integer");
        }
        return m_nodeDao.get(nodeId);
    }

    private void notifyCategoryChange(int nodeId) {
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI, "CategoryUI");
        bldr.setNodeid(nodeId);
        send(bldr.getEvent());
    }
    
    private void send(Event e) {
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException e1) {
            throw new DataSourceLookupFailureException("Unable to send event to eventd", e1);
        }
    }
    
    public class CategoryAndMemberNodes {
        private OnmsCategory m_category;
        private Collection<OnmsNode> m_memberNodes;

        public CategoryAndMemberNodes(OnmsCategory category,
                Collection<OnmsNode> memberNodes) {
            m_category = category;
            m_memberNodes = memberNodes;
        }

        public OnmsCategory getCategory() {
            return m_category;
        }

        public Collection<OnmsNode> getMemberNodes() {
            return m_memberNodes;
        }
    }

    public class EditModel {
        private OnmsCategory m_category;
        private List<OnmsNode> m_nodes;
        private List<OnmsNode> m_sortedMemberNodes;

        public EditModel(OnmsCategory category, List<OnmsNode> nodes,
                Collection<OnmsNode> memberNodes) {
            m_category = category;
            m_nodes = nodes;
            
            for (OnmsNode node : memberNodes) {
                m_nodes.remove(node);
            }
            
            m_sortedMemberNodes =
                new ArrayList<OnmsNode>(memberNodes);
            Collections.sort(m_sortedMemberNodes);
        }

        public OnmsCategory getCategory() {
            return m_category;
        }

        public List<OnmsNode> getNodes() {
            return m_nodes;
        }

        public List<OnmsNode> getSortedMemberNodes() {
            return m_sortedMemberNodes;
        }
        
    }
    
    public class NodeEditModel {
        private OnmsNode m_node;
        private List<OnmsCategory> m_categories;
        private List<OnmsCategory> m_sortedCategories;

        public NodeEditModel(OnmsNode node, List<OnmsCategory> categories) {
            m_node = node;
            m_categories = categories;
            
            for (OnmsCategory category : m_node.getCategories()) {
                m_categories.remove(category);
            }
            
            m_sortedCategories =
                new ArrayList<OnmsCategory>(m_node.getCategories());
            Collections.sort(m_sortedCategories);
        }

        public OnmsNode getNode() {
            return m_node;
        }

        public List<OnmsCategory> getCategories() {
            return m_categories;
        }

        public List<OnmsCategory> getSortedCategories() {
            return m_sortedCategories;
        }
        
    }
}
