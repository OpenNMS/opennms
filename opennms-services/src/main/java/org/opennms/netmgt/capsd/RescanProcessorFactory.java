package org.opennms.netmgt.capsd;

public interface RescanProcessorFactory {

    public abstract RescanProcessor createRescanProcessor(int nodeId);

    public abstract RescanProcessor createForcedRescanProcessor(int nodeId);

}