package org.opennms.netmgt.bsm.mapreduce.api;

import java.util.Set;

public interface Edge {

    boolean isEnabled();

    int getWeight();

    Set<String> getReductionKeys();

    MapFunction getMapFunction();

}
