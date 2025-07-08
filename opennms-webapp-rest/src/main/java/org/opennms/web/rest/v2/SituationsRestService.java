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
package org.opennms.web.rest.v2;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Path("situations")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Tag(name = "Situation", description = "Alarms API")
public class SituationsRestService extends AlarmRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SituationsRestService.class);
    public enum Action { ACK, UNACK, ESCALATE, CLEAR, ACCEPT}
    static final String SITUATION_LOG_MSG="situationLogMsg";
    static final String DESCR="situationDescr";
    static final String STATUS="situationStatus";
    static final String ID="situationId";
    static final String RELATED_PREFIX="related-reductionKey";
    static final String CREATED="USER_CREATED";
    static final String REMOVED_ALARM="REMOVED_ALARM";
    static final String ADDED_ALARM="ADDED_ALARM";
    static final String ACCEPTED="ACCEPTED";
    static final String REJECTED="REJECTED";
    static final String SOURCE = "Api";
    private final String IS_SITUATION = "isSituation";

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private AcknowledgmentDao m_ackDao;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@Context UriInfo uriInfo,
                        @Context SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @POST
    @Path("create")
    @Transactional
    public Response create(@Context UriInfo uriInfo, SituationPayload payload) throws InterruptedException {
        String situationId = UUID.randomUUID().toString();
        return handleAssociation(
                payload.getAlarmIdList(),
                payload.getDiagnosticText(),
                payload.getDescription(),
                uriInfo,
                situationId
        );
    }

    @POST
    @Path("associateAlarm")
    @Transactional
    public Response associateAlarm(@Context UriInfo uriInfo,
                                   AlarmAddRemoveRequest request) throws InterruptedException {

        OnmsAlarm situationAlarm = getDao().get(request.getSituationId());
        if (situationAlarm == null || !situationAlarm.isSituation()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid situation ID: " + request.getSituationId())
                    .build();
        }

        Set<OnmsAlarm> toAdd = loadValidAlarms(request.getAlarmIdList(), uriInfo);
        Set<OnmsAlarm> mergedAlarms = new LinkedHashSet<>(situationAlarm.getRelatedAlarms());
        mergedAlarms.addAll(toAdd);
        if (mergedAlarms.equals(situationAlarm.getRelatedAlarms())) {
            return Response.noContent().build();
        }

        String sid = getSituationParamFromAlarm(situationAlarm, ID)
                .orElseGet(() -> {
                    LOG.warn("Could not find situationId on alarm: {}. Using reductionKey.", situationAlarm.getId());
                    return String.valueOf(situationAlarm.getId());
                });

        buildAndSendEvent(mergedAlarms, situationAlarm, sid, ADDED_ALARM, null, null);
        return Response.ok().build();
    }

    @DELETE
    @Path("removeAlarm")
    @Transactional
    public Response removeAlarm(@Context UriInfo uriInfo,
                                AlarmAddRemoveRequest request) {
        OnmsAlarm situationAlarm = getDao().get(request.getSituationId());
        if (situationAlarm == null || !situationAlarm.isSituation()) {
            return Response.noContent().build();
        }

      return removeAlarmsFromSituation(
                situationAlarm,
                request.getAlarmIdList()
        );
    }


    @POST
    @Path("clear")
    @Transactional
    public Response doAction(
            AlarmAddRemoveRequest req,
            @Context SecurityContext secCtx) {

        Integer alarmId = req.getSituationId();
        try {
            writeLock();
            if (alarmId == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Unable to determine alarm ID to update based on query path.").type(MediaType.TEXT_PLAIN).build();
            }
            final OnmsAlarm alarm = getDao().get(alarmId);
            if (alarm == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Unable to locate alarm with ID '" + alarmId + "'").type(MediaType.TEXT_PLAIN).build();
            }

            final String ackUser = secCtx.getUserPrincipal().getName();
            if (StringUtils.isNotBlank(ackUser)) {
                SecurityHelper.assertUserEditCredentials(secCtx, ackUser);
            }
            clearAlarm(alarm,ackUser);
        } finally {
            writeUnlock();
        }

        return Response.ok().build();
    }


    @POST
    @Path("alarms/clear")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Transactional
    public Response removeAndClear(
            AlarmAddRemoveRequest req,
            @Context SecurityContext secCtx,
            @Context UriInfo uriInfo) throws InterruptedException {

        try {
            writeLock();
            OnmsAlarm situationAlarm = getDao().get(req.getSituationId());
            if (situationAlarm == null || !situationAlarm.isSituation()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid situation ID: " + req.getSituationId())
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }

            Response initialResponse = removeAlarmsFromSituation(
                    situationAlarm,
                    req.getAlarmIdList()
            );
            if (initialResponse.getStatus() != 200) {
                return initialResponse;
            }

            String user = secCtx.getUserPrincipal().getName();
            if (StringUtils.isNotBlank(user)) {
                SecurityHelper.assertUserEditCredentials(secCtx, user);
            }
            clearAlarms(req.getAlarmIdList(), user);

            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Path("accepted/{id}")
    public Response acceptSituation(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Situation ID is required")
                    .build();
        }

        OnmsAlarm situation = getDao().get(id);
        if (situation == null || !situation.isSituation()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Situation not found: " + id)
                    .build();
        }

        String currentStatus = getSituationParamFromAlarm(situation, STATUS)
                .orElse("");

        if (ACCEPTED.equals(currentStatus)) {
            LOG.debug("Situation {} already accepted", id);
            return Response
                    .status(Response.Status.NOT_MODIFIED)
                    .entity("Situation " + id + " already accepted")
                    .build();
        }

        String sid = getSituationParamFromAlarm(situation, ID)
                .orElseGet(() -> {
                    LOG.warn("Could not find situationId on alarm: {}. Using reductionKey.", situation.getId());
                    return String.valueOf(situation.getId());
                });

        buildAndSendEvent(
                situation.getRelatedAlarms(),
                situation,
                sid,
                ACCEPTED,
                null,
                null
        );

        return Response.ok().build();
    }

    private void clearAlarm(OnmsAlarm alarm, String user) {
        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
        performAction(ack, Action.CLEAR, Boolean.TRUE);
    }

    private void clearAlarms(List<Integer> alarmIds, String user) {
        for (Integer alarmId : alarmIds) {
            OnmsAlarm alarm = getDao().get(alarmId);
            if (alarm != null) {
                clearAlarm(alarm, user);
            }
        }
    }

    private Response removeAlarmsFromSituation(OnmsAlarm situationAlarm, List<Integer> alarmIdsToRemove) {
        Set<OnmsAlarm> remaining = situationAlarm.getRelatedAlarms().stream()
                .filter(a -> !alarmIdsToRemove.contains(a.getId()))
                .collect(Collectors.toUnmodifiableSet());

        if (situationAlarm.getRelatedAlarms().equals(remaining)) {
            return Response.noContent().build();
        }

        String situationId = getSituationParamFromAlarm(situationAlarm, ID)
                .orElseGet(() -> {
                    LOG.warn("Could not find situationId on alarm {}. Using ID as fallback.", situationAlarm.getId());
                    return Integer.toString(situationAlarm.getId());
                });
        buildAndSendEvent(remaining, situationAlarm, situationId, SituationsRestService.REMOVED_ALARM, null, null);
        return Response.ok().build();
    }

    public void performAction(OnmsAcknowledgment ack, Action action, Boolean value) {
        boolean alarmUpdated = false;
        switch (action) {
            case ACK:
                ack.setAckAction(value
                        ? AckAction.ACKNOWLEDGE
                        : AckAction.UNACKNOWLEDGE);
                alarmUpdated = true;
                break;
            case ESCALATE:
                if (Boolean.TRUE.equals(value)) {
                    ack.setAckAction(AckAction.ESCALATE);
                    alarmUpdated = true;
                }
                break;
            case CLEAR:
                if (Boolean.TRUE.equals(value)) {
                    ack.setAckAction(AckAction.CLEAR);
                    alarmUpdated = true;
                }
                break;
            case ACCEPT:
                break;
        }

        if (alarmUpdated) {
            m_ackDao.processAck(ack);
            m_ackDao.flush();
        }

    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        CriteriaBuilder builder = super.getCriteriaBuilder(uriInfo);
        @SuppressWarnings("unchecked")
        CriteriaBehavior<String> isSituationBehavior =
                (CriteriaBehavior<String>) getCriteriaBehaviors()
                        .get(Aliases.alarm.prop(IS_SITUATION));
        isSituationBehavior.beforeVisit(
                builder,
                "true",
                ConditionType.EQUALS,
                false
        );
        return builder;
    }

    @Override
    protected Map<String, CriteriaBehavior<?>> getCriteriaBehaviors() {
        Map<String, CriteriaBehavior<?>> base = CriteriaBehaviors.ALARM_BEHAVIORS;
        Map<String, CriteriaBehavior<?>> prefixed =
                CriteriaBehaviors.withAliasPrefix(Aliases.alarm, base);
        CriteriaBehavior<?> b = prefixed.get(Aliases.alarm.prop(IS_SITUATION));
        return Collections.singletonMap(Aliases.alarm.prop(IS_SITUATION), b);
    }

    private Response handleAssociation(List<Integer> alarmIds, String diagText, String desctiption, UriInfo uriInfo, String sid) throws InterruptedException {
        Set<OnmsAlarm> alarms = loadValidAlarms(alarmIds, uriInfo);
        if (alarms.size() < 2) {
            return Response.noContent().build();
        }

        buildAndSendEvent(alarms, null, sid, CREATED, diagText, desctiption);
        return Response.ok().build();
    }

    private Set<OnmsAlarm> loadValidAlarms(List<Integer> ids, UriInfo uriInfo) throws InterruptedException {
        Set<OnmsAlarm> alarms = new HashSet<>();
        for (Integer id : ids) {
            OnmsAlarm alarm = getDao().load(id);
            if (alarm != null && alarmIsNotInAnotherSituation(alarm.getReductionKey(), uriInfo)) {
                alarms.add(alarm);
            }
        }
        return alarms;
    }

    private boolean alarmIsNotInAnotherSituation(String reductionKey, UriInfo uriInfo) throws InterruptedException {
        for (OnmsAlarm sit : fetchAllSituationAlarms(uriInfo)) {
            for (OnmsAlarm a : sit.getRelatedAlarms()) {
                if (reductionKey.equals(a.getReductionKey())) {
                    LOG.debug("Alarm {} already in another situation", reductionKey);
                    return false;
                }
            }
        }
        return true;
    }

    protected List<OnmsAlarm> fetchAllSituationAlarms(UriInfo uriInfo) {
        CriteriaBuilder builder = getCriteriaBuilder(uriInfo);
        return getDao().findMatching(builder.toCriteria());
    }

    public static OnmsAlarm getAlarmForDescription(final Collection<OnmsAlarm> alarms) {
        Objects.requireNonNull(alarms, "alarms can not be null");
        if (alarms.isEmpty()) {
            throw new IllegalArgumentException("alarms can not be empty");
        }

        return alarms
                .stream()
                .sorted(Comparator.comparing(OnmsAlarm::getSeverity).thenComparing(x -> x.getLastEventTime().getTime()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("alarms can not be empty"));
    }

    private void buildAndSendEvent(
            Collection<OnmsAlarm> alarms,
            OnmsAlarm situation,
            String sid,
            String status,
            String diagText,
            String desctiption) {

        EventBuilder eb = new EventBuilder()
                .setUei(EventConstants.SITUATION_EVENT_UEI)
                .setSource(SOURCE)
                .setSeverity(maxSeverityLabel(alarms))
                .setTime(new Date())
                .addParam(ID, sid);

        OnmsAlarm desc = null;
        if (!REJECTED.equals(status)) {
            if (CREATED.equals(status)) {
                desc = getAlarmForDescription(alarms);
            } else {
                desc = getAlarmForDescription(situation.getRelatedAlarms());
            }
        }

        String logMsg;
        String descr;
        switch (status) {
            case CREATED:
                logMsg = String.format("Created situation with %d alarms", alarms.size());
                descr = Objects.toString(desctiption, "");
                if (diagText != null) {
                    descr += "<p>Diagnostic: " + diagText + "</p>";
                }
                break;
            case ADDED_ALARM:
                logMsg = String.format("Added alarms to situation %s", sid);
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;

            case REMOVED_ALARM:
                logMsg = String.format("Removed alarms from situation %s", sid);
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;

            case ACCEPTED:
                logMsg = "Situation accepted";
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;
            case REJECTED:
                logMsg = "Situation rejected";
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;
            default:
                logMsg = situation.getLogMsg();
                descr = desctiption;
        }

        if (!logMsg.isBlank()) {
            eb.addParam(SITUATION_LOG_MSG, logMsg);
        }
        if (!descr.isBlank()) {
            eb.addParam(DESCR, descr);
        }

        if (desc != null && desc.getNodeId() != null) {
            eb.setNodeid(desc.getNodeId().longValue());
        }

        AtomicInteger idx = new AtomicInteger(0);
        for (String key : alarms.stream().map(OnmsAlarm::getReductionKey).toList()) {
            eb.addParam(RELATED_PREFIX + idx.incrementAndGet(), key);
        }
        eb.addParam(STATUS, status);
        eventForwarder.sendNow(eb.getEvent());
    }

    public String maxSeverityLabel(Collection<OnmsAlarm> alarmSet) {
        final OnmsSeverity maxSeverity = OnmsSeverity.get(
                alarmSet.stream()
                        .mapToInt(a -> a.getSeverity() != null ? a.getSeverity().getId() : OnmsSeverity.INDETERMINATE.getId())
                        .max()
                        .orElseGet(OnmsSeverity.INDETERMINATE::getId)
        );
        return maxSeverity.getLabel();
    }

    private Optional<String> getSituationParamFromAlarm(OnmsAlarm alarm, String name) {
        final OnmsEvent databaseEvent = alarm.getLastEvent();
        if (databaseEvent == null) {
            return Optional.empty();
        }
        final List<OnmsEventParameter> parms = databaseEvent.getEventParameters().stream().filter((x) -> x.getName().equals(name)).toList();
        if (parms == null) {
            return Optional.empty();
        }

        return parms.stream()
                .map(OnmsEventParameter::getValue)
                .findFirst();
    }
}