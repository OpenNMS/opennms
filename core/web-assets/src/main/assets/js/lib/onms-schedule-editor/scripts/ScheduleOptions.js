import ClockMode from './ClockMode';
import Weekdays from './Weekdays';
import Types from './Types';
import Time from './Time';
import CronDefinition from './CronDefintion';
import WeekdayOptions from './WeekdayOptions';
import CustomParser from './parsers/CustomParser';
import DailyParser from './parsers/DailyParser';
import DayOfMonthParser from './parsers/DayOfMonthParser';
import ContextError from './ContextError';
import Intervals from './Intervals';

export default class ScheduleOptions {

    /* eslint-disable complexity */
    constructor(input) {
        const options = typeof input === 'undefined' ? {} : input;

        // Pre populate Values
        this.clockMode = options.clockMode || ClockMode.FULL_CLOCK_SYSTEM;
        if (this.clockMode !== ClockMode.HALF_CLOCK_SYSTEM && this.clockMode !== ClockMode.FULL_CLOCK_SYSTEM) {
            throw new Error('Provided clock mode not supported')
        }

        // Daily
        this.type = options.type || Types.DAILY;
        this.interval = typeof options.interval !== 'undefined' ? options.interval : 0;
        this.at = options.at || new Time({hours: 0, minutes: 0});
        this.from = options.from || new Time({hours: 0, minutes: 0});
        this.to = options.to || new Time({hours: 23, minutes: 0});

        // Days per Week
        this.daysOfWeek = options.daysOfWeek || {};

        // Days per Month
        this.dayOfMonth = options.dayOfMonth || '1'; // 1st day
        this.weekOfMonth = options.dayOfMonth || '1';
        this.dayOfWeek = options.dayOfWeek || String(Weekdays.Sunday.id);
        this.dayOfMonthToggle = options.dayOfMonthToggle || 'dayOfMonth';

        // Custom
        this.cronExpression = options.cronExpression || '0 0/5 * * * ?';

        // Enable debugging?
        this.showDebugOptions = options.showDebugOptions || false;

        // Ensure each time is actually a Time object
        if (!(this.at instanceof Time)) {
            this.at = new Time(this.at);
        }
        if (!(this.from instanceof Time)) {
            this.from = new Time(this.from);
        }
        if (!(this.to instanceof Time)) {
            this.to = new Time(this.to);
        }

        // Enforce the right clock mode
        this.at.convert(this.clockMode);
        this.from.convert(this.clockMode);
        this.to.convert(this.clockMode);

        // Enforce disabling Minutes
        this.from.options.disableMinutes = true;
        this.to.options.disableMinutes = true;

        // Enforce correct type
        if (!(this.daysOfWeek instanceof WeekdayOptions)) {
            this.daysOfWeek = new WeekdayOptions(this.daysOfWeek);
        }
        this.interval = String(this.interval);
    }

    getSelectedWeekdays() {
        const selectedWeekdays = Object.keys(this.daysOfWeek)
            .filter(function(key) {
                return this.daysOfWeek[key] === true;
            }, this)
            .map(function(key) {
                return key.substr(0, 3).toUpperCase();
            });
        return selectedWeekdays;
    }

