package org.opennms.netmgt.tools.spectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.LogUtils;
import org.springframework.core.io.Resource;

public class AlertMapReader {
    private Resource m_resource;
    private Reader m_reader;
    private StreamTokenizer m_tokenizer;
    
    private static final String oidExpr = "^\\.?(\\d+\\.){2,}\\d+$";
    private static final String eventCodeExpr = "^0[Xx][0-9A-Fa-f]{1,8}$";
    
    /**
     * Alert Code               Event Code  OID Mappings
     * 1.3.6.1.4.1.9.9.13.3.6.5 0x180000    1.3.6.1.4.1.9.9.13.1.5.1.2(1,2) \ 
     *                                      1.3.6.1.4.1.9.9.13.1.5.1.3(3,0)
     */
    
    public AlertMapReader(Resource rsrc) throws IOException {
        m_resource = rsrc;
        m_reader = new BufferedReader(new InputStreamReader(m_resource.getInputStream()));
        m_tokenizer = new StreamTokenizer(m_reader);
        m_tokenizer.resetSyntax();
        m_tokenizer.commentChar('#');
        m_tokenizer.eolIsSignificant(false);
        m_tokenizer.whitespaceChars(' ', ' ');
        m_tokenizer.whitespaceChars('\t', '\t');
        m_tokenizer.whitespaceChars('\n', '\n');
        m_tokenizer.whitespaceChars('\r', '\r');
        m_tokenizer.wordChars('.', '.');
        m_tokenizer.wordChars('0', '9');
        m_tokenizer.wordChars('a', 'z');
        m_tokenizer.wordChars('A', 'Z');
    }
    
    public List<AlertMapping> getAlertMappings() throws IOException {
        List<AlertMapping> alertMappings = new ArrayList<AlertMapping>();
        AlertMapping thisAlertMapping = null;
        OidMapping thisOidMapping = null;
        
        int lastEventCodeLine = -1;
        
        while (m_tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            //System.err.println(m_tokenizer);
            
            if (m_tokenizer.ttype == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches(oidExpr)) {
                String observedOid = m_tokenizer.sval;
                LogUtils.tracef(this, "Found an OID: %s on line %d; what to do with it...", observedOid, m_tokenizer.lineno());
                if (m_tokenizer.nextToken() == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches(eventCodeExpr)) {
                    thisAlertMapping = new AlertMapping(observedOid);
                    thisAlertMapping.setEventCode(m_tokenizer.sval);
                    lastEventCodeLine = m_tokenizer.lineno();
                    LogUtils.debugf(this, "Created a new alert mapping with alert code %s and event code %s (on line %d)", thisAlertMapping.getAlertCode(), thisAlertMapping.getEventCode(), m_tokenizer.lineno());
                    if (m_tokenizer.nextToken() != '(' && m_tokenizer.lineno() > lastEventCodeLine && lastEventCodeLine > -1) {
                        LogUtils.debugf(this, "Alert mapping for alert code %s to event code %s on line %d looks to have no OID mappings, putting it on the completed pile", thisAlertMapping.getAlertCode(), thisAlertMapping.getEventCode(), m_tokenizer.lineno());
                        alertMappings.add(thisAlertMapping);
                    }
                    m_tokenizer.pushBack();
                } else if (m_tokenizer.ttype == '(') {
                    LogUtils.tracef(this, "Peeking ahead I see an open-parenthesis on line %d, opening a new OID mapping for OID %s and pushing back the open-paren", m_tokenizer.lineno(), observedOid);
                    thisOidMapping = new OidMapping(observedOid);
                    m_tokenizer.pushBack();
                } else if (lastEventCodeLine < m_tokenizer.lineno()) {
                    LogUtils.tracef(this, "Found an OID %s on line %d not followed by an open-paren, and last set an event code on line %d, so adding alert mapping for event code %s to the completed pile", observedOid, m_tokenizer.lineno(), lastEventCodeLine, thisAlertMapping.getEventCode());
                    m_tokenizer.pushBack();
                } else {
                    LogUtils.errorf(this, "Uhh, what do I do with token with type %d, string [%s], numeric [%d] on line %d?", m_tokenizer.ttype, m_tokenizer.nval, m_tokenizer.sval);
                    m_tokenizer.pushBack();
                }
            }
            
            else if (m_tokenizer.ttype == '(') {
                if (m_tokenizer.nextToken() == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches("^\\d+$")) {
                    LogUtils.tracef(this, "Encountered an open-paren on line %d; next token is a word %s that looks like a whole number so using it as the event variable number", m_tokenizer.lineno(), m_tokenizer.sval);
                    thisOidMapping.setEventVarNum(Integer.valueOf(m_tokenizer.sval));
                } else {
                    LogUtils.errorf(this, "Encountered what appears to be a malformed OID mapping on line %d of %s while expecting an event variable number", m_tokenizer.lineno(), m_resource);
                    throw new IllegalArgumentException("An apparent OID mapping went wrong while expecting event variable number on line " + m_tokenizer.lineno() + " of " + m_resource);
                }
            }
            
            else if (m_tokenizer.ttype == ',') {
                if (m_tokenizer.nextToken() == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches("^\\d+$")) {
                    LogUtils.tracef(this, "Encountered a comma on line %d; next token is a word %s that looks like a whole number so using it as the index length", m_tokenizer.lineno(), m_tokenizer.sval);
                    thisOidMapping.setIndexLength(Integer.valueOf(m_tokenizer.sval));
                    thisAlertMapping.addOidMapping(thisOidMapping);
                    LogUtils.tracef(this, "Alert-mapping for alert code %s to event code %s now has %d OID-mapping(s)", thisAlertMapping.getAlertCode(), thisAlertMapping.getEventCode(), thisAlertMapping.getOidMappings().size());
                } else {
                    LogUtils.errorf(this, "Encountered what appears to be a malformed OID mapping on line %d of %s while expecting an index length", m_tokenizer.lineno(), m_resource);
                    throw new IllegalArgumentException("An apparent OID mapping went wrong while expecting index length on line " + m_tokenizer.lineno() + " of " + m_resource);
                }
            }
            
            else if (m_tokenizer.ttype == ')') {
                if (m_tokenizer.nextToken() == '\\') {
                    LogUtils.tracef(this, "Found a close-paren followed by a backslash; continuing to process OID mappings for event code %s (on line %d)", thisAlertMapping.getEventCode(), m_tokenizer.lineno());
                } else {
                    LogUtils.debugf(this, "Found a close-paren NOT followed by a backslash; adding alert mapping for event code %s to the completed pile (on line %d)", thisAlertMapping.getEventCode(), m_tokenizer.lineno());
                    alertMappings.add(thisAlertMapping);
                    m_tokenizer.pushBack();
                }
            }
        }
        LogUtils.infof(this, "Loaded %d alert-mappings from [%s]", alertMappings.size(), m_resource);
        return alertMappings;
    }
    
}
