package org.opennms.web.performance;

import java.util.Set;

public interface GraphResource {
    public String getName();
    public String getLabel();
    public Set<GraphAttribute> getAttributes();
}
