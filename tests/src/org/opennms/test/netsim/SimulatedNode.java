package org.opennms.test.netsim;

public final class SimulatedNode
{
    private String _nodeName = "";
    private SimulatorController _controller;
    private java.util.ArrayList _interfaces = new java.util.ArrayList();
    
    public SimulatedNode(String name)
    {
    }

    public void addInterface(SimulatedInterface newInt)
    {
        this._interfaces.add(newInt);
        newInt.setParentNode(this);
    }

    protected void setParentController(SimulatorController cont)
    {
        this._controller = cont;
    }

    protected SimulatorController getParentController()
    {
        return this._controller;
    }
}
