/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.tools.spectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class AlertMapReader {

    private static final Logger LOG = LoggerFactory.getLogger(AlertMapReader.class);

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
                LOG.trace("Found an OID: {} on line {}; what to do with it...", observedOid, m_tokenizer.lineno());
                if (m_tokenizer.nextToken() == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches(eventCodeExpr)) {
                    thisAlertMapping = new AlertMapping(observedOid);
                    thisAlertMapping.setEventCode(m_tokenizer.sval);
                    lastEventCodeLine = m_tokenizer.lineno();
					LOG.debug(
							"Created a new alert mapping with alert code {} and event code {} (on line {})",
							thisAlertMapping.getAlertCode(),
							thisAlertMapping.getEventCode(),
							m_tokenizer.lineno());
                    if (m_tokenizer.nextToken() != '(' && m_tokenizer.lineno() > lastEventCodeLine && lastEventCodeLine > -1) {
						LOG.debug(
								"Alert mapping for alert code {} to event code {} on line {} looks to have no OID mappings, putting it on the completed pile",
								thisAlertMapping.getAlertCode(),
								thisAlertMapping.getEventCode(),
								m_tokenizer.lineno());
                        alertMappings.add(thisAlertMapping);
                    }
                    m_tokenizer.pushBack();
                } else if (m_tokenizer.ttype == '(') {
					LOG.trace(
							"Peeking ahead I see an open-parenthesis on line {}, opening a new OID mapping for OID {} and pushing back the open-paren",
							m_tokenizer.lineno(),
							observedOid);
                    thisOidMapping = new OidMapping(observedOid);
                    m_tokenizer.pushBack();
                } else if (lastEventCodeLine < m_tokenizer.lineno()) {
					LOG.trace(
							"Found an OID {} on line {} not followed by an open-paren, and last set an event code on line {}, so adding alert mapping for event code {} to the completed pile",
							observedOid,
							m_tokenizer.lineno(),
							lastEventCodeLine,
							thisAlertMapping.getEventCode());
                    m_tokenizer.pushBack();
                } else {
					LOG.error(
							"Uhh, what do I do with token with type {}, string [{}], numeric [{}] on line {}?",
							m_tokenizer.ttype,
							m_tokenizer.sval,
							m_tokenizer.nval,
							lastEventCodeLine);
                    m_tokenizer.pushBack();
                }
            }
            
            else if (m_tokenizer.ttype == '(') {
                if (m_tokenizer.nextToken() == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches("^\\d+$")) {
					LOG.trace(
							"Encountered an open-paren on line {}; next token is a word {} that looks like a whole number so using it as the event variable number",
							m_tokenizer.lineno(),
							m_tokenizer.sval);
                    thisOidMapping.setEventVarNum(Integer.valueOf(m_tokenizer.sval));
                } else {
					LOG.error(
							"Encountered what appears to be a malformed OID mapping on line {} of {} while expecting an event variable number",
							m_tokenizer.lineno(),
							m_resource);
                    throw new IllegalArgumentException("An apparent OID mapping went wrong while expecting event variable number on line " + m_tokenizer.lineno() + " of " + m_resource);
                }
            }
            
            else if (m_tokenizer.ttype == ',') {
                if (m_tokenizer.nextToken() == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches("^\\d+$")) {
					LOG.trace(
							"Encountered a comma on line {}; next token is a word {} that looks like a whole number so using it as the index length",
							m_tokenizer.lineno(),
							m_tokenizer.sval);
                    thisOidMapping.setIndexLength(Integer.valueOf(m_tokenizer.sval));
                    thisAlertMapping.addOidMapping(thisOidMapping);
					LOG.trace(
							"Alert-mapping for alert code {} to event code {} now has {} OID-mapping(s)",
							thisAlertMapping.getAlertCode(),
							thisAlertMapping.getEventCode(),
							thisAlertMapping.getOidMappings().size());
                } else {
					LOG.error(
							"Encountered what appears to be a malformed OID mapping on line {} of {} while expecting an index length",
							m_tokenizer.lineno(),
							m_resource);
                    throw new IllegalArgumentException("An apparent OID mapping went wrong while expecting index length on line " + m_tokenizer.lineno() + " of " + m_resource);
                }
            }
            
            else if (m_tokenizer.ttype == ')') {
                if (m_tokenizer.nextToken() == '\\') {
					LOG.trace(
							"Found a close-paren followed by a backslash; continuing to process OID mappings for event code {} (on line {})",
							thisAlertMapping.getEventCode(),
							m_tokenizer.lineno());
                } else {
					LOG.debug(
							"Found a close-paren NOT followed by a backslash; adding alert mapping for event code {} to the completed pile (on line {})",
							thisAlertMapping.getEventCode(),
							m_tokenizer.lineno());
                    alertMappings.add(thisAlertMapping);
                    m_tokenizer.pushBack();
                }
            }
        }
        LOG.info("Loaded {} alert-mappings from [{}]", alertMappings.size(), m_resource);
        return Collections.unmodifiableList(alertMappings);
    }
    
}
