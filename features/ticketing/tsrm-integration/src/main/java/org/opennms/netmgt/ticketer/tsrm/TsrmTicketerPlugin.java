package org.opennms.netmgt.ticketer.tsrm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.opennms.netmgt.ticketer.tsrm.TsrmConstants.*;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.maximo.CreateSHSIMPINCResponseType;
import com.ibm.maximo.CreateSHSIMPINCType;
import com.ibm.maximo.INCIDENTKeyType;
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
                final TrustManager[] simpleTrustManager = new TrustManager[] {
                        new X509TrustManager() {
                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs,
                                    String authType) {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs,
                                    String authType) {
                            }

                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                        } };
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

                MXStringType ticketIdFromIncident = new MXStringType();
                ticketIdFromIncident = incident.getTICKETID();
                MXStringType description = new MXStringType();
                description = incident.getDESCRIPTION();
                MXStringType longDescription = new MXStringType();
                longDescription = incident.getDESCRIPTIONLONGDESCRIPTION();
                MXStringType location = new MXStringType();
                location = incident.getLOCATION();
                MXStringType reportedBy = new MXStringType();
                reportedBy = incident.getREPORTEDBY();
                MXStringType classStructureId = new MXStringType();
                classStructureId = incident.getCLASSSTRUCTUREID();
                MXStringType ownerGroup = new MXStringType();
                ownerGroup = incident.getOWNERGROUP();
                MXStringType siteId = new MXStringType();
                siteId = incident.getSITEID();
                MXStringType source = new MXStringType();
                source = incident.getSOURCE();

                if (ticketIdFromIncident == null) {
                    return null;
                }

                ticket.setId(ticketIdFromIncident.getValue());

                if (description != null) {
                    ticket.setSummary(description.getValue());
                }
                if (longDescription != null) {
                    ticket.setDetails(longDescription.getValue());
                }
                if (reportedBy != null) {
                    ticket.setUser(reportedBy.getValue());
                }
                if (location != null) {
                    ticket.addAttribute(LOCATION, location.getValue());
                }
                if (classStructureId != null) {
                    ticket.addAttribute(CLASS_STRUCTURE_ID,
                                        classStructureId.getValue());
                }
                if (ownerGroup != null) {
                    ticket.addAttribute(OWNER_GROUP, ownerGroup.getValue());
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
                return ticket;
            }
            return null;

        } else {
            return null;
        }

    }

    @Override
    public void saveOrUpdate(Ticket ticket) throws PluginException {

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

        MXStringType location = new MXStringType();
        location.setValue(ticket.getAttribute(LOCATION));
        incident.setLOCATION(location);

        MXStringType description = new MXStringType();
        description.setValue(ticket.getSummary());
        incident.setDESCRIPTION(description);

        MXStringType longDescription = new MXStringType();
        longDescription.setValue(ticket.getDetails());
        incident.setDESCRIPTIONLONGDESCRIPTION(longDescription);

        MXStringType classStructureId = new MXStringType();
        classStructureId.setValue(ticket.getAttribute(CLASS_STRUCTURE_ID));
        incident.setCLASSSTRUCTUREID(classStructureId);

        MXStringType ownerGroup = new MXStringType();
        ownerGroup.setValue(ticket.getAttribute(OWNER_GROUP));
        incident.setOWNERGROUP(ownerGroup);

        MXStringType siteId = new MXStringType();
        siteId.setValue(ticket.getAttribute(SITE_ID));
        incident.setSITEID(siteId);

        MXStringType source = new MXStringType();
        source.setValue(ticket.getAttribute(SOURCE));
        incident.setSOURCE(source);

        MXStringType reportedBy = new MXStringType();
        reportedBy.setValue(ticket.getUser());
        incident.setREPORTEDBY(reportedBy);

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