    getCronExpression() {

        // Validate before actually returning the expression
        this.validate();

        // In case of a custom expression, just use it directly
        if (this.type === Types.CUSTOM) {
            return this.cronExpression;
        }

        // Otherwise, re Populate a cron definition
        const cron = new CronDefinition({seconds: 0, minutes: 0, hours: 0, dayOfMonth: '*', month: '*', dayOfWeek: '?'});

        // Daily Calculation
        if (this.type === Types.DAILY || this.type === Types.DAYS_PER_WEEK) {
            const interval = this.interval;
            const at = new Time(this.at, ClockMode.FULL_CLOCK_SYSTEM);
            const from = new Time(this.from, ClockMode.FULL_CLOCK_SYSTEM);
            const to = new Time(this.to, ClockMode.FULL_CLOCK_SYSTEM);

            if (interval === '0') { // Only once per day
                cron.hours = at.hours;
                cron.minutes = at.minutes;
            } else if (interval === Intervals.EVERY_HOUR) { // Every hours
                const hours = from.hours === to.hours ? from.hours : (from.hours + '-' + to.hours);
                cron.hours = hours;
                cron.minutes = 0;
            } else if (interval % 60 === 0) { // every 2 or 3 hours
                const hours = from.hours + '-' + to.hours;
                cron.hours = hours + '/' + (interval / 60);
                cron.minutes = 0;
            } else { // every n minutes
                if (from.hours === to.hours) {
                    cron.hours = from.hours;
                    cron.minutes = 0;
                } else {
                    // In case we defined an hours range, we decrease the end range by 1
                    // This is necessary, because the cron expression "0 0/30 1-2 * * ?"
                    // would fire at 1:00, 1:30, 2:00 and 2:30 every day.
                    // By decreasing the hour by 1, it will fire at 1:00 and 1:30 instead.
                    cron.hours = from.hours + '-' + (to.hours - 1);
                    cron.minutes = '0/' + interval;
                }
            }
        }

        // Update dayOfWeek if we are in day of week mode
        if (this.type === Types.DAYS_PER_WEEK) {
            cron.dayOfWeek = this.getSelectedWeekdays().join(',');
        }
        if (this.type === Types.DAYS_PER_MONTH) {
            const at = new Time(this.at, ClockMode.FULL_CLOCK_SYSTEM);
            cron.minutes = at.minutes;
            cron.hours = at.hours;
            if (this.dayOfMonthToggle === 'dayOfMonth') {
                cron.dayOfMonth = this.dayOfMonth;
            } else {
                cron.dayOfWeek = this.dayOfWeek + (this.weekOfMonth !== 'L' ? '#' : '') + this.weekOfMonth;
            }
        }

        // Due to a quartz limitation either cron.dayOfMonth or cron.dayOfWeek must be '?'
        // Source: http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html (Bottom of the page)
        //
        // As dayOfWeek is '?' by default if not defined, we set dayOfMonth to ? if dayOfWeek is set
        if (this.type === Types.DAYS_PER_WEEK || this.type === Types.DAYS_PER_MONTH && cron.dayOfWeek !== '?') {
            cron.dayOfMonth = '?';
        }

        return cron.asCronExpression();
    }

    validate() {
        if (this.type === Types.CUSTOM) {
            const items = this.cronExpression.trim().split(' ');
            if (items.length < 6 || items.length > 7) {
                throw new ContextError('cronExpression', 'Invalid cron expression');
            }
        }

        // Ensure that we actually have set a day
        if (this.type === Types.DAYS_PER_WEEK) {
            if (this.type === Types.DAYS_PER_WEEK && this.getSelectedWeekdays().length === 0) {
                throw new ContextError('dayOfWeek', 'Please select at least one day');
            }
        }

        // If there is an interval, ensure from is before to, otherwise bail
        if ((this.type === Types.DAILY || this.type === Types.DAYS_PER_WEEK) && this.interval !== '0') {
            if (this.to.isBefore(this.from)) {
                throw new ContextError('to', 'To time must be equal or after from time');
            }
        }
    }

    isValid() {
        return typeof this.errors === 'undefined' || Object.keys(this.errors).length === 0;
    }

    static createFrom(input) {
       const parsers = [
            new DailyParser(),
            new DayOfMonthParser(),
            new CustomParser() // If nothing else was able to parse it, we make it a custom expression
        ];
        for (let i = 0; i < parsers.length; i++) {
            if (parsers[i].canParse(input) === true) {
                const options = parsers[i].parse(input);
                return options;
            }
        }
        // This is technically unreachable code
        // but to make eslint happy, we have to put this in
        return new CustomParser().parse(input);
    }
}
