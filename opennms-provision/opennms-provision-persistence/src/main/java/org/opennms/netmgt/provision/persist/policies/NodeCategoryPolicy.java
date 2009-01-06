package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;

public class NodeCategoryPolicy extends BasePolicy<OnmsNode> {
    public OnmsNode apply(OnmsNode node) {
        String category = getParameter("category");
        if (category == null) {
            return node;
        }
        
        if (getParameter("type") != null) {
            if (!match(node.getType(), getParameter("type"))) {
                return null;
            }
        }
        if (getParameter("sysobjectid") != null) {
            if (!match(node.getSysObjectId(), getParameter("sysobjectid"))) {
                return null;
            }
        }
        if (getParameter("sysname") != null) {
            if (!match(node.getSysName(), getParameter("sysname"))) {
                return null;
            }
        }
        if (getParameter("sysdescription") != null) {
            if (!match(node.getSysDescription(), getParameter("sysdescription"))) {
                return null;
            }
        }
        if (getParameter("syslocation") != null) {
            if (!match(node.getSysLocation(), getParameter("syslocation"))) {
                return null;
            }
        }
        if (getParameter("syscontact") != null) {
            if (!match(node.getSysContact(), getParameter("syscontact"))) {
                return null;
            }
        }
        if (getParameter("label") != null) {
            if (!match(node.getLabel(), getParameter("label"))) {
                return null;
            }
        }
        if (getParameter("labelsource") != null) {
            if (!match(node.getLabelSource(), getParameter("labelsource"))) {
                return null;
            }
        }
        if (getParameter("netbiosname") != null) {
            if (!match(node.getNetBiosName(), getParameter("netbiosname"))) {
                return null;
            }
        }
        if (getParameter("netbiosdomain") != null) {
            if (!match(node.getNetBiosDomain(), getParameter("netbiosdomain"))) {
                return null;
            }
        }
        if (getParameter("operatingsystem") != null) {
            if (!match(node.getOperatingSystem(), getParameter("operatingsystem"))) {
                return null;
            }
        }
        if (getParameter("foreignid") != null) {
            if (!match(node.getForeignId(), getParameter("foreignid"))) {
                return null;
            }
        }
        if (getParameter("foreignsource") != null) {
            if (!match(node.getForeignSource(), getParameter("foreignsource"))) {
                return null;
            }
        }
        if (getParameter("foreignsource") != null) {
            if (!match(node.getForeignSource(), getParameter("foreignsource"))) {
                return null;
            }
        }

        node.addCategory(new OnmsCategory(category));

        return node;
    }
}
