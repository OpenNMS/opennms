import Types from '../../lib/onms-schedule-editor/scripts/Types';
import ScheduleOptions from '../../lib/onms-schedule-editor/scripts/ScheduleOptions';
import ContextError from "../../lib/onms-http/ContextError";
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

        // In order to have the ui look the same as before, just order the parameters
        const order = ['string', 'integer', 'float', 'double', 'date', 'timezone'];
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
            parameter.hidden = parameter.name === 'GRAFANA_ENDPOINT_UID' || parameter.name === 'GRAFANA_DASHBOARD_UID';

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
            parameter.internalValue = moment(parameter.date, parameter.internalFormat).hours(parameter.hours).minutes(parameter.minutes).toDate();
        });

        // Ensure timezone has a valid value
        this.parameters.filter(function(parameter) {
           return parameter.type === 'timezone' && (typeof parameter.value === 'undefined' || parameter.value === '');
        }).forEach(function(parameter) {
            // We guess the timezone. If it actually exist, it is used
            // otherwise the first is selected
            const guessedTimezone = moment.tz.guess(true);
            if (this.supportedTimezones.indexOf(guessedTimezone) >= 0) {
                parameter.value = guessedTimezone;
            } else {
                parameter.value = this.supportedTimezones[0];
            }
        }, this);

        // Adjust format
        if (this.supportedFormats.indexOf(this.format) === -1) {
            this.format = this.supportedFormats[0];
        }
        if (this.supportedFormats.indexOf(this.deliveryOptions.format) === -1) {
            this.deliveryOptions.format = this.format;
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

    // Before sending the report we must replace the values for the Endpoint UID and Dashboard UID
    updateGrafanaParameters(selected) {
        if (this.isGrafanaReport()) {
            this.parametersByName['GRAFANA_ENDPOINT_UID'].value = selected.endpoint ? selected.endpoint.uid : undefined;
            this.parametersByName['GRAFANA_DASHBOARD_UID'].value = selected.dashboard ? selected.dashboard.uid : undefined;
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
        });
    }
}
