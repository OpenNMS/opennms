package org.opennms.netmgt.bsm.persistence.api;

import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;

public interface Edge {

    boolean isEnabled();

    int getWeight();

    Set<String> getReductionKeys();

    AbstractMapFunctionEntity getMapFunction();

}
