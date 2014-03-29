package org.opennms.netmgt.config.snmp;

import java.util.List;

public interface SnmpConfigVisitor {
    void visitSnmpConfig(SnmpConfig config);
    void visitSnmpConfigFinished();
    
    void visitDefinition(Definition definition);
    void visitDefinitionFinished();
    
    void visitSpecifics(List<String> specifics);
    void visitSpecificsFinished();
    
    void visitRanges(List<Range> ranges);
    void visitRangesFinished();
    
    void visitIpMatches(List<String> ipMatches);
    void visitIpMatchesFinished();
}
