package org.opennms.netmgt.model;

public class HeatMapElement {
    public enum Type {
        Outage,
        Alarm
    }

    private String name;
    private int id;
    private int nodesUp;
    private int nodesTotal;
    private int servicesDown;
    private int servicesTotal;
    private int maximumSeverity;
    private Type type;

    public HeatMapElement(String name, Number id, Number servicesDown, Number servicesTotal, Number nodesUp, Number nodesTotal) {
        this.name = name == null ? "Uncategorized" : name;
        this.id = id == null ? -1 : id.intValue();
        this.servicesDown = servicesDown == null ? 0 : servicesDown.intValue();
        this.servicesTotal = servicesTotal == null ? 0 : servicesTotal.intValue();
        this.nodesUp = nodesUp == null ? 0 : nodesUp.intValue();
        this.nodesTotal = nodesTotal == null ? 0 : nodesTotal.intValue();
        this.type = Type.Outage;
    }

    public HeatMapElement(String name, Number id, Number servicesTotal, Number nodesTotal, Number maximumSeverity) {
        this.name = name == null ? "Uncategorized" : name;
        this.id = id == null ? -1 : id.intValue();
        this.servicesTotal = servicesTotal == null ? 0 : servicesTotal.intValue();
        this.nodesTotal = nodesTotal == null ? 0 : nodesTotal.intValue();
        this.maximumSeverity = maximumSeverity == null ? 0 : maximumSeverity.intValue();
        this.type = Type.Alarm;
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

    public int getMaximumSeverity() {
        return maximumSeverity;
    }

    public Type getType() {
        return type;
    }

    public double getColor() {
        if (type == Type.Alarm) {
            switch (OnmsSeverity.get(getMaximumSeverity())) {
                case WARNING: {
                    return 0.1;
                }
                case MINOR: {
                    return 0.2;
                }
                case MAJOR: {
                    return 0.4;
                }
                case CRITICAL: {
                    return 1.0;
                }
                default: {
                    return 0.0;
                }
            }
        } else {
            if (getServicesTotal() == 0) {
                return 0.0;
            } else {
                return (double) getServicesDown() / (double) getServicesTotal();
            }
        }
    }
}
