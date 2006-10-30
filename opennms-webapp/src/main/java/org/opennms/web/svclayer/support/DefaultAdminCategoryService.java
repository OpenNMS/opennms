package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.AdminCategoryService;

public class DefaultAdminCategoryService implements
        AdminCategoryService {
    
    private CategoryDao m_categoryDao;
    private NodeDao m_nodeDao;
    
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

    public OnmsCategory getCategory(String categoryIdString) {
        if (categoryIdString == null) {
            throw new IllegalArgumentException("categoryIdString must not be null");
        }

        int categoryId = -1;
        try {
            categoryId = Integer.parseInt(categoryIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parameter 'categoryid' "
                                               + "with value '"
                                               + categoryIdString
                                               + "' could not be parsed "
                                               + "as an integer");
        }

        OnmsCategory category = m_categoryDao.get(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Could not find category "
                                               + "with category ID "
                                               + categoryId);
        }
        m_categoryDao.initialize(category.getMemberNodes());
        // XXX does anything need to be initialized in each member node?
        return category;
    }

    public List<OnmsNode> findAllNodes() {
        List<OnmsNode> list =
            new ArrayList<OnmsNode>(m_nodeDao.findAll());
        Collections.sort(list);
        
        return list;
    }
    
    public EditModel findCategoryAndAllNodes(String categoryIdString) {
        OnmsCategory category = getCategory(categoryIdString);
        List<OnmsNode> monitoredNodes = findAllNodes();
        return new EditModel(category, monitoredNodes);
    }

    public void performEdit(String categoryIdString, String editAction,
            String[] toAdd, String[] toDelete) {
        if (categoryIdString == null) {
            throw new IllegalArgumentException("categoryIdString cannot be null");
        }
        if (editAction == null) {
            throw new IllegalArgumentException("editAction cannot be null");
        }
        
        OnmsCategory category = getCategory(categoryIdString);
       
        if (editAction.equals("Add")) {
            if (toAdd == null) {
                return;
                //throw new IllegalArgumentException("toAdd cannot be null if editAction is 'Add'");
            }
           
            for (String idString : toAdd) {
                Integer id;
                try {
                    id = Integer.parseInt(idString);
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
                if (category.getMemberNodes().contains(node)) {
                    throw new IllegalArgumentException("node with "
                                                       + "id of " + id
                                                       + "is already a member of "
                                                       + "category "
                                                       + category.getName());
                }
                category.getMemberNodes().add(node);
            }
            
            m_categoryDao.save(category);
       } else if (editAction.equals("Remove")) {
            if (toDelete == null) {
                return;
                //throw new IllegalArgumentException("toDelete cannot be null if editAction is 'Remove'");
            }
            
            for (String idString : toDelete) {
                Integer id;
                try {
                    id = Integer.parseInt(idString);
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
                if (!category.getMemberNodes().contains(node)) {
                    throw new IllegalArgumentException("Node with "
                                                       + "id of " + id
                                                       + "is not a member of "
                                                       + "category "
                                                       + category.getName());
                }
                category.getMemberNodes().remove(node);
            }

            m_categoryDao.save(category);
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }

    public OnmsCategory addNewCategory(String name) {
        OnmsCategory category = new OnmsCategory();
        category.setName(name);
        m_categoryDao.save(category);
        return category;
    }

    public List<OnmsCategory> findAllCategories() {
        Collection<OnmsCategory> categories = m_categoryDao.findAll();
        List<OnmsCategory> sortedCategories =
            new ArrayList<OnmsCategory>(categories);
        Collections.sort(sortedCategories);

        return sortedCategories;
    }

    public void removeCategory(String categoryIdString) {
        OnmsCategory category = getCategory(categoryIdString);
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
       
        if (editAction.equals("Add")) {
            if (toAdd == null) {
                return;
                //throw new IllegalArgumentException("toAdd cannot be null if editAction is 'Add'");
            }
           
            for (String idString : toAdd) {
                Integer id;
                try {
                    id = Integer.parseInt(idString);
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
       } else if (editAction.equals("Remove")) {
            if (toDelete == null) {
                return;
                //throw new IllegalArgumentException("toDelete cannot be null if editAction is 'Remove'");
            }
            
            for (String idString : toDelete) {
                Integer id;
                try {
                    id = Integer.parseInt(idString);
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
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }


    private OnmsNode findNode(String nodeIdString) {
        int nodeId;
        
        try {
            nodeId = Integer.parseInt(nodeIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parameter 'node' "
                                               + "with value '"
                                               + nodeIdString
                                               + "' could not be parsed "
                                               + "as an integer");
        }
        return m_nodeDao.get(nodeId);
    }

    public class EditModel {
        private OnmsCategory m_category;
        private List<OnmsNode> m_nodes;
        private List<OnmsNode> m_sortedMemberNodes;

        public EditModel(OnmsCategory category, List<OnmsNode> nodes) {
            m_category = category;
            m_nodes = nodes;
            
            for (OnmsNode node : m_category.getMemberNodes()) {
                m_nodes.remove(node);
            }
            
            m_sortedMemberNodes =
                new ArrayList<OnmsNode>(m_category.getMemberNodes());
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
