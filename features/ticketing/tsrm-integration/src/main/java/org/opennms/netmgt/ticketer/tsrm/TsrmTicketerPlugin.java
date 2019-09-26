/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.tsrm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import static org.opennms.netmgt.ticketer.tsrm.TsrmConstants.*;
import javax.net.ssl.TrustManager;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.cxf.common.util.CollectionUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.utils.AnyServerX509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.maximo.CreateSHSIMPINCResponseType;
import com.ibm.maximo.CreateSHSIMPINCType;
import com.ibm.maximo.INCIDENTKeyType;
import com.ibm.maximo.MXBooleanType;
import com.ibm.maximo.MXDateTimeType;
import com.ibm.maximo.MXStringQueryType;
import com.ibm.maximo.MXStringType;
import com.ibm.maximo.QuerySHSIMPINCResponseType;
import com.ibm.maximo.QuerySHSIMPINCType;
import com.ibm.maximo.SHSIMPINCINCIDENTType;
import com.ibm.maximo.SHSIMPINCQueryType;
import com.ibm.maximo.SHSIMPINCSetType;
import com.ibm.maximo.UpdateSHSIMPINCType;
import com.ibm.maximo.wsdl.shsimpinc.SHSIMPINC;
import com.ibm.maximo.wsdl.shsimpinc.SHSIMPINCPortType;

