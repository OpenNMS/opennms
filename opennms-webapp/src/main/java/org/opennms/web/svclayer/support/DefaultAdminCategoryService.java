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

package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.svclayer.AdminCategoryService;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

/**
 * <p>DefaultAdminCategoryService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultAdminCategoryService implements
        AdminCategoryService {
    
    private CategoryDao m_categoryDao;
    private NodeDao m_nodeDao;
    private EventProxy m_eventProxy;
    
    /**
     * <p>getCategoryDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    /**
     * <p>setCategoryDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao dao) {
        m_categoryDao = dao;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    /**
     * <p>setEventProxy</p>
     *
     * @param eventProxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }

    /** {@inheritDoc} */
    @Override
    public CategoryAndMemberNodes getCategory(String categoryIdString) {
        if (categoryIdString == null) {
            throw new IllegalArgumentException("categoryIdString must not be null");
        }

        OnmsCategory category = findCategory(categoryIdString);
        
        final Collection<OnmsNode> memberNodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : getNodeDao().findByCategory(category)) {
        	if (!"D".equals(node.getType())) {
        		memberNodes.add(node);
        	}
        }
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

    /**
     * <p>findAllNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<OnmsNode> findAllNodes() {
    	final List<OnmsNode> list = new ArrayList<OnmsNode>();
    	for (final OnmsNode node : getNodeDao().findAll()) {
    		if (!"D".equals(node.getType())) {
    			list.add(node);
    		}
    	}
        Collections.sort(list);
        
        return list;
    }
    
    /** {@inheritDoc} */
    @Override
    public EditModel findCategoryAndAllNodes(String categoryIdString) {
        CategoryAndMemberNodes cat = getCategory(categoryIdString); 
        List<OnmsNode> monitoredNodes = findAllNodes();
        return new EditModel(cat.getCategory(), monitoredNodes, cat.getMemberNodes());
    }

    /**
     * <p>performEdit</p>
     *
     * @param categoryIdString a {@link java.lang.String} object.
     * @param editAction a {@link java.lang.String} object.
     * @param toAdd an array of {@link java.lang.String} objects.
     * @param toDelete an array of {@link java.lang.String} objects.
     */
    @Override
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
                OnmsNode node = getNodeDao().get(id);
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
                getNodeDao().save(node);
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
                OnmsNode node = getNodeDao().get(id);
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
                getNodeDao().save(node);
            }
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsCategory addNewCategory(final String name) {
        OnmsCategory category = new OnmsCategory();
        category.setName(name);
        m_categoryDao.save(category);
        return category;
    }

    /** {@inheritDoc} */
    @Override
    public OnmsCategory getCategoryWithName(final String name) {
        return m_categoryDao.findByName(name);
    }

    /**
     * <p>findAllCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<OnmsCategory> findAllCategories() {
        Collection<OnmsCategory> categories = m_categoryDao.findAll();
        List<OnmsCategory> sortedCategories =
            new ArrayList<OnmsCategory>(categories);
        Collections.sort(sortedCategories, new Comparator<OnmsCategory>() {
            @Override
            public int compare(OnmsCategory o1, OnmsCategory o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return sortedCategories;
    }

    /** {@inheritDoc} */
    @Override
    public void removeCategory(String categoryIdString) {
        OnmsCategory category = findCategory(categoryIdString);
        CategoryAndMemberNodes cat = getCategory(categoryIdString);
        for (OnmsNode adriftNode : cat.getMemberNodes()) {
        	notifyCategoryChange(adriftNode);
        }
        m_categoryDao.delete(category);
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsCategory> findByNode(int nodeId) {
        final OnmsNode node = getNodeDao().get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("node with id of " + nodeId + "could not be found");
        }
        
        List<OnmsCategory> categories = new ArrayList<OnmsCategory>(node.getCategories());
        Collections.sort(categories);
        return categories;
    }
    
    /** {@inheritDoc} */
    @Override
    public NodeEditModel findNodeCategories(String nodeIdString) {
        if (nodeIdString == null) {
            throw new IllegalArgumentException("nodeIdString must not be null");
        }

        OnmsNode node = findNode(nodeIdString);
        List<OnmsCategory> categories = findAllCategories();
        
        return new NodeEditModel(node, categories);
    }
    
    /**
     * <p>performNodeEdit</p>
     *
     * @param nodeIdString a {@link java.lang.String} object.
     * @param editAction a {@link java.lang.String} object.
     * @param toAdd an array of {@link java.lang.String} objects.
     * @param toDelete an array of {@link java.lang.String} objects.
     */
    @Override
    public void performNodeEdit(String nodeIdString, String editAction,
            String[] toAdd, String[] toDelete) {
        if (nodeIdString == null) {
            throw new IllegalArgumentException("nodeIdString cannot be null");
        }
        if (editAction == null) {
            throw new IllegalArgumentException("editAction cannot be null");
        }
        
        OnmsNode node = findNode(nodeIdString);
        if (node == null) {
            throw new IllegalArgumentException("No 'node' with id '"
                                               + nodeIdString
                                               + "' could be found");
        }
       
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
                if (category == null) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + " could not be found");
                }
                if (node.getCategories().contains(category)) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + "is already on node "
                                                       + node.getLabel());
                }
                node.getCategories().add(category);
            }
            
            getNodeDao().save(node);
            notifyCategoryChange(node);
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
                if (category == null) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + " could not be found");
                }
                if (!node.getCategories().contains(category)) {
                    throw new IllegalArgumentException("Category with "
                                                       + "id of " + id
                                                       + "is not on node "
                                                       + node.getLabel());
                }
                node.getCategories().remove(category);
            }

            getNodeDao().save(node);
            notifyCategoryChange(node);
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }


    private OnmsNode findNode(String nodeIdString) {
    	final int nodeId;
        
        try {
            nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("parameter 'node' with value '" + nodeIdString + "' could not be parsed as an integer");
        }
        return getNodeDao().get(nodeId);
    }

    private void notifyCategoryChange(final OnmsNode node) {
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI, "CategoryUI");
        bldr.setNode(node);
        bldr.setParam("nodelabel", node.getLabel());
        send(bldr.getEvent());
    }
    
    private void send(final Event e) {
        try {
            m_eventProxy.send(e);
        } catch (final EventProxyException e1) {
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
