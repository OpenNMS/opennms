import ClockMode from './ClockMode';

export default class Time {
    constructor (input, clockMode) {
        const newInput = input || {};
        this.hours = typeof newInput.hours === 'undefined' ? 12 : parseInt(newInput.hours, 10);
        this.minutes = typeof newInput.minutes === 'undefined' ? 0 : parseInt(newInput.minutes, 10);
        this.suffix = newInput.suffix || null;
        this.mode = this.suffix === null ? ClockMode.FULL_CLOCK_SYSTEM : ClockMode.HALF_CLOCK_SYSTEM;

        // Reset suffix if 24 clock mode
        if (this.mode === ClockMode.FULL_CLOCK_SYSTEM) {
            this.suffix = null;
        }

        // Additional options for rendering
        this.options = newInput.options || {};
        this.verify();

        // If a clockMode is defined, automatically convert it
        if (clockMode === ClockMode.FULL_CLOCK_SYSTEM || clockMode === ClockMode.HALF_CLOCK_SYSTEM) {
            this.convert(clockMode);
        }
    }

    verify() {
        if (this.mode === ClockMode.HALF_CLOCK_SYSTEM) {
            if (this.suffix !== 'am' && this.suffix !== 'pm') {
                throw new Error('Clock suffix, must be either \'am\' or \'pm\', but was \'' + this.suffix + '\'');
            }
            if (this.hours <= 0 || this.hours > 12) {
                throw new Error('Hours must be between [1-12] but was ' + this.hours);
            }
        }
        if (this.mode === ClockMode.FULL_CLOCK_SYSTEM) {
            if (this.hours < 0 || this.hours > 23) {
                throw new Error('Hours must be between [0-23] but was ' + this.hours);
            }
        }
        if (this.minutes < 0 || this.minutes > 59) {
            throw new Error('Minutes must be between [0-59] but was ' + this.minutes);
        }
    }

    getMinutesOfDay() {
        const thisTime = new Time(this, ClockMode.FULL_CLOCK_SYSTEM);
        return thisTime.hours * 60 + thisTime.minutes;
    }

    isBefore(other) {
        if (!(other instanceof Time)) {
            throw new Error('Other object must be of type Time, but was of type ' + typeof other);
        }
        const minutesOfDay = this.getMinutesOfDay();
        const otherMinutesOfDay = other.getMinutesOfDay();
        const before = minutesOfDay - otherMinutesOfDay < 0;
        return before;
    }

    convert(newMode) {
        if (newMode !== ClockMode.FULL_CLOCK_SYSTEM && newMode !== ClockMode.HALF_CLOCK_SYSTEM) {
            throw new Error('Unknown Clock Mode \'' + newMode + '\'');
        }

        // If conversion is required
        if (this.mode !== newMode) {
            // AM/PM Clock -> 24 Hours Clock
            if (newMode === ClockMode.FULL_CLOCK_SYSTEM) {
                // 12:00 am is 00:00
                if (this.hours === 12 && this.suffix === 'am') {
                    this.hours = 0;
                }
                // anything pm requires a 12 hours offset
                if (this.suffix === 'pm' && this.hours >= 1 && this.hours < 12) {
                    this.hours += 12;
                }
                this.suffix = null;
            }

            // 24 Hours Clock -> AM/PM Clock
            if (newMode === ClockMode.HALF_CLOCK_SYSTEM) {
                // 00:00 is 12:00 am
                if (this.hours === 0) {
                    this.hours = 12;
                    this.suffix = 'am';
                }
                // anything between 1:00 and 11:00 am (we consider hours only here) are 'am'
                else if (this.hours >= 1 && this.hours <= 11) {
                    this.suffix = 'am';
                }
                // 12:00 is 12:00 pm
                else if (this.hours === 12) {
                    this.suffix = 'pm';
                }
                // anything > 12, requires a -12 and is pm
                else if (this.hours > 12) {
                    this.hours -= 12;
                    this.suffix = 'pm';
                }
            }
            this.mode = newMode;
        }
        return this;
    }

    toString() {
        return ('00' + this.hours).slice(-2) + ':' + ('00' + this.minutes).slice(-2) + (this.mode === ClockMode.HALF_CLOCK_SYSTEM ? (' ' + this.suffix) : '');
    }
}