public class TsrmTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(TsrmTicketerPlugin.class);
    SHSIMPINCPortType port;

    public TsrmTicketerPlugin() {
        getService();
    }

    private SHSIMPINCPortType getService() {
        final SHSIMPINC service = new SHSIMPINC();
        port = service.getSHSIMPINCSOAP12Port();

        final Client cxfClient = ClientProxy.getClient(port);

        try {
            cxfClient.getRequestContext().put(Message.ENDPOINT_ADDRESS,
                                              getProperties().getProperty("tsrm.url"));
            final HTTPConduit http = (HTTPConduit) cxfClient.getConduit();
            String stictSSL = getProperties().getProperty("tsrm.ssl.strict");

            if (!Boolean.parseBoolean(stictSSL)) {

                LOG.debug("Disabling strict SSL checking.");
                // Accept all certificates
                final TrustManager[] simpleTrustManager = new TrustManager[] { new AnyServerX509TrustManager() };
                final TLSClientParameters tlsParams = new TLSClientParameters();
                tlsParams.setTrustManagers(simpleTrustManager);
                tlsParams.setDisableCNCheck(true);
                http.setTlsClientParameters(tlsParams);
            }
        } catch (IOException e) {
            LOG.error("Unable to load tsrm properties ", e);
        }

        // Log incoming and outgoing requests
        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
        loggingInInterceptor.setPrettyLogging(true);
        cxfClient.getInInterceptors().add(loggingInInterceptor);

        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        loggingOutInterceptor.setPrettyLogging(true);
        cxfClient.getOutInterceptors().add(loggingOutInterceptor);

        return port;
    }

    @Override
    public Ticket get(String ticketId) throws PluginException {

        SHSIMPINCQueryType queryType = new SHSIMPINCQueryType();
        SHSIMPINCQueryType.INCIDENT incidentQuery = new SHSIMPINCQueryType.INCIDENT();
        List<MXStringQueryType> ticketList = incidentQuery.getTICKETID();
        MXStringQueryType ticketQuery = new MXStringQueryType();
        ticketQuery.setValue(ticketId);
        ticketList.add(ticketQuery);
        queryType.setINCIDENT(incidentQuery);
        QuerySHSIMPINCType queryIncident = new QuerySHSIMPINCType();
        queryIncident.setSHSIMPINCQuery(queryType);

        QuerySHSIMPINCResponseType response = port.querySHSIMPINC(queryIncident);

        if (!CollectionUtils.isEmpty(response.getSHSIMPINCSet().getINCIDENT())) {

            // Response will only have one element in the list
            SHSIMPINCINCIDENTType incident = response.getSHSIMPINCSet().getINCIDENT().get(0);

            if (incident != null) {

                Ticket ticket = new Ticket();

                MXStringType affectedPerson = new MXStringType();
                affectedPerson = incident.getAFFECTEDPERSON();
                MXStringType assetNum = new MXStringType();
                assetNum = incident.getASSETNUM();
                MXStringType classId = new MXStringType();
                classId = incident.getCLASS();
                MXStringType classStructureId = new MXStringType();
                classStructureId = incident.getCLASSSTRUCTUREID();
                MXStringType description = new MXStringType();
                description = incident.getDESCRIPTION();
                MXStringType longDescription = new MXStringType();
                longDescription = incident.getDESCRIPTIONLONGDESCRIPTION();
                MXStringType location = new MXStringType();
                location = incident.getLOCATION();
                MXStringType ownerGroup = new MXStringType();
                ownerGroup = incident.getOWNERGROUP();
                MXStringType reportedBy = new MXStringType();
                reportedBy = incident.getREPORTEDBY();
                MXStringType shsCallerType = new MXStringType();
                shsCallerType = incident.getSHSCALLERTYPE();
                MXStringType shsReasonForOutage = new MXStringType();
                shsReasonForOutage = incident.getSHSREASONFOROUTAGE();
                MXStringType shsResolution = new MXStringType();
                shsResolution = incident.getSHSRESOLUTION();
                MXStringType shsRoomNumber = new MXStringType();
                shsRoomNumber = incident.getSHSROOMNUMBER();
                MXStringType siteId = new MXStringType();
                siteId = incident.getSITEID();
                MXStringType source = new MXStringType();
                source = incident.getSOURCE();
                MXBooleanType statusIface = new MXBooleanType();
                statusIface = incident.getSTATUSIFACE();
                MXStringType ticketIdFromIncident = new MXStringType();
                ticketIdFromIncident = incident.getTICKETID();

                if (ticketIdFromIncident == null) {
                    return null;
                }

                ticket.setId(ticketIdFromIncident.getValue());

                if (affectedPerson != null) {
                    ticket.addAttribute(AFFECTED_PERSON,
                                        affectedPerson.getValue());
                }
                if (assetNum != null) {
                    ticket.addAttribute(ASSET_NUM, assetNum.getValue());
                }
                if (classId != null) {
                    ticket.addAttribute(CLASS_ID, classId.getValue());
                }
                if (classStructureId != null) {
                    ticket.addAttribute(CLASS_STRUCTURE_ID,
                                        classStructureId.getValue());
                }
                if (description != null) {
                    ticket.setSummary(description.getValue());
                }
                if (longDescription != null) {
                    ticket.setDetails(longDescription.getValue());
                }
                if (location != null) {
                    ticket.addAttribute(LOCATION, location.getValue());
                }
                if (ownerGroup != null) {
                    ticket.addAttribute(OWNER_GROUP, ownerGroup.getValue());
                }
                if (reportedBy != null) {
                    ticket.setUser(reportedBy.getValue());
                }
                if (shsCallerType != null) {
                    ticket.addAttribute(SHS_CALLER_TYPE,
                                        shsCallerType.getValue());
                }
                if (shsReasonForOutage != null) {
                    ticket.addAttribute(SHS_REASON_FOR_OUTAGE,
                                        shsReasonForOutage.getValue());
                }
                if (shsResolution != null) {
                    ticket.addAttribute(SHS_RESOLUTION,
                                        shsResolution.getValue());
                }
                if (shsRoomNumber != null) {
                    ticket.addAttribute(SHS_ROOM_NUMBER,
                                        shsRoomNumber.getValue());
                }
                if (siteId != null) {
                    ticket.addAttribute(SITE_ID, siteId.getValue());
                }
                if (source != null) {
                    ticket.addAttribute(SOURCE, source.getValue());
                }

                MXStringType status = new MXStringType();
                status = incident.getSTATUS();
                try {
                    if ((status != null) && (status.getValue() != null)
                            && (status.getValue().equals(getProperties().getProperty("tsrm.status.open")))) {
                        ticket.setState(Ticket.State.OPEN);
                    } else if ((status != null) && (status.getValue() != null)
                            && (status.getValue().equals(getProperties().getProperty("tsrm.status.close")))) {
                        ticket.setState(Ticket.State.CLOSED);
                    }
                } catch (IOException e) {
                    LOG.error("Unable to load tsrm.status from properties ",
                              e);
                }

                if (statusIface != null) {
                    ticket.addAttribute(STATUS_IFACE,
                                        statusIface.isValue().toString());
                }
                return ticket;
            }
            return null;

        } else {
            return null;
        }

    }

    @Override
    public void saveOrUpdate(Ticket ticket) throws PluginException {
        LOG.debug("saveOrUpdate called with ticket: {}", ticket);
        String ticketId = ticket.getId();

        if (StringUtils.isEmpty(ticketId)) {

            SHSIMPINCINCIDENTType incident = new SHSIMPINCINCIDENTType();

            updateIncidentWithTicket(incident, ticket);

            SHSIMPINCSetType incSetType = new SHSIMPINCSetType();
            List<SHSIMPINCINCIDENTType> incidentList = incSetType.getINCIDENT();
            incidentList.add(incident);
            CreateSHSIMPINCType createIncidentType = new CreateSHSIMPINCType();
            createIncidentType.setSHSIMPINCSet(incSetType);

            CreateSHSIMPINCResponseType response = port.createSHSIMPINC(createIncidentType);

            if (response != null) {
                List<INCIDENTKeyType> incidentKeyList = response.getINCIDENTMboKeySet().getINCIDENT();

                if (!CollectionUtils.isEmpty(incidentKeyList)) {
                    // Response will only have one element in the list
                    INCIDENTKeyType incidentKey = incidentKeyList.get(0);
                    ticket.setId(incidentKey.getTICKETID().getValue());
                }
            }

        } else {

            SHSIMPINCQueryType queryType = new SHSIMPINCQueryType();
            SHSIMPINCQueryType.INCIDENT incidentQuery = new SHSIMPINCQueryType.INCIDENT();
            List<MXStringQueryType> ticketList = incidentQuery.getTICKETID();
            MXStringQueryType ticketQuery = new MXStringQueryType();
            ticketQuery.setValue(ticketId);
            ticketList.add(ticketQuery);
            queryType.setINCIDENT(incidentQuery);
            QuerySHSIMPINCType queryIncident = new QuerySHSIMPINCType();
            queryIncident.setSHSIMPINCQuery(queryType);

            QuerySHSIMPINCResponseType response = port.querySHSIMPINC(queryIncident);

            if (!CollectionUtils.isEmpty(response.getSHSIMPINCSet().getINCIDENT())) {
                // Response will only have one element in the list
                SHSIMPINCINCIDENTType incident = response.getSHSIMPINCSet().getINCIDENT().get(0);

                updateIncidentWithTicket(incident, ticket);

                UpdateSHSIMPINCType updateIncident = new UpdateSHSIMPINCType();
                SHSIMPINCSetType updateIncidentType = new SHSIMPINCSetType();
                updateIncidentType.getINCIDENT().add(incident);
                updateIncident.setSHSIMPINCSet(updateIncidentType);
                port.updateSHSIMPINC(updateIncident);
            }

        }

    }

    private void updateIncidentWithTicket(SHSIMPINCINCIDENTType incident,
            Ticket ticket) {

        if (!StringUtils.isEmpty(ticket.getAttribute(AFFECTED_PERSON))) {
            MXStringType affectedPerson = new MXStringType();
            affectedPerson.setValue(ticket.getAttribute(AFFECTED_PERSON));
            incident.setAFFECTEDPERSON(affectedPerson);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(ASSET_NUM))) {
            MXStringType assetNum = new MXStringType();
            assetNum.setValue(ticket.getAttribute(ASSET_NUM));
            incident.setASSETNUM(assetNum);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(CLASS_ID))) {
            MXStringType classId = new MXStringType();
            classId.setValue(ticket.getAttribute(CLASS_ID));
            incident.setCLASS(classId);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(CLASS_STRUCTURE_ID))) {
            MXStringType classStructureId = new MXStringType();
            classStructureId.setValue(ticket.getAttribute(CLASS_STRUCTURE_ID));
            incident.setCLASSSTRUCTUREID(classStructureId);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(COMMODITY))) {
            MXStringType commodity = new MXStringType();
            commodity.setValue(ticket.getAttribute(COMMODITY));
            incident.setCOMMODITY(commodity);
        }

        if (!StringUtils.isEmpty(ticket.getSummary())) {
            MXStringType description = new MXStringType();
            description.setValue(ticket.getSummary());
            incident.setDESCRIPTION(description);
        }

        if (!StringUtils.isEmpty(ticket.getDetails())) {
            MXStringType longDescription = new MXStringType();
            longDescription.setValue(ticket.getDetails());
            incident.setDESCRIPTIONLONGDESCRIPTION(longDescription);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(LOCATION))) {
            MXStringType location = new MXStringType();
            location.setValue(ticket.getAttribute(LOCATION));
            incident.setLOCATION(location);
        }
        if (!StringUtils.isEmpty(ticket.getAttribute(OWNER_GROUP))) {
            MXStringType ownerGroup = new MXStringType();
            ownerGroup.setValue(ticket.getAttribute(OWNER_GROUP));
            incident.setOWNERGROUP(ownerGroup);
        }

        if (!StringUtils.isEmpty(ticket.getUser())) {
            if (StringUtils.isEmpty(ticket.getId())) {
                MXStringType reportedBy = new MXStringType();
                reportedBy.setValue(ticket.getUser());
                incident.setREPORTEDBY(reportedBy);
            } else {
                MXDateTimeType date = new MXDateTimeType();
                GregorianCalendar calendarTime = new GregorianCalendar();
                calendarTime.setTime(new Date());
                XMLGregorianCalendar value;
                @SuppressWarnings({ "unchecked", "rawtypes" })
                JAXBElement<MXDateTimeType> jaxbElement = new JAXBElement(new QName(MXDateTimeType.class.getName()),
                                                                          MXDateTimeType.class,
                                                                          date);
                try {
                    value = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendarTime);
                    date.setValue(value);
                    incident.setCHANGEDATE(jaxbElement);
                } catch (DatatypeConfigurationException e) {
                    LOG.error("Unable to create changedDate", e);
                }

            }
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(SHS_CALLER_TYPE))) {
            MXStringType shsCallerType = new MXStringType();
            shsCallerType.setValue(ticket.getAttribute(SHS_CALLER_TYPE));
            incident.setSHSCALLERTYPE(shsCallerType);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(SHS_REASON_FOR_OUTAGE))) {
            MXStringType shsReasonForOutage = new MXStringType();
            shsReasonForOutage.setValue(ticket.getAttribute(SHS_REASON_FOR_OUTAGE));
            incident.setSHSREASONFOROUTAGE(shsReasonForOutage);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(SHS_RESOLUTION))) {
            MXStringType shsResolution = new MXStringType();
            shsResolution.setValue(ticket.getAttribute(SHS_RESOLUTION));
            incident.setSHSRESOLUTION(shsResolution);
        }

        if (!StringUtils.isEmpty(ticket.getAttribute(SHS_ROOM_NUMBER))) {
            MXStringType shsRoomNumber = new MXStringType();
            shsRoomNumber.setValue(ticket.getAttribute(SHS_ROOM_NUMBER));
            incident.setSHSROOMNUMBER(shsRoomNumber);
        }
        if (!StringUtils.isEmpty(ticket.getAttribute(SITE_ID))) {
            MXStringType siteId = new MXStringType();
            siteId.setValue(ticket.getAttribute(SITE_ID));
            incident.setSITEID(siteId);
        }
        if (!StringUtils.isEmpty(ticket.getAttribute(SOURCE))) {
            MXStringType source = new MXStringType();
            source.setValue(ticket.getAttribute(SOURCE));
            incident.setSOURCE(source);
        }

        MXStringType status = new MXStringType();
        try {
            if (ticket.getState().equals(Ticket.State.OPEN)) {

                status.setValue(getProperties().getProperty("tsrm.status.open"));

            } else if (ticket.getState().equals(Ticket.State.CLOSED)) {
                status.setValue(getProperties().getProperty("tsrm.status.close"));
            }
        } catch (IOException e) {
            LOG.error("Unable to load tsrm.status from properties ", e);
        }
        incident.setSTATUS(status);

        if (!StringUtils.isEmpty(ticket.getAttribute(STATUS_IFACE))) {
            MXBooleanType statusIface = new MXBooleanType();
            statusIface.setValue(Boolean.parseBoolean(ticket.getAttribute(STATUS_IFACE)));
            incident.setSTATUSIFACE(statusIface);
        }

    }

    private static Properties getProperties() throws IOException {

        File home = new File(System.getProperty("opennms.home"));
        File etc = new File(home, "etc");
        File config = new File(etc, "tsrm.properties");

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(config)) {
            props.load(in);

        } catch (IOException e) {
            LOG.error("Unable to load config  {} ", config, e);
            throw new IOException("Error loading properties", e);
        }

        LOG.debug("Loaded endpointURL {} ",
                  props.getProperty("tsrm.url").toString());
        LOG.debug("Loaded disableSSLCheck {} ",
                  props.getProperty("tsrm.ssl.strict").toString());

        return props;
    }

    public SHSIMPINCPortType getPort() {
        return port;
    }

    public void setPort(SHSIMPINCPortType port) {
        this.port = port;
    }
}
