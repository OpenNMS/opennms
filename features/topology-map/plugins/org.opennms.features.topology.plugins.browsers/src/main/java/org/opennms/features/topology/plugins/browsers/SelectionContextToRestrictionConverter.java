package org.opennms.features.topology.plugins.browsers;

import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SelectionContextToRestrictionConverter {
    public List<Restriction> getRestrictions(SelectionContext selectionContext) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        restrictions.add(getAnyRestriction(selectionContext));
        return restrictions;
    }

    private AnyRestriction getAnyRestriction(SelectionContext selectionContext) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        for (VertexRef ref : selectionContext.getSelectedVertexRefs()) {
            if ("nodes".equals(ref.getNamespace())) {
                try {
                    restrictions.add(new EqRestriction("node.id", Integer.valueOf(ref.getId())));
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(this.getClass()).warn("Cannot filter nodes with ID: {}", ref.getId());
                }
            }
        }
        return new AnyRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
    }
}
