package org.opennms.dashboard.client;

public interface Visitor {
    public void visitAll();
    public void visitGroup(SurveillanceGroup group);
    public void visitIntersection(SurveillanceGroup row, SurveillanceGroup column);
}
