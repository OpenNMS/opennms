package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.CategoryAndMemberNodes;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.NodeEditModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AdminCategoryService {
    public CategoryAndMemberNodes getCategory(String categoryIdString);

    public List<OnmsNode> findAllNodes();
    
    public EditModel findCategoryAndAllNodes(String categoryIdString);

    @Transactional(readOnly = false)
    public void performEdit(String editAction, String editAction2, String[] toAdd, String[] toDelete);

    @Transactional(readOnly = false)
    public OnmsCategory addNewCategory(String name);

    public List<OnmsCategory> findAllCategories();

    @Transactional(readOnly = false)
    public void removeCategory(String categoryIdString);

    public List<OnmsCategory> findByNode(int nodeId);

    public NodeEditModel findNodeCategories(String nodeIdString);

    @Transactional(readOnly = false)
    public void performNodeEdit(String nodeIdString, String editAction, String[] toAdd, String[] toDelete);

}
