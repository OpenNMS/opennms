package org.opennms.test.netsim;

public final class SimulatedService
{
    private SimulatedInterface _interface;
    private String _servicename = "";
    private boolean _isUp = true;
    private String _remoteScript = "";


    public SimulatedService(String name, String remoteScript)
    {
        this._servicename = name;
        this._remoteScript = remoteScript;
    }

    protected void setParentInterface(SimulatedInterface iface)
    {
        this._interface = iface;
    }

    protected SimulatedInterface getParentInterface()
    {
        return this._interface;
    }

    public String getName()
    {
        return this._servicename;
    }

    public void start()
    {
        SimulatorController cont = this.getParentInterface().getParentNode().getParentController();

        String command = this._remoteScript 
                       + " start " 
                       + cont.getLocalInetAddress().getHostAddress()
                       + " " 
                       + this.getParentInterface().getIpAddress();

        cont.sendCommand(command);
        this._isUp = true;
    }

    public void stop()
    {
        SimulatorController cont = this.getParentInterface().getParentNode().getParentController();

        String command = this._remoteScript 
                       + " stop " 
                       + cont.getLocalInetAddress().getHostAddress()
                       + " " 
                       + this.getParentInterface().getIpAddress();

        cont.sendCommand(command);
        this._isUp = false;
    }

    public boolean isUp()
    {
        return this._isUp;
    }

    public boolean isDown()
    {
        return !(this.isUp());
    }
}
