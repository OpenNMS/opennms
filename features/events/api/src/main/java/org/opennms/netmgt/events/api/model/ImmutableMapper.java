/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.events.api.model;

import org.opennms.core.utils.ImmutableCollections;
import org.opennms.netmgt.xml.event.*;

import java.util.stream.Collectors;

/**
 * A mapper from mutable to immutable types for Event (and related objects).
 */
public class ImmutableMapper {

    public static ImmutableAlarmData fromMutableAlarmData(AlarmData alarmData) {
        if (alarmData == null) {
            return null;
        }

        return ImmutableAlarmData.newBuilder()
                .setReductionKey(alarmData.getReductionKey())
                .setAlarmType(alarmData.hasAlarmType() ? alarmData.getAlarmType() : null)
                .setClearKey(alarmData.getClearKey())
                .setAutoClean(alarmData.hasAutoClean() ? alarmData.getAutoClean() : null)
                .setX733AlarmType(alarmData.getX733AlarmType())
                .setX733ProbableCause(alarmData.hasX733ProbableCause() ? alarmData.getX733ProbableCause() : null)
                .setUpdateFieldList(
                        alarmData.getUpdateFieldList()
                                .stream().map(ImmutableMapper::fromMutableUpdateField)
                                .collect(Collectors.toList()))
                .setManagedObject(fromMutableManagedObject(alarmData.getManagedObject()))
                .build();
    }

    public static ImmutableAutoAcknowledge fromMutableAutoAcknowledge(Autoacknowledge autoacknowledge) {
        if (autoacknowledge == null) {
            return null;
        }

        return ImmutableAutoAcknowledge.newBuilder()
                .setContent(autoacknowledge.getContent())
                .setState(autoacknowledge.getState())
                .build();
    }

    public static ImmutableAutoAction fromMutableAutoAction(Autoaction autoaction) {
        if (autoaction == null) {
            return null;
        }

        return ImmutableAutoAction.newBuilder()
                .setContent(autoaction.getContent())
                .setState(autoaction.getState())
                .build();
    }

    public static ImmutableCorrelation fromMutableCorrelation(Correlation correlation) {
        if (correlation == null) {
            return null;
        }

        return ImmutableCorrelation.newBuilder()
                .setState(correlation.getState())
                .setPath(correlation.getPath())
                .setCueiList(ImmutableCollections.newListOfImmutableType(
                        correlation.getCueiCollection()))
                .setCmin(correlation.getCmin())
                .setCmax(correlation.getCmax())
                .setCtime(correlation.getCtime())
                .build();
    }

    public static ImmutableEvent fromMutableEvent(Event event) {
        if (event == null) {
            return null;
        }

        return ImmutableEvent.newBuilder()
                .setUuid(event.getUuid())
                .setDbId(event.hasDbid() ? event.getDbid() : null)
                .setDistPoller(event.getDistPoller())
                .setCreationTime(event.getCreationTime())
                .setMasterStation(event.getMasterStation())
                .setMask(ImmutableMapper.fromMutableMask(event.getMask()))
                .setUei(event.getUei())
                .setSource(event.getSource())
                .setNodeid(event.hasNodeid() ? event.getNodeid() : null)
                .setTime(event.getTime())
                .setHost(event.getHost())
                .setInterface(event.getInterface())
                .setInterfaceAddress(event.getInterfaceAddress())
                .setSnmpHost(event.getSnmphost())
                .setService(event.getService())
                .setSnmp(ImmutableMapper.fromMutableSnmp(event.getSnmp()))
                .setParms(ImmutableCollections.newListOfImmutableType(
                        event.getParmCollection().stream().map(
                                ImmutableMapper::fromMutableParm).collect(Collectors.toList())))
                .setDescr(event.getDescr())
                .setLogMsg(ImmutableMapper.fromMutableLogMsg(event.getLogmsg()))
                .setSeverity(event.getSeverity())
                .setPathOutage(event.getPathoutage())
                .setCorrelation(ImmutableMapper.fromMutableCorrelation(event.getCorrelation()))
                .setOperInstruct(event.getOperinstruct())
                .setAutoActionList(ImmutableCollections.newListOfImmutableType(
                        event.getAutoactionCollection().stream().map(
                                ImmutableMapper::fromMutableAutoAction).collect(Collectors.toList())))
                .setOperActionList(ImmutableCollections.newListOfImmutableType(
                        event.getOperactionCollection().stream().map(
                                ImmutableMapper::fromMutableOperAction).collect(Collectors.toList())))
                .setAutoAcknowledge(ImmutableMapper.fromMutableAutoAcknowledge(event.getAutoacknowledge()))
                .setLogGroupList(ImmutableCollections.newListOfImmutableType(event.getLoggroupCollection()))
                .settTicket(ImmutableMapper.fromMutableTticket(event.getTticket()))
                .setForwardList(ImmutableCollections.newListOfImmutableType(
                        event.getForwardCollection().stream().map(
                                ImmutableMapper::fromMutableForward).collect(Collectors.toList())))
                .setScriptList(ImmutableCollections.newListOfImmutableType(
                        event.getScriptCollection().stream().map(
                                ImmutableMapper::fromMutableScript).collect(Collectors.toList())))
                .setIfIndex(event.hasIfIndex() ? event.getIfIndex() : null)
                .setIfAlias(event.getIfAlias())
                .setMouseOverText(event.getMouseovertext())
                .setAlarmData(ImmutableMapper.fromMutableAlarmData(event.getAlarmData()))
                .build();
    }

