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
package org.opennms.web.rest.v1.config;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.OnmsRestService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * The Class JavamailConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class JavamailConfigurationResource extends OnmsRestService implements InitializingBean {

    /** The Javamail configuration DAO. */
    @Resource(name="javamailConfigDao")
    private JavaMailConfigurationDao m_javamailConfigurationDao;

    /** The event proxy. */
    @Resource(name="eventProxy")
    private EventProxy m_eventProxy;

    /**
     * The Class SendmailConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="sendmail-configs")
    public static class SendmailConfigList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new sendmail configuration list.
         */
        public SendmailConfigList() {}

        /**
         * Instantiates a new sendmail configuration list.
         *
         * @param sendmailConfigs the sendmail configurations
         */
        public SendmailConfigList(List<SendmailConfig> sendmailConfigs) {
            sendmailConfigs.forEach(d -> {
                if (d.getName() != null) {
                    add(d.getName());
                }
            });
        }

        /**
         * Gets the sendmail configurations.
         *
         * @return the sendmail configurations
         */
        @XmlElement(name="sendmail-config")
        public List<String> getSendmailConfigs() {
            return getObjects();
        }
    }

    /**
     * The Class ReadmailConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="sendmail-configs")
    public static class ReadmailConfigList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new readmail configuration list.
         */
        public ReadmailConfigList() {}

        /**
         * Instantiates a new readmail configuration list.
         *
         * @param sendmailConfigs the sendmail configurations
         */
        public ReadmailConfigList(List<ReadmailConfig> sendmailConfigs) {
            sendmailConfigs.forEach(d -> {
                if (d.getName() != null) {
                    add(d.getName());
                }
            });
        }

        /**
         * Gets the readmail configurations.
         *
         * @return the readmail configurations
         */
        @XmlElement(name="readmail-config")
        public List<String> getReadmailConfigs() {
            return getObjects();
        }
    }

    /**
     * The Class End2endConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="end2end-configs")
    public static class End2endConfigList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new end2end configuration list.
         */
        public End2endConfigList() {}

        /**
         * Instantiates a new end2end configuration list.
         *
         * @param end2endConfigs the end2end configurations
         */
        public End2endConfigList(List<End2endMailConfig> end2endConfigs) {
            end2endConfigs.forEach(d -> {
                if (d.getName() != null) {
                    add(d.getName());
                }
            });
        }

        /**
         * Gets the end2end configurations.
         *
         * @return the end2end configurations
         */
        @XmlElement(name="end2end-config")
        public List<String> getEnd2endConfigs() {
            return getObjects();
        }
    }

    /**
     * After properties set.
     *
     * @throws Exception the exception
     */
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_javamailConfigurationDao, "javamailConfigurationDao must be set!");
        Assert.notNull(m_eventProxy, "eventProxy must be set!");
    }

    /**
     * Gets the default readmail configuration.
     *
     * @return the default readmail configuration
     */
    @GET
    @Path("default/readmail")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Get the name of the default readmail configuration",
            description = """
                    Returns the value of the `default-read-config-name` attribute on the root element of
                    javamail-configuration.xml as a bare string, not as a JSON or XML document.

                    This operation produces text/plain only, so a request sent with
                    `Accept: application/json` is rejected with a 406.""",
            operationId = "getDefaultReadmailConfigurationName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The default readmail configuration name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "localhost"))),
            @ApiResponse(responseCode = "404", description = "No default readmail configuration is set. Bodiless.")
    })
    public Response getDefaultReadmailConfiguration() {
        ReadmailConfig config = m_javamailConfigurationDao.getDefaultReadmailConfig();
        if (config == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(config.getName()).build();
    }

    /**
     * Gets the default sendmail configuration.
     *
     * @return the default sendmail configuration
     */
    @GET
    @Path("default/sendmail")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Get the name of the default sendmail configuration",
            description = """
                    Returns the value of the `default-send-config-name` attribute on the root element of
                    javamail-configuration.xml as a bare string, not as a JSON or XML document.

                    This operation produces text/plain only, so a request sent with
                    `Accept: application/json` is rejected with a 406.""",
            operationId = "getDefaultSendmailConfigurationName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The default sendmail configuration name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "localhost"))),
            @ApiResponse(responseCode = "404", description = "No default sendmail configuration is set. Bodiless.")
    })
    public Response getDefaultSendmailConfiguration() {
        SendmailConfig config = m_javamailConfigurationDao.getDefaultSendmailConfig();
        if (config == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(config.getName()).build();
    }

    /**
     * Sets the default readmail configuration.
     *
     * @param readmailConfigName the readmail configuration name
     * @return the response
     */
    @PUT
    @Path("default/readmail/{readmailConfig}")
    @Operation(
            summary = "Set the default readmail configuration",
            description = """
                    Sets `default-read-config-name` on the root element of javamail-configuration.xml. The name
                    is taken from the path; there is no request body.

                    A name that matches no `readmail-config` entry is silently ignored: the call still returns
                    204 and the stored default is left unchanged. Check with
                    `GET /config/javamail/default/readmail` afterwards.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost. The passwords in `user-auth` are stored and returned in the clear.""",
            operationId = "setDefaultReadmailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The request was accepted and the configuration saved. This does not confirm that the name matched an existing configuration."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setDefaultReadmailConfiguration(@Parameter(description = "Name of the readmail-config entry.", required = true, example = "localhost") @PathParam("readmailConfig") final String readmailConfigName) {
        m_javamailConfigurationDao.setDefaultReadmailConfig(readmailConfigName);
        return saveConfiguration();
    }

    /**
     * Sets the default sendmail configuration.
     *
     * @param sendmailConfigName the sendmail configuration name
     * @return the response
     */
    @PUT
    @Path("default/sendmail/{sendmailConfig}")
    @Operation(
            summary = "Set the default sendmail configuration",
            description = """
                    Sets `default-send-config-name` on the root element of javamail-configuration.xml. The name
                    is taken from the path; there is no request body.

                    A name that matches no `sendmail-config` entry is silently ignored: the call still returns
                    204 and the stored default is left unchanged. Check with
                    `GET /config/javamail/default/sendmail` afterwards.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost. The passwords in `user-auth` are stored and returned in the clear.""",
            operationId = "setDefaultSendmailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The request was accepted and the configuration saved. This does not confirm that the name matched an existing configuration."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setDefaultSendmailConfiguration(@Parameter(description = "Name of the sendmail-config entry.", required = true, example = "localhost") @PathParam("sendmailConfig") final String sendmailConfigName) {
        m_javamailConfigurationDao.setDefaultSendmailConfig(sendmailConfigName);
        return saveConfiguration();
    }

    /**
     * Gets all the readmail configurations.
     *
     * @return the readmail configuration list
     */
    @GET
    @Path("readmails")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the readmail configuration names",
            description = """
                    Returns only the names of the `readmail-config` entries, not the configurations themselves.
                    Entries with no `name` are skipped.

                    The XML form is rooted at `sendmail-configs` even though the child elements are
                    `readmail-config`; the wrapper class carries the sendmail root element name.""",
            operationId = "getReadmailConfigurationNames")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The readmail configuration names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = ReadmailConfigList.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "totalCount": 2,
                                              "count": 2,
                                              "offset": 0,
                                              "readmail-config": ["localhost", "google"]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = ReadmailConfigList.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <sendmail-configs count="2" offset="0" totalCount="2">
                                              <readmail-config>localhost</readmail-config>
                                              <readmail-config>google</readmail-config>
                                            </sendmail-configs>"""))
                    })
    })
    public Response getReadmailConfigurations() {
        ReadmailConfigList readmails = new ReadmailConfigList(m_javamailConfigurationDao.getReadmailConfigs());
        return Response.ok(readmails).build();
    }

    /**
     * Gets all the sendmail configurations.
     *
     * @return the sendmail configuration list
     */
    @GET
    @Path("sendmails")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the sendmail configuration names",
            description = """
                    Returns only the names of the `sendmail-config` entries, not the configurations themselves.
                    Entries with no `name` are skipped.""",
            operationId = "getSendmailConfigurationNames")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The sendmail configuration names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SendmailConfigList.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "totalCount": 2,
                                              "count": 2,
                                              "offset": 0,
                                              "sendmail-config": ["localhost", "google"]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SendmailConfigList.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <sendmail-configs count="2" offset="0" totalCount="2">
                                              <sendmail-config>localhost</sendmail-config>
                                              <sendmail-config>google</sendmail-config>
                                            </sendmail-configs>"""))
                    })
    })
    public Response getSendmailConfigurations() {
        SendmailConfigList sendmails = new SendmailConfigList(m_javamailConfigurationDao.getSendmailConfigs());
        return Response.ok(sendmails).build();
    }

    /**
     * Gets all the end2end configurations.
     *
     * @return the end2end configuration list
     */
    @GET
    @Path("end2ends")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the end2end mail configuration names",
            description = """
                    Returns only the names of the `end2end-mail-config` entries. Each pairs a sendmail
                    configuration with a readmail configuration for the mail transport monitor to send through
                    one and read back from the other.""",
            operationId = "getEnd2endConfigurationNames")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The end2end configuration names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = End2endConfigList.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "totalCount": 1,
                                              "count": 1,
                                              "offset": 0,
                                              "end2end-config": ["default"]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = End2endConfigList.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <end2end-configs count="1" offset="0" totalCount="1">
                                              <end2end-config>default</end2end-config>
                                            </end2end-configs>"""))
                    })
    })
    public Response getEnd2endConfigurations() {
        End2endConfigList sendmails = new End2endConfigList(m_javamailConfigurationDao.getEnd2EndConfigs());
        return Response.ok(sendmails).build();
    }

    /**
     * Gets a specific readmail configuration.
     *
     * @param readmailConfig the readmail configuration
     * @return the readmail configuration
     */
    @GET
    @Path("readmails/{readmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one readmail configuration",
            description = """
                    Returns the named `readmail-config`. The literal name `default` is special-cased to the
                    configuration named by `default-read-config-name`, so a configuration actually called
                    `default` cannot be fetched by name.

                    `user-auth.password` is returned in the clear.""",
            operationId = "getReadmailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The readmail configuration.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = ReadmailConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "name": "ApiDocReadmail",
                                              "debug": false,
                                              "mail-folder": "INBOX",
                                              "attempt-interval": 1000,
                                              "delete-all-mail": false,
                                              "javamail-property": [
                                                {
                                                  "name": "mail.pop3.rsetbeforequit",
                                                  "value": "false"
                                                }
                                              ],
                                              "readmail-host": {
                                                "host": "127.0.0.1",
                                                "port": 110,
                                                "readmail-protocol": {
                                                  "transport": "pop3",
                                                  "ssl-enable": false,
                                                  "start-tls": false
                                                }
                                              },
                                              "user-auth": {
                                                "user-name": "opennms",
                                                "password": "opennms"
                                              }
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = ReadmailConfig.class),
                                    examples = @ExampleObject(value = """
                                            <readmail-config xmlns="http://xmlns.opennms.org/xsd/config/javamail-configuration" name="ApiDocReadmail" attempt-interval="1000" delete-all-mail="false"
                                                            mail-folder="INBOX" debug="false">
                                              <javamail-property name="mail.pop3.rsetbeforequit" value="false"/>
                                              <readmail-host host="127.0.0.1" port="110">
                                                <readmail-protocol transport="pop3" ssl-enable="false" start-tls="false"/>
                                              </readmail-host>
                                              <user-auth user-name="opennms" password="opennms"/>
                                            </readmail-config>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No readmail configuration has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Readmail configuration ApiDocReadmail was not found.")))
    })
    public ReadmailConfig getReadmailConfiguration(@Parameter(description = "Name of the readmail-config entry.", required = true, example = "localhost") @PathParam("readmailConfig") final String readmailConfig) {
        ReadmailConfig readmail = "default".equals(readmailConfig) ? m_javamailConfigurationDao.getDefaultReadmailConfig() : m_javamailConfigurationDao.getReadMailConfig(readmailConfig);
        if (readmail == null) {
            throw getException(Status.NOT_FOUND, "Readmail configuration {} was not found.", readmailConfig);
        }
        return readmail;
    }

    /**
     * Gets a specific sendmail configuration.
     *
     * @param sendmailConfig the sendmail configuration
     * @return the sendmail configuration
     */
    @GET
    @Path("sendmails/{sendmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one sendmail configuration",
            description = """
                    Returns the named `sendmail-config`. The literal name `default` is special-cased to the
                    configuration named by `default-send-config-name`, so a configuration actually called
                    `default` cannot be fetched by name.

                    `user-auth.password` is returned in the clear.""",
            operationId = "getSendmailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The sendmail configuration.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SendmailConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "name": "ApiDocSendmail",
                                              "debug": false,
                                              "use-authentication": false,
                                              "use-jmta": false,
                                              "attempt-interval": 3000,
                                              "javamail-property": [],
                                              "sendmail-host": {
                                                "host": "127.0.0.1",
                                                "port": 25
                                              },
                                              "sendmail-protocol": {
                                                "char-set": "us-ascii",
                                                "mailer": "smtpsend",
                                                "message-content-type": "text/plain",
                                                "message-encoding": "7-bit",
                                                "quit-wait": true,
                                                "transport": "smtp",
                                                "ssl-enable": false,
                                                "start-tls": false
                                              },
                                              "sendmail-message": {
                                                "to": "root@localhost",
                                                "from": "root@[127.0.0.1]",
                                                "reply-to": null,
                                                "subject": "OpenNMS Test Message",
                                                "body": "This is an OpenNMS test message."
                                              },
                                              "user-auth": {
                                                "user-name": "opennms",
                                                "password": "opennms"
                                              }
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SendmailConfig.class),
                                    examples = @ExampleObject(value = """
                                            <sendmail-config xmlns="http://xmlns.opennms.org/xsd/config/javamail-configuration" name="ApiDocSendmail" attempt-interval="3000" use-authentication="false"
                                                            use-jmta="false" debug="false">
                                              <sendmail-host host="127.0.0.1" port="25"/>
                                              <sendmail-protocol char-set="us-ascii" mailer="smtpsend"
                                                                 message-content-type="text/plain" message-encoding="7-bit"
                                                                 quit-wait="true" transport="smtp" ssl-enable="false"
                                                                 start-tls="false"/>
                                              <sendmail-message to="root@localhost" from="root@[127.0.0.1]"
                                                                subject="OpenNMS Test Message"
                                                                body="This is an OpenNMS test message."/>
                                              <user-auth user-name="opennms" password="opennms"/>
                                            </sendmail-config>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No sendmail configuration has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Sendmail configuration ApiDocSendmail was not found.")))
    })
    public SendmailConfig getSendmailConfiguration(@Parameter(description = "Name of the sendmail-config entry.", required = true, example = "localhost") @PathParam("sendmailConfig") final String sendmailConfig) {
        SendmailConfig sendmail = "default".equals(sendmailConfig) ? m_javamailConfigurationDao.getDefaultSendmailConfig() : m_javamailConfigurationDao.getSendMailConfig(sendmailConfig);
        if (sendmail == null) {
            throw getException(Status.NOT_FOUND, "Sendmail configuration {} was not found.", sendmailConfig);
        }
        return sendmail;
    }

    /**
     * Gets a specific end2end mail configuration.
     *
     * @param end2endConfig the end2end configuration
     * @return the end2end mail configuration
     */
    @GET
    @Path("end2ends/{end2endConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one end2end mail configuration",
            description = """
                    Returns the named `end2end-mail-config`. Unlike the readmail and sendmail lookups there is
                    no `default` special case, so `default` here means an entry actually named `default`.

                    Neither name is validated against the configurations that exist.""",
            operationId = "getEnd2endMailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The end2end mail configuration.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = End2endMailConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "name": "ApiDocEnd2End",
                                              "sendmail-config-name": "localhost",
                                              "readmail-config-name": "localhost"
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = End2endMailConfig.class),
                                    examples = @ExampleObject(value = """
                                            <end2end-mail-config xmlns="http://xmlns.opennms.org/xsd/config/javamail-configuration"
                                                             name="ApiDocEnd2End" sendmail-config-name="localhost"
                                                                 readmail-config-name="localhost"/>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No end2end configuration has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "End2End configuration ApiDocEnd2End was not found.")))
    })
    public End2endMailConfig getEnd2EndMailConfiguration(@Parameter(description = "Name of the end2end-mail-config entry.", required = true, example = "default") @PathParam("end2endConfig") final String end2endConfig) {
        End2endMailConfig end2end = m_javamailConfigurationDao.getEnd2endConfig(end2endConfig);
        if (end2end == null) {
            throw getException(Status.NOT_FOUND, "End2End configuration {} was not found.", end2endConfig);
        }
        return end2end;
    }

    /**
     * Sets the readmail configuration.
     * <p>If there is a readmail configuration with the same name, the existing one will be overridden.</p>
     *
     * @param readmailConfig the readmail configuration
     * @return the response
     */
    @POST
    @Path("readmails")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace a readmail configuration",
            description = """
                    Stores the `readmail-config` under its `name`, replacing any existing entry of that name
                    outright. This is the only way to change the nested `readmail-host`, `readmail-protocol`,
                    `user-auth` or `javamail-property` blocks: the PUT reaches the scalar fields only.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "setReadmailConfiguration")
    @RequestBody(required = true, description = "The readmail configuration to add or replace.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ReadmailConfig.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "name": "ApiDocReadmail",
                                              "debug": false,
                                              "mail-folder": "INBOX",
                                              "attempt-interval": 1000,
                                              "delete-all-mail": false,
                                              "javamail-property": [
                                                {
                                                  "name": "mail.pop3.rsetbeforequit",
                                                  "value": "false"
                                                }
                                              ],
                                              "readmail-host": {
                                                "host": "127.0.0.1",
                                                "port": 110,
                                                "readmail-protocol": {
                                                  "transport": "pop3",
                                                  "ssl-enable": false,
                                                  "start-tls": false
                                                }
                                              },
                                              "user-auth": {
                                                "user-name": "opennms",
                                                "password": "opennms"
                                              }
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ReadmailConfig.class),
                            examples = @ExampleObject(value = """
                                    <readmail-config xmlns="http://xmlns.opennms.org/xsd/config/javamail-configuration" name="ApiDocReadmail" attempt-interval="1000" delete-all-mail="false"
                                                            mail-folder="INBOX" debug="false">
                                              <javamail-property name="mail.pop3.rsetbeforequit" value="false"/>
                                              <readmail-host host="127.0.0.1" port="110">
                                                <readmail-protocol transport="pop3" ssl-enable="false" start-tls="false"/>
                                              </readmail-host>
                                              <user-auth user-name="opennms" password="opennms"/>
                                            </readmail-config>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The configuration was stored and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the configuration could not be saved. An empty body reaches this rather than the 400 the null check suggests.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setReadmailConfiguration(final ReadmailConfig readmailConfig) {
        writeLock();
        try {
            if (readmailConfig == null) {
                throw getException(Status.BAD_REQUEST, "Readmail configuration object cannot be null");
            }
            m_javamailConfigurationDao.addReadMailConfig(readmailConfig);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Sets the sendmail configuration.
     * <p>If there is a sendmail configuration with the same name, the existing one will be overridden.</p>
     *
     * @param sendmailConfig the sendmail configuration
     * @return the response
     */
    @POST
    @Path("sendmails")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace a sendmail configuration",
            description = """
                    Stores the `sendmail-config` under its `name`, replacing any existing entry of that name
                    outright. This is the only way to change the nested `sendmail-host`, `sendmail-protocol`,
                    `sendmail-message`, `user-auth` or `javamail-property` blocks: the PUT reaches the scalar
                    fields only.

                    Email northbounder destinations reference these entries by name, so the name is what ties a
                    destination to its SMTP settings.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "setSendmailConfiguration")
    @RequestBody(required = true, description = "The sendmail configuration to add or replace.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SendmailConfig.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "name": "ApiDocSendmail",
                                              "debug": false,
                                              "use-authentication": false,
                                              "use-jmta": false,
                                              "attempt-interval": 3000,
                                              "javamail-property": [],
                                              "sendmail-host": {
                                                "host": "127.0.0.1",
                                                "port": 25
                                              },
                                              "sendmail-protocol": {
                                                "char-set": "us-ascii",
                                                "mailer": "smtpsend",
                                                "message-content-type": "text/plain",
                                                "message-encoding": "7-bit",
                                                "quit-wait": true,
                                                "transport": "smtp",
                                                "ssl-enable": false,
                                                "start-tls": false
                                              },
                                              "sendmail-message": {
                                                "to": "root@localhost",
                                                "from": "root@[127.0.0.1]",
                                                "subject": "OpenNMS Test Message",
                                                "body": "This is an OpenNMS test message."
                                              },
                                              "user-auth": {
                                                "user-name": "opennms",
                                                "password": "opennms"
                                              }
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SendmailConfig.class),
                            examples = @ExampleObject(value = """
                                    <sendmail-config xmlns="http://xmlns.opennms.org/xsd/config/javamail-configuration" name="ApiDocSendmail" attempt-interval="3000" use-authentication="false"
                                                            use-jmta="false" debug="false">
                                              <sendmail-host host="127.0.0.1" port="25"/>
                                              <sendmail-protocol char-set="us-ascii" mailer="smtpsend"
                                                                 message-content-type="text/plain" message-encoding="7-bit"
                                                                 quit-wait="true" transport="smtp" ssl-enable="false"
                                                                 start-tls="false"/>
                                              <sendmail-message to="root@localhost" from="root@[127.0.0.1]"
                                                                subject="OpenNMS Test Message"
                                                                body="This is an OpenNMS test message."/>
                                              <user-auth user-name="opennms" password="opennms"/>
                                            </sendmail-config>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The configuration was stored and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the configuration could not be saved. An empty body reaches this rather than the 400 the null check suggests.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setSendmailConfiguration(final SendmailConfig sendmailConfig) {
        writeLock();
        try {
            if (sendmailConfig == null) {
                throw getException(Status.BAD_REQUEST, "Sendmail configuration object cannot be null");
            }
            m_javamailConfigurationDao.addSendMailConfig(sendmailConfig);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Sets the end2end mail configuration.
     * <p>If there is a end2end configuration with the same name, the existing one will be overridden.</p>
     *
     * @param end2endMailConfig the end2end mail configuration
     * @return the response
     */
    @POST
    @Path("end2ends")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace an end2end mail configuration",
            description = """
                    Stores the `end2end-mail-config` under its `name`, replacing any existing entry of that name
                    outright. Neither `sendmail-config-name` nor `readmail-config-name` is validated at write
                    time.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "setEnd2endMailConfiguration")
    @RequestBody(required = true, description = "The end2end mail configuration to add or replace.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = End2endMailConfig.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "name": "ApiDocEnd2End",
                                              "sendmail-config-name": "localhost",
                                              "readmail-config-name": "localhost"
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = End2endMailConfig.class),
                            examples = @ExampleObject(value = """
                                    <end2end-mail-config xmlns="http://xmlns.opennms.org/xsd/config/javamail-configuration"
                                                             name="ApiDocEnd2End" sendmail-config-name="localhost"
                                                                 readmail-config-name="localhost"/>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The configuration was stored and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the configuration could not be saved. An empty body reaches this rather than the 400 the null check suggests.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setEnd2EndMailConfiguration(final End2endMailConfig end2endMailConfig) {
        writeLock();
        try {
            if (end2endMailConfig == null) {
                throw getException(Status.BAD_REQUEST, "End2End configuration object cannot be null");
            }
            m_javamailConfigurationDao.addEnd2endMailConfig(end2endMailConfig);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update readmail configuration.
     *
     * @param readmailConfigName the readmail configuration name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("readmails/{readmailConfig}")
    @Operation(
            summary = "Update fields on a readmail configuration",
            description = """
                    Takes a form-encoded body and applies each key to the matching writable bean property of
                    the configuration, then saves. Keys are bean property names, not XML element or attribute
                    names, and unrecognised keys are ignored rather than rejected.

                    The writable properties are `name`, `debug`, `attemptInterval`, `deleteAllMail`, `mailFolder`, `javamailProperties`, `readmailHost` and `userAuth`. The ones holding nested objects or lists cannot be
                    expressed as a form value, so POST the configuration again to change those.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "updateReadmailConfiguration")
    @RequestBody(required = true, description = "Form-encoded property assignments, keyed by bean property name.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "debug=true&attemptInterval=2000&mailFolder=INBOX")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "At least one property was applied and the configuration was saved."),
            @ApiResponse(responseCode = "304", description = "No key in the body matched a writable property, so nothing was changed."),
            @ApiResponse(responseCode = "404", description = "No readmail configuration has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Readmail configuration ApiDocReadmail was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response updateReadmailConfiguration(@Parameter(description = "Name of the readmail-config entry.", required = true, example = "localhost") @PathParam("readmailConfig") final String readmailConfigName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            ReadmailConfig readmailConfig = getReadmailConfiguration(readmailConfigName);
            if (updateConfiguration(readmailConfig, params)) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update sendmail configuration.
     *
     * @param sendmailConfigName the sendmail configuration name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("sendmails/{sendmailConfig}")
    @Operation(
            summary = "Update fields on a sendmail configuration",
            description = """
                    Takes a form-encoded body and applies each key to the matching writable bean property of
                    the configuration, then saves. Keys are bean property names, not XML element or attribute
                    names, and unrecognised keys are ignored rather than rejected.

                    The writable properties are `name`, `debug`, `attemptInterval`, `useAuthentication`, `useJmta`, `javamailProperties`, `sendmailHost`, `sendmailProtocol`, `sendmailMessage` and `userAuth`. The ones holding nested objects or lists cannot be
                    expressed as a form value, so POST the configuration again to change those.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "updateSendmailConfiguration")
    @RequestBody(required = true, description = "Form-encoded property assignments, keyed by bean property name.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "debug=true&attemptInterval=5000&useAuthentication=true")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "At least one property was applied and the configuration was saved."),
            @ApiResponse(responseCode = "304", description = "No key in the body matched a writable property, so nothing was changed."),
            @ApiResponse(responseCode = "404", description = "No sendmail configuration has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Sendmail configuration ApiDocSendmail was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response updateSendmailConfiguration(@Parameter(description = "Name of the sendmail-config entry.", required = true, example = "localhost") @PathParam("sendmailConfig") final String sendmailConfigName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            SendmailConfig sendmailConfig = getSendmailConfiguration(sendmailConfigName);
            if (updateConfiguration(sendmailConfig, params)) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update end2end configuration.
     *
     * @param end2endConfigName the end2end configuration name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("end2ends/{end2endConfig}")
    @Operation(
            summary = "Update fields on a end2end mail configuration",
            description = """
                    Takes a form-encoded body and applies each key to the matching writable bean property of
                    the configuration, then saves. Keys are bean property names, not XML element or attribute
                    names, and unrecognised keys are ignored rather than rejected.

                    The writable properties are `name`, `sendmailConfigName` and `readmailConfigName`. The ones holding nested objects or lists cannot be
                    expressed as a form value, so POST the configuration again to change those.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "updateEnd2endMailConfiguration")
    @RequestBody(required = true, description = "Form-encoded property assignments, keyed by bean property name.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "sendmailConfigName=localhost&readmailConfigName=localhost")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "At least one property was applied and the configuration was saved."),
            @ApiResponse(responseCode = "304", description = "No key in the body matched a writable property, so nothing was changed."),
            @ApiResponse(responseCode = "404", description = "No end2end mail configuration has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "End2End configuration ApiDocEnd2End was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response updateEnd2endConfiguration(@Parameter(description = "Name of the end2end-mail-config entry.", required = true, example = "default") @PathParam("end2endConfig") final String end2endConfigName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            End2endMailConfig end2endConfig = getEnd2EndMailConfiguration(end2endConfigName);
            if (updateConfiguration(end2endConfig, params)) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Removes the readmail configuration.
     *
     * @param readmailConfig the readmail configuration name
     * @return the response
     */
    @DELETE
    @Path("readmails/{readmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete a readmail configuration",
            description = """
                    Removes the entry and saves javamail-configuration.xml. Nothing checks whether an `end2end-mail-config` or the `default-read-config-name` attribute still points at it.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "removeReadmailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The configuration was removed and the reload event sent."),
            @ApiResponse(responseCode = "404", description = "No readmail configuration has that name. Bodiless."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response removeReadmailConfig(@Parameter(description = "Name of the readmail-config entry.", required = true, example = "localhost") @PathParam("readmailConfig") final String readmailConfig) {
        if (m_javamailConfigurationDao.removeReadMailConfig(readmailConfig)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Removes the sendmail configuration.
     *
     * @param sendmailConfig the sendmail configuration name
     * @return the response
     */
    @DELETE
    @Path("sendmails/{sendmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete a sendmail configuration",
            description = """
                    Removes the entry and saves javamail-configuration.xml. Nothing checks whether an `end2end-mail-config`, the `default-send-config-name` attribute or an Email northbounder destination still points at it.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "removeSendmailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The configuration was removed and the reload event sent."),
            @ApiResponse(responseCode = "404", description = "No sendmail configuration has that name. Bodiless."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response removeSendmailConfig(@Parameter(description = "Name of the sendmail-config entry.", required = true, example = "localhost") @PathParam("sendmailConfig") final String sendmailConfig) {
        if (m_javamailConfigurationDao.removeSendMailConfig(sendmailConfig)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Removes the end2end configuration.
     *
     * @param end2endConfig the end2end configuration name
     * @return the response
     */
    @DELETE
    @Path("end2ends/{end2endConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete a end2end mail configuration",
            description = """
                    Removes the entry and saves javamail-configuration.xml.

                    Any write here re-marshals the whole of javamail-configuration.xml from the in-memory model and
                    then sends a `reloadDaemonConfig` event naming `EmailNBI`, so comments and formatting in the
                    file are lost.""",
            operationId = "removeEnd2endMailConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The configuration was removed and the reload event sent."),
            @ApiResponse(responseCode = "404", description = "No end2end mail configuration has that name. Bodiless."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response removeEnd2endConfig(@Parameter(description = "Name of the end2end-mail-config entry.", required = true, example = "default") @PathParam("end2endConfig") final String end2endConfig) {
        if (m_javamailConfigurationDao.removeEnd2endConfig(end2endConfig)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Update configuration.
     *
     * @param config the configuration object
     * @param params the parameters
     * @return true, if successful
     */
    private boolean updateConfiguration(final Object config, final MultivaluedMapImpl params) {
        boolean modified = false;
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(config);
        for (final String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                final String stringValue = params.getFirst(key);
                final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Saves the configuration.
     *
     * @return the response
     */
    public Response saveConfiguration() {
        writeLock();
        try {
            // FIXME Validate configuration.
            m_javamailConfigurationDao.saveConfiguration();
            EventBuilder eb = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "ReST");
            eb.addParam(EventConstants.PARM_DAEMON_NAME, "EmailNBI");
            m_eventProxy.send(eb.getEvent());
            return Response.noContent().build();
        } catch (Throwable t) {
            throw getException(Status.INTERNAL_SERVER_ERROR, t);
        } finally {
            writeUnlock();            
        }
    }

}
