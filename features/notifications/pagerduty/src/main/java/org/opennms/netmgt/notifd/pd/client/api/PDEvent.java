/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd.pd.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PDEvent {

    @JsonProperty("payload")
    private PDEventPayload payload;

    /**
     * The GUID of one of your Events API V2 integrations. This is the "Integration Key" listed on the Events API V2 integration's detail page.
     */
    @JsonProperty("routing_key")
    private String routingKey;

    /**
     * Deduplication key for correlating triggers and resolves.
     */
    @JsonProperty("dedup_key")
    private String dedupKey;

    /**
     * The type of event.
     */
    @JsonProperty("event_action")
    @JsonSerialize(using = PDEventActionSerializer.class)
    private PDEventAction eventAction;

    @JsonProperty("client")
    private String client;

    @JsonProperty("client_url")
    private String clientUrl;

    /**
     * This property is used to attach images to the incident.
     */
    @JsonProperty("images")
    private List<PDEventImage> images = new ArrayList<>();

    /**
     * This property is used to attach links to the incident.
     */
    @JsonProperty("links")
    private List<PDEventLink> links = new ArrayList<>();

    public PDEventPayload getPayload() {
        return payload;
    }

    public void setPayload(PDEventPayload payload) {
        this.payload = payload;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public void setDedupKey(String dedupKey) {
        this.dedupKey = dedupKey;
    }

    public PDEventAction getEventAction() {
        return eventAction;
    }

    public void setEventAction(PDEventAction eventAction) {
        this.eventAction = eventAction;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public List<PDEventImage> getImages() {
        return images;
    }

    public void setImages(List<PDEventImage> images) {
        this.images = images;
    }

    public List<PDEventLink> getLinks() {
        return links;
    }

    public void setLinks(List<PDEventLink> links) {
        this.links = links;
    }
}