    public static ImmutableForward fromMutableForward(Forward forward) {
        if (forward == null) {
            return null;
        }
        return ImmutableForward.newBuilder()
                .setContent(forward.getContent())
                .setMechanism(forward.getMechanism())
                .setState(forward.getState())
                .build();
    }

    public static ImmutableLogMsg fromMutableLogMsg(Logmsg logMsg) {
        if (logMsg == null) {
            return null;
        }

        return ImmutableLogMsg.newBuilder()
                .setContent(logMsg.getContent())
                .setDest(logMsg.getDest())
                .setNotify(logMsg.hasNotify() ? logMsg.getNotify() : null)
                .build();
    }

    public static ImmutableManagedObject fromMutableManagedObject(ManagedObject managedObject) {
        if (managedObject == null) {
            return null;
        }

        return ImmutableManagedObject.newBuilder()
                .setType(managedObject.getType())
                .build();
    }

    public static ImmutableMaskElement fromMutableMaskElement(Maskelement maskElement) {
        if (maskElement == null) {
            return null;
        }

        return ImmutableMaskElement.newBuilder()
                .setMeName(maskElement.getMename())
                .setMeValues(maskElement.getMevalueCollection())
                .build();
    }

    public static ImmutableMask fromMutableMask(Mask mask) {
        if (mask == null) {
            return null;
        }

        return ImmutableMask.newBuilder()
                .setMaskElements(mask.getMaskelementCollection().stream().map(
                        ImmutableMapper::fromMutableMaskElement).collect(Collectors.toList()))
                .build();
    }

    public static ImmutableOperAction fromMutableOperAction(Operaction operaction) {
        if (operaction == null) {
            return null;
        }

        return ImmutableOperAction.newBuilder()
                .setContent(operaction.getContent())
                .setState(operaction.getState())
                .setMenutext(operaction.getMenutext())
                .build();
    }

    public static ImmutableParm fromMutableParm(Parm parm) {
        if (parm == null) {
            return null;
        }

        return ImmutableParm.newBuilder()
                .setParmName(parm.getParmName())
                .setValue(ImmutableMapper.fromMutableValue(parm.getValue()))
                .build();
    }

    public static ImmutableScript fromMutableScript(Script script) {
        if (script == null) {
            return null;
        }

        return ImmutableScript.newBuilder()
                .setContent(script.getContent())
                .setLanguage(script.getLanguage())
                .build();
    }

    public static ImmutableSnmp fromMutableSnmp(Snmp snmp) {
        if (snmp == null) {
            return null;
        }

        return ImmutableSnmp.newBuilder()
                .setId(snmp.getId())
                .setTrapOID(snmp.getTrapOID())
                .setIdText(snmp.getIdtext())
                .setVersion(snmp.getVersion())
                .setSpecific(snmp.hasSpecific() ? snmp.getSpecific() : null)
                .setGeneric(snmp.hasGeneric() ? snmp.getGeneric() : null)
                .setCommunity(snmp.getCommunity())
                .setTimeStamp(snmp.hasTimeStamp() ? snmp.getTimeStamp() : null)
                .build();
    }

    public static ImmutableTticket fromMutableTticket(Tticket tticket) {
        if (tticket == null) {
            return null;
        }

        return ImmutableTticket.newBuilder()
                .setContent(tticket.getContent())
                .setState(tticket.getState())
                .build();
    }

    public static ImmutableUpdateField fromMutableUpdateField(UpdateField updateField) {
        if (updateField == null) {
            return null;
        }

        return ImmutableUpdateField.newBuilder()
                .setFieldName(updateField.getFieldName())
                .setUpdateOnReduction(updateField.isUpdateOnReduction())
                .setValueExpression(updateField.getValueExpression())
                .build();
    }

    public static ImmutableValue fromMutableValue(Value value) {
        if (value == null) {
            return null;
        }

        return ImmutableValue.newBuilder()
                .setContent(value.getContent())
                .setType(value.getType())
                .setEncoding(value.getEncoding())
                .setExpand(value.isExpand())
                .build();
    }
}
