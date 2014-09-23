package org.opennms.netmgt.provision.service;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.persist.AbstractRequisitionVisitor;
import org.opennms.netmgt.provision.persist.OnmsNodeCategoryRequisition;
import org.opennms.netmgt.provision.persist.RequisitionVisitor;

public class CategoryAssociationVisitor extends AbstractRequisitionVisitor implements RequisitionVisitor {
    private List<String> m_categories = new ArrayList<>();

    @Override
    public void visitNodeCategory(final OnmsNodeCategoryRequisition catReq) {
        m_categories.add(catReq.getName());
    }

    public List<String> getCategories() {
        return m_categories;
    }
}
