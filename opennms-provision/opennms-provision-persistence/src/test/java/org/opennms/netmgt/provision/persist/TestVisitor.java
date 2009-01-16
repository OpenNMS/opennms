package org.opennms.netmgt.provision.persist;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.modelimport.Asset;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;

public class TestVisitor implements ImportVisitor {
    
    public TestVisitor() {
    }
    
    private final List<Node> m_nodes = new ArrayList<Node>();
    
    public void completeAsset(Asset asset) {
    }

    public void completeCategory(Category category) {
    }

    public void completeInterface(Interface iface) {
    }

    public void completeModelImport(ModelImport modelImport) {
    }

    public void completeMonitoredService(MonitoredService svc) {
    }

    public void completeNode(Node node) {
        m_nodes.add(node);
    }

    public void visitAsset(Asset asset) {
    }

    public void visitCategory(Category category) {
    }

    public void visitInterface(Interface iface) {
    }

    public void visitModelImport(ModelImport mi) {
    }

    public void visitMonitoredService(MonitoredService svc) {
    }

    public void visitNode(Node node) {
    }
    
    public List<Node> getNodes() {
        return m_nodes;
    }
}