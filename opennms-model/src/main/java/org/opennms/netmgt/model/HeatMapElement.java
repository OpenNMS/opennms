package org.opennms.netmgt.model;

public class HeatMapElement {
    private String name;
    private int id;
    private int nodesUp;
    private int nodesTotal;
    private int servicesDown;
    private int servicesTotal;

    public HeatMapElement(String name, Number id, Number servicesDown, Number servicesTotal, Number nodesUp, Number nodesTotal) {
        this.name = name;
        this.id = id == null ? 0 : id.intValue();
        this.servicesDown = servicesDown == null ? 0 : servicesDown.intValue();
        this.servicesTotal = servicesTotal == null ? 0 : servicesTotal.intValue();
        this.nodesUp = nodesUp == null ? 0 : nodesUp.intValue();
        this.nodesTotal = nodesTotal == null ? 0 : nodesTotal.intValue();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getNodesUp() {
        return nodesUp;
    }

    public int getNodesDown() {
        return getNodesTotal() - getNodesUp();
    }

    public int getNodesTotal() {
        return nodesTotal;
    }

    public int getServicesUp() {
        return getServicesTotal() - getServicesDown();
    }

    public int getServicesDown() {
        return servicesDown;
    }

    public int getServicesTotal() {
        return servicesTotal;
    }
}
