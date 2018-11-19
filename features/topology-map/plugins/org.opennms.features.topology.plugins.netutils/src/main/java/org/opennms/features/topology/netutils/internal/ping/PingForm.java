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

package org.opennms.features.topology.netutils.internal.ping;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.converter.StringToLongConverter;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

/**
 * Vaadin-Form to allow configuring a Ping command {@link org.opennms.netmgt.icmp.Pinger}.
 *
 * @author mvrueden
 */
public class PingForm extends FormLayout {

    private static final float FIELD_WIDTH = 150;

    private final BeanItem<PingRequest> item;
    private final NativeSelect ipDropdown;
    private final NativeSelect packetSizeDropdown;
    private final NativeSelect locationDropdown;
    private final TextField numberOfRequestsField;
    private final TextField timeoutField;
    private final FieldGroup binder;

    public PingForm(List<String> locations, String defaultLocation, List<String> ipAddresses, String defaultIp) {
        Objects.requireNonNull(locations);
        Objects.requireNonNull(defaultLocation);
        Objects.requireNonNull(ipAddresses);
        Objects.requireNonNull(defaultIp);

        PingRequest pingRequest = new PingRequest()
                .withNumberRequests(4)
                .withTimeout(1, TimeUnit.SECONDS)
                .withPackageSize(64)
                .withIpAddress(defaultIp)
                .withLocation(defaultLocation);

        // IP
        ipDropdown = new NativeSelect();
        ipDropdown.setCaption("IP Address");
        for (String eachIp : ipAddresses) {
            ipDropdown.addItem(eachIp);
        }
        ipDropdown.setNullSelectionAllowed(false);
        ipDropdown.setWidth(FIELD_WIDTH, Unit.PIXELS);

		// Packet Size
        packetSizeDropdown = new NativeSelect();
        packetSizeDropdown.setCaption("Packet Size");
        packetSizeDropdown.addItem(64);
        packetSizeDropdown.addItem(128);
        packetSizeDropdown.addItem(256);
        packetSizeDropdown.addItem(512);
        packetSizeDropdown.addItem(1024);
        packetSizeDropdown.addItem(2048);
        packetSizeDropdown.setNullSelectionAllowed(false);
        packetSizeDropdown.setWidth(FIELD_WIDTH, Unit.PIXELS);

        // Location
        locationDropdown = new NativeSelect();
        locationDropdown.setCaption("Location");
        for (String eachLocation : locations) {
            locationDropdown.addItem(eachLocation);
        }
        locationDropdown.setNullSelectionAllowed(false);
        locationDropdown.setWidth(FIELD_WIDTH, Unit.PIXELS);

        // Number of Requests
        numberOfRequestsField = new TextField();
        numberOfRequestsField.setCaption("Number of Requests");
        numberOfRequestsField.setRequired(true);
        numberOfRequestsField.setRequiredError("Must be given");
        numberOfRequestsField.setNullRepresentation("");
        numberOfRequestsField.setWidth(FIELD_WIDTH, Unit.PIXELS);
        numberOfRequestsField.addValidator((Validator) value -> {
            if (value != null) {
                if (((Integer) value).intValue() <= 0) {
                    throw new Validator.InvalidValueException("must be > 0");
                }
            }
        });

        // Timeout
        timeoutField = new TextField();
        timeoutField.setCaption("Timeout (seconds)");
        timeoutField.setRequired(true);
        timeoutField.setRequiredError("Must be given");
        timeoutField.setWidth(FIELD_WIDTH, Unit.PIXELS);
        timeoutField.setConverter(new StringToLongConverter() {
            @Override
            public Long convertToModel(String value, Class<? extends Long> targetType, Locale locale) throws ConversionException {
                Long longValue = super.convertToModel(value, targetType, locale);
                if (longValue != null) {
                    return TimeUnit.MILLISECONDS.convert(longValue, TimeUnit.SECONDS);
                }
                return longValue;
            }

            @Override
            public String convertToPresentation(Long value, Class<? extends String> targetType, Locale locale) throws ConversionException {
                if (value != null) {
                    return super.convertToPresentation(TimeUnit.SECONDS.convert(value, TimeUnit.MILLISECONDS), targetType, locale);
                }
                return super.convertToPresentation(value, targetType, locale);
            }
        });
        timeoutField.addValidator(new NullValidator("Must be given", false));
        timeoutField.addValidator((Validator) value -> {
            if (value != null) {
                if (((Long) value).intValue() <= 0) {
                    throw new Validator.InvalidValueException("must be > 0");
                }
            }
        });

        addComponent(ipDropdown);
        addComponent(numberOfRequestsField);
        addComponent(locationDropdown);
        addComponent(timeoutField);
        addComponent(packetSizeDropdown);

        item = new BeanItem<>(pingRequest);
        binder = new FieldGroup(item);
        binder.bind(ipDropdown, "ipAddress");
        binder.bind(numberOfRequestsField, "numberRequests");
        binder.bind(timeoutField, "timeout");
        binder.bind(packetSizeDropdown, "packetSize");
        binder.bind(locationDropdown, "location");
        binder.setBuffered(true);
    }

    public PingRequest getPingRequest() throws FieldGroup.CommitException {
        binder.commit(); // we must commit before to ensure there are no errors
        return item.getBean()
                .withRetries(0);
    }

}
