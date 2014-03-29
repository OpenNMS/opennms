package org.opennms.netmgt.config.snmp;

import java.util.List;

public abstract class AbstractSnmpConfigVisitor implements SnmpConfigVisitor {
    @Override public void visitSnmpConfig(SnmpConfig config) {}
    @Override public void visitSnmpConfigFinished() {}

    @Override public void visitDefinition(Definition definition) {}
    @Override public void visitDefinitionFinished() {}
 
    @Override public void visitSpecifics(List<String> specifics) {}
    @Override public void visitSpecificsFinished() {}
    
    @Override public void visitRanges(List<Range> ranges) {}
    @Override public void visitRangesFinished() {}
    
    @Override public void visitIpMatches(List<String> ipMatches) {}
    @Override public void visitIpMatchesFinished() {}
}
