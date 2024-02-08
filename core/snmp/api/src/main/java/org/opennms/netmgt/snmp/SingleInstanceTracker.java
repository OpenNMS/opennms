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

public class SingleInstanceTracker extends CollectionTracker {
    private static final transient Logger LOG = LoggerFactory.getLogger(SingleInstanceTracker.class);

    private SnmpObjId m_base;
    private SnmpInstId m_inst;
    private SnmpObjId m_oid;
    private int m_maxRetries;
    private Integer m_retries;
    
    public SingleInstanceTracker(SnmpObjId base, SnmpInstId inst) {
        this(base, inst, null);
    }
    
    public SingleInstanceTracker(String baseOid, String instId) {
        this(SnmpObjId.get(baseOid), new SnmpInstId(instId));
    }

    public SingleInstanceTracker(SnmpObjId base, SnmpInstId inst, CollectionTracker parent) {
        super(parent);
        m_base = base;
        m_inst = inst;
        m_oid = SnmpObjId.get(m_base, m_inst);
    }
    
    @Override
    public void setMaxRepetitions(int maxRepititions) {
        // do nothing since we are not a repeater
    }

    public int getMaxRetries() {
        return m_maxRetries;
    }

    @Override
    public void setMaxRetries(final int maxRetries) {
        m_maxRetries = maxRetries;
    }

    @Override
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) throws SnmpException {
        if (pduBuilder.getMaxVarsPerPdu() < 1) {
            throw new SnmpException("maxVarsPerPdu < 1");
        }
        
        SnmpObjId requestOid = m_oid.decrement();
        LOG.debug("Requesting oid following: {}", requestOid);
        pduBuilder.addOid(requestOid);
        pduBuilder.setNonRepeaters(1);
        pduBuilder.setMaxRepetitions(1);
        
        ResponseProcessor rp = new ResponseProcessor() {

            @Override
            public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
                LOG.debug("Processing varBind: {} = {}", responseObjId, val);
                
                if (val.isEndOfMib()) {
                    receivedEndOfMib();
                }

                if (m_oid.equals(responseObjId)) {
                    storeResult(new SnmpResult(m_base, m_inst, val));
                }
                
                setFinished(true);
            }

            @Override
            public boolean processErrors(int errorStatus, int errorIndex) throws SnmpException {
                if (m_retries == null) m_retries = getMaxRetries();
                //LOG.trace("processErrors: errorStatus={}, errorIndex={}, retries={}", errorStatus, errorIndex, m_retries);

                final ErrorStatus status = ErrorStatus.fromStatus(errorStatus);
                if (status == ErrorStatus.TOO_BIG) {
                    throw new SnmpException("Unable to handle tooBigError for oid request "+m_oid.decrement());
                } else if (status == ErrorStatus.GEN_ERR) {
                    reportGenErr("Received genErr requesting oid "+m_oid.decrement()+". Marking column as finished.");
                    errorOccurred();
                    return true;
                } else if (status == ErrorStatus.NO_SUCH_NAME) {
                    reportNoSuchNameErr("Received noSuchName requesting oid "+m_oid.decrement()+". Marking column as finished.");
                    errorOccurred();
                    return true;
                } else if (status.isFatal()) {
                    final ErrorStatusException ex = new ErrorStatusException(status, "Unexpected error processing oid "+m_oid.decrement()+". Marking column as finished!");
                    LOG.debug("Fatal Error: {}", status, ex);
                    throw ex;
                } else if (status != ErrorStatus.NO_ERROR) {
                    LOG.warn("Non-fatal error encountered: {}. {}", status, status.retry()? "Retrying." : "Giving up.");
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

    protected void errorOccurred() {
        setFinished(true);
    }

    protected void receivedEndOfMib() {
        setFinished(true);
    }

    @Override
    public List<WalkRequest> getWalkRequests() {
        final WalkRequest walkRequest = new WalkRequest(m_base);
        walkRequest.setInstance(m_inst);
        walkRequest.setMaxRepetitions(1);
        return Collections.singletonList(walkRequest);
    }

    @Override
    public void handleWalkResponses(List<WalkResponse> responses) {
        // Store the result
        responses.stream()
            .flatMap(res -> res.getResults().stream())
            .filter(res -> m_oid.equals(SnmpObjId.get(res.getBase(), res.getInstance())))
            .forEach(this::storeResult);
        setFinished(true);
    }
}
