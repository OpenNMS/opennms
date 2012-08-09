package org.opennms.web.controller.nrt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 08.08.12
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class NrtUIMetricMappingHelper {

    private String m_oid;

    private HashMap<String, HashSet<String>> m_consolidationFunction = new HashMap<String, HashSet<String>>();

    public NrtUIMetricMappingHelper(String oid) {
        this.m_oid = oid;
    }

    public void addTarget(String consolidationFunction, String target) {
        getConsolidationFunction(consolidationFunction).add(target);
    }

    private HashSet<String> getConsolidationFunction(String consolidationFunction) {
        if (!m_consolidationFunction.containsKey(consolidationFunction)) {
            m_consolidationFunction.put(consolidationFunction, new HashSet<String>());
        }
        return m_consolidationFunction.get(consolidationFunction);
    }

    public Set<String> getConsolidationFunctions() {
        return m_consolidationFunction.keySet();
    }

    public Set<String> getTargetsForConsolidationFunction(String consolidationFunction) {
        HashSet<String> targets = new HashSet<String>();
        for (String target : m_consolidationFunction.get(consolidationFunction)) {
            targets.add(target);
        }
        return targets;

    }
}
