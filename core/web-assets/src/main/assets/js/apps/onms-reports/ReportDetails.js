import Types from '../../lib/onms-schedule-editor/scripts/Types';
import ScheduleOptions from '../../lib/onms-schedule-editor/scripts/ScheduleOptions';
import ContextError from '../../lib/onms-http/ContextError';
import Util from 'lib/util';

import moment from 'moment';
require('moment-timezone');

export default class ReportDetails {

    constructor(input = {}) {
        this.supportedSurveillanceCategories = input.surveillanceCategories;
        this.supportedCategories = input.categories;
        this.supportedFormats = [];
        if (Array.isArray(input.formats)) {
            this.supportedFormats = input.formats.map(function(item) {
                return item.name
            });
        }
        this.supportedTimezones = input.timezones || ['US/Eastern'];
        this.parameters = input.parameters || [];
        this.parametersByName = {};
        this.deliveryOptions = input.deliveryOptions || {};
        this.scheduleOptions = typeof input.cronExpression === 'string' ? ScheduleOptions.createFrom(input.cronExpression) : new ScheduleOptions();
        this.format = input.deliveryOptions && input.deliveryOptions.format || 'PDF';
        this.id = input.id;
        this.errors = input.errors || {};
        this.timezoneEditable = true;

        // In order to have the ui look the same as before, just order the parameters
        const order = ['string', 'integer', 'float', 'double', 'timezone', 'date'];
        this.parameters.sort(function(left, right) {
            return order.indexOf(left.type) - order.indexOf(right.type);
        });

        // Pre processing of parameters
        this.parameters.forEach(function(parameter) {
            // Apply default values for categories
            if (parameter.inputType === 'reportCategorySelector') {
                parameter.value = this.supportedSurveillanceCategories[0];
            }
            if (parameter.inputType === 'onmsCategorySelector') {
                parameter.value = this.supportedCategories[0];
            }

            // Hide certain items
            parameter.hidden = parameter.name === 'GRAFANA_ENDPOINT_UID'
                || parameter.name === 'GRAFANA_DASHBOARD_UID'
                || parameter.name === 'dateFormat';

            // index parameters
            this.parametersByName[parameter.name] = parameter;
        }, this);

        this.parameters.filter(function(parameter) {
            return parameter.type === 'date'
        }).forEach(function(parameter) {
            // Originally the idea was to format the date using the user locale setting
            // However this format is ISO conform, so we always use it instead
            parameter.internalFormat = 'YYYY-MM-DD HH:mm';
            parameter.internalLocale = 'en'; // Always assume en as locale
            parameter.internalValue = moment(parameter.date, parameter.internalFormat).hours(parameter.hours).minutes(parameter.minutes).format(parameter.internalFormat);
        });

        this.updateTimezoneEditable();
        this.validateTimezone();

        // Adjust format
        if (this.supportedFormats.indexOf(this.format) === -1) {
            this.format = this.supportedFormats[0];
        }
        if (this.supportedFormats.indexOf(this.deliveryOptions.format) === -1) {
            this.deliveryOptions.format = this.format;
        }

        if (window._onmsZoneId) {
            this.scheduleOptions.serverZone = window._onmsZoneId;
        } else {
            const xhr = new XMLHttpRequest();
            const checkResponseText = () => {
                try {
                    if (xhr.readyState === XMLHttpRequest.DONE) {
                        if (xhr.status === 200) {
                            const config = JSON.parse(xhr.responseText);
                            if (config.datetimeformatConfig && config.datetimeformatConfig.zoneId) {
                                window._onmsZoneId = config.datetimeformatConfig.zoneId;
                                this.scheduleOptions.serverZone = config.datetimeformatConfig.zoneId;
                                return;
                            }
                        }
                        // eslint-disable-next-line no-console
                        console.error('Failed to request server time zone: ' + xhr.status + ' ' + xhr.statusText);
                        this.scheduleOptions.serverZone = null;
                    }
                } catch (e) {
                    // eslint-disable-next-line no-console
                    console.error('An error occurred getting the server time zone:', e);
                    this.scheduleOptions.serverZone = null;
                }
            };

            xhr.onreadystatechange = () => {
                if (input && input.scope) {
                    input.scope.$evalAsync(checkResponseText);
                } else {
                    checkResponseText();
                }
            };
            xhr.open('GET', Util.getBaseHref() + 'rest/info');
            xhr.setRequestHeader('Accept', 'application/json');
            xhr.send();
        }

    }

    hasErrors() {
        const hasErrors = Object.keys(this.errors).length > 0
            || (this.isGrafanaReport() && !this.isGrafanaEndpointSelected())
            || !this.scheduleOptions.isValid();
        return hasErrors;
    }

    resetErrors() {
        this.errors = {};
    }

