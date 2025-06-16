/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.snmp;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.snmp.proxy.WalkRequest;
import org.opennms.netmgt.snmp.proxy.WalkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnTracker extends CollectionTracker {
    private static final transient Logger LOG = LoggerFactory.getLogger(ColumnTracker.class);
    
    private SnmpObjId m_base;
    private SnmpObjId m_last;
    private int m_maxRepetitions;
    private int m_maxRetries;
    private Integer m_retries;

    public ColumnTracker(SnmpObjId base) {
        this(null, base);
    }

    public ColumnTracker(SnmpObjId base, int maxRepititions, int maxRetries) {
        this(null, base, maxRepititions, maxRetries);
    }
    
    public ColumnTracker(CollectionTracker parent, SnmpObjId base) {
        this(parent, base, 2, 0);
    }

    public ColumnTracker(CollectionTracker parent, SnmpObjId base, int maxRepititions, int maxRetries) {
        super(parent);
        m_base = base;
        m_last = base;
        m_maxRepetitions = maxRepititions;
        m_maxRetries = maxRetries;
    }

    public SnmpObjId getBase() {
        return m_base;
    }

        @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("base", m_base)
            .append("last oid", m_last)
            .append("max repetitions", m_maxRepetitions)
            .append("finished?", isFinished())
            .toString();
    }

    @Override
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) throws SnmpException {
        if (pduBuilder.getMaxVarsPerPdu() < 1) {
            throw new SnmpException("maxVarsPerPdu < 1");
        }

        LOG.debug("Requesting oid following: {}", m_last);
        pduBuilder.addOid(m_last);
        pduBuilder.setNonRepeaters(0);
        pduBuilder.setMaxRepetitions(getMaxRepetitions());
        
        ResponseProcessor rp = new ResponseProcessor() {
            @Override
            public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
                if (val.isEndOfMib()) {
                    receivedEndOfMib();
                    return;
                }
                LOG.debug("Processing varBind: {} = {}", responseObjId, val);

                // We requested OIDs following the m_last
                // If the response OID is not a successor of m_last, then we have received an invalid response
                // and should stop processing
                // See NMS-10621 for details
                if (!responseObjId.isSuccessorOf(m_last)) {
                    LOG.info("Received varBind: {} = {} after requesting an OID following: {}. "
                            + "The received varBind is not a successor! Marking tracker as finished.",
                            responseObjId, val, m_last);
                    setFinished(true);
                    return;
                }

                m_last = responseObjId;
                if (m_base.isPrefixOf(responseObjId) && !m_base.equals(responseObjId)) {
                    SnmpInstId inst = responseObjId.getInstance(m_base);
                    if (inst != null) {
                        storeResult(new SnmpResult(m_base, inst, val));
                    }
                }
                
                if (!m_base.isPrefixOf(m_last)) {
                    setFinished(true);
                }
                
            }

            @Override
            public boolean processErrors(int errorStatus, int errorIndex) throws SnmpException {
                if (m_retries == null) m_retries = getMaxRetries();
                //LOG.trace("processErrors: errorStatus={}, errorIndex={}, retries={}", errorStatus, errorIndex, m_retries);

                final ErrorStatus status = ErrorStatus.fromStatus(errorStatus);
                if (status == ErrorStatus.TOO_BIG) {
                    throw new SnmpException("Unable to handle tooBigError for next oid request after "+m_last);
                } else if (status == ErrorStatus.GEN_ERR) {
                    reportGenErr("Received genErr requesting next oid after "+m_last+". Marking column is finished.");
                    errorOccurred();
                    return true;
                } else if (status == ErrorStatus.NO_SUCH_NAME) {
                    reportNoSuchNameErr("Received noSuchName requesting next oid after "+m_last+". Marking column is finished.");
                    errorOccurred();
                    return true;
                } else if (status.isFatal()) {
                    final ErrorStatusException ex = new ErrorStatusException(status, "Unexpected error processing next oid after "+m_last+". Aborting!");
                    reportFatalErr(ex);
                    throw ex;
                } else if (status != ErrorStatus.NO_ERROR) {
                    reportNonFatalErr(status);
                }

                if (status.retry()) {
                    if (m_retries-- <= 0) {
                        final ErrorStatusException ex = new ErrorStatusException(status, "Non-fatal error met maximum number of retries. Aborting!");
                        reportFatalErr(ex);
                        throw ex;
                    }
                } else {
                    // On success, reset the retries
                    m_retries = getMaxRetries();
                }

                return status.retry();
            }
        };
        
        return rp;
    }

    public int getMaxRepetitions() {
        return m_maxRepetitions;
    }

    @Override
    public void setMaxRepetitions(int maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
    }

    public int getMaxRetries() {
        return m_maxRetries;
    }
    
    @Override
    public void setMaxRetries(final int maxRetries) {
        LOG.debug("setMaxRetries({})", maxRetries);
        m_maxRetries = maxRetries;
    }

    protected void receivedEndOfMib() {
        setFinished(true);
    }

    protected void errorOccurred() {
        setFinished(true);
    }

    public SnmpInstId getLastInstance() {
        if (m_base.isPrefixOf(m_last) && !m_base.equals(m_last)) {
            return m_last.getInstance(m_base);
        } else {
            return null;
        }
    }

    @Override
    public List<WalkRequest> getWalkRequests() {
        final WalkRequest walkRequest = new WalkRequest(m_base);
        walkRequest.setMaxRepetitions(m_maxRepetitions);
        return Collections.singletonList(walkRequest);
    }

    @Override
    public void handleWalkResponses(List<WalkResponse> responses) {
        // Store the result
        responses.stream()
            .flatMap(res -> res.getResults().stream())
            .filter(res -> {
                SnmpObjId responseOid = SnmpObjId.get(res.getBase(), res.getInstance());
                return m_base.isPrefixOf(responseOid) && !m_base.equals(responseOid);
            })
            .forEach(this::storeResult);
        setFinished(true);
    }
}
