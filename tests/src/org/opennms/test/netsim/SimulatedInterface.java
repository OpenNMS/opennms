package org.opennms.test.netsim;

public final class SimulatedInterface
{
    private java.net.InetAddress _addr;
    private SimulatedNode _node;
    private java.util.ArrayList _services = new java.util.ArrayList();

    public SimulatedInterface(java.net.InetAddress addr)
    {
        this._addr = addr;
    }

    protected void setParentNode(SimulatedNode node)
    {
        this._node = node;
    }

    protected SimulatedNode getParentNode()
    {
        return this._node;
    }

    protected String getIpAddress()
    {
        return this._addr.getHostAddress();
    }

    public void addService(SimulatedService newService)
    {
        this._services.add(newService);
        newService.setParentInterface(this);
    }
}