    setErrors(contextError) {
        if ((contextError && contextError.context && contextError.message) || (contextError instanceof ContextError)) {
            if (contextError.context !== 'cronExpression'
                  || (contextError.context === 'cronExpression' && this.scheduleOptions.type === Types.CUSTOM)
            ) {
                this.errors[contextError.context] = contextError.message;
                return;
            } else if (contextError.context === 'cronExpression' && this.scheduleOptions.type !== Types.CUSTOM) {
                throw new Error("Generated cronExpression was not parsable by backend. If this happens contact OpenNMS support");
            }
        }
        throw new Error("Provided contextError must be of type ContextError")
    }

    isGrafanaEndpointSelected() {
        if (!this.isGrafanaReport()) {
            throw new Error("Report is not a Grafana Report");
        }
        const endpointUid = this.parametersByName['GRAFANA_ENDPOINT_UID'];
        const dashboardUid = this.parametersByName['GRAFANA_DASHBOARD_UID'];

        const endpointSelected = endpointUid.value && typeof endpointUid.value === 'string' && endpointUid.value.length > 0
            && dashboardUid.value && typeof dashboardUid.value && dashboardUid.value.length > 0;
        return endpointSelected;
    }

    isGrafanaReport() {
        return typeof this.parametersByName['GRAFANA_ENDPOINT_UID'] !== 'undefined'
            && typeof this.parametersByName['GRAFANA_DASHBOARD_UID'] !== 'undefined'
    }

    updateTimezoneEditable() {
        const dashboardZone = this.dashboard && this.dashboard.timezone !== undefined ? this.dashboard.timezone : undefined;

        if (dashboardZone !== undefined) {
            if (dashboardZone === 'browser' || dashboardZone.trim() === '') {
                // timezone in grafana dashboard is set to default/browser,
                // which for our purposes should behave the same
                this.timezoneEditable = true;
            } else if (this.supportedTimezones.indexOf(this.parametersByName['timezone'].value) >= 0) {
                // timezone in grafana dashboard matches an available timezone,
                // use it (always)
                this.timezoneEditable = false;
            } else {
                // timezone in grafana dashboard isn't default/browser, but
                // we don't have a matching supported timezone; the timezone
                // on the other end is one supposedly supported by Jasper
                // so I guess I'll just add it to the list ¯\_(ツ)_/¯
                this.supportedTimezones.push(dashboardZone);
                this.timezoneEditable = false;
            }
        } else {
            // either this isn't a grafana report, or we haven't gotten the dashboard timezone (yet)
        }
    }

    validateTimezone() {
        this.updateTimezoneEditable();

        // Ensure timezone has a valid value
        this.parameters.filter((parameter) => {
            return parameter.type === 'timezone';
         }).forEach((parameter) => {
            if (this.timezoneEditable) {
                if (parameter.value && parameter.value.trim().length > 0 && this.supportedTimezones.indexOf(parameter.value) >= 0) {
                    // we have already selected a valid timezone, carry on
                } else {
                    // otherwise, guess the timezone; If it actually exists, it is used;
                    // if it doesn't exist, the first from the list is selected
                    const guessedTimezone = moment.tz.guess(true);
                    if (this.supportedTimezones.indexOf(guessedTimezone) >= 0) {
                        parameter.value = guessedTimezone;
                    } else {
                        parameter.value = this.supportedTimezones[0];
                    }
                }

                this.parametersByName['timezone'] = parameter;
            } else {
                // if the timezone is not editable, it should already be
                // set to something in the supported list, so it's safe
                // to just not do anything
            }
        });
        if (this.parametersByName['timezone']) {
            this.scheduleOptions.timezone = this.parametersByName['timezone'].value;
        }
    }

    updateTimezoneParameter(selected) {
        this.dashboard = selected.dashboard;
        let timezone = selected.dashboard ? selected.dashboard.timezone : undefined;
        if (timezone === 'utc') {
            // special case: Grafana passes UTC as `utc` (sigh)
            timezone = 'UTC';
        }
        if (timezone) {
            this.parametersByName['timezone'].value = timezone;
            this.scheduleOptions.timezone = timezone;
        }
        this.validateTimezone();
    }

    // Before sending the report we must replace the values for the Endpoint UID and Dashboard UID
    updateGrafanaParameters(selected) {
        if (this.isGrafanaReport()) {
            this.parametersByName['GRAFANA_ENDPOINT_UID'].value = selected.endpoint ? selected.endpoint.uid : this.parametersByName['GRAFANA_ENDPOINT_UID'].value;
            this.parametersByName['GRAFANA_DASHBOARD_UID'].value = selected.dashboard ? selected.dashboard.uid : this.parametersByName['GRAFANA_DASHBOARD_UID'].value;
            this.updateTimezoneParameter(selected);
        }
    }

    // Before sending the report, the date values must be updated accordingly
    updateDateParameters() {
        // Set the date value
        this.parameters.filter(function (parameter) {
            return parameter.type === 'date';
        }).forEach(function (p) {
            const date = moment(p.internalValue, p.internalFormat);
            p.date = date.format('YYYY-MM-DD');
            p.hours = date.hours();
            p.minutes = date.minutes();
            p.internalValue = date.format(p.internalFormat);
        });
    }
}
