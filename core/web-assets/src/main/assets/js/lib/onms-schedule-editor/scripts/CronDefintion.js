export default class CronDefinition {
    constructor(input) {
        let options = typeof input !== 'undefined' ? input : {};
        this.seconds = typeof options.seconds !== 'undefined' ? options.seconds : {};
        this.minutes = typeof options.minutes !== 'undefined' ? options.minutes : {};
        this.hours = typeof options.hours !== 'undefined' ? options.hours : {};
        this.dayOfMonth = typeof options.dayOfMonth !== 'undefined' ? options.dayOfMonth : {};
        this.month = typeof options.month !== 'undefined' ? options.month : {};
        this.dayOfWeek = typeof options.dayOfWeek !== 'undefined' ? options.dayOfWeek : {};
    }

    asCronExpression() {
        const array = [
            this.seconds, this.minutes, this.hours, this.dayOfMonth, this.month, this.dayOfWeek
        ];
        return array.join(' ');
    }

    get interval() {
        if (this.isMinutesInterval()) {
            return this.minutes.substr(this.minutes.indexOf('/') + 1);
        }
        if (this.isHoursInterval()) {
            const hoursInterval = this.hours.substr(this.hours.indexOf('/') + 1);
            return String(parseInt(hoursInterval, 10) * 60);
        }

        // If there is no range, it is not possible to determine
        // anymore if the user originally selected every hour, every 30, 15, 10 or 5 minutes
        // However if the hours are a range, we must set the interval to anything != 0
        if (this.isHoursRange() >= 1) {
            return '60'; // fallback to every hour
        }
        return '0';
    }

    isConcreteMinutes() {
        return !this.isMinutesInterval()
            && !this.isMinutesRange()
            && !this.__contains(this.minutes, ',')
            && parseInt(this.minutes, 10) >= 0;
    }

    isMinutesInterval() {
        return this.__contains(this.minutes, '/');
    }

    isMinutesRange() {
        return this.__contains(this.minutes, '-');
    }

    isConcreteHours() {
        return !this.isHoursInterval()
            && !this.isHoursRange()
            && !this.__contains(this.hours, ',')
            && parseInt(this.hours, 10) >= 0;
    }

    isHoursRange() {
        return this.__contains(this.hours, '-');
    }

    isHoursInterval() {
        return this.__contains(this.hours, '/');
    }

    __contains(input, findMe) {
        if (typeof input === 'string') {
            return input.indexOf(findMe) >= 1;
        }
        return false;
    }

    static createFrom(input) {
        if (typeof input !== 'string' || input === '' || input.trim() === '') {
            throw new Error('Cannot parse provided expression. Please make sure it is a valid cron expression');
        }
        const cronExpression = input.trim();
        const fields = cronExpression.split(' ');
        if (fields.length < 6 || fields.length > 7) {
            throw new Error('Invalid Cron Expression. Expected field count is 6 or 7, but got ' + fields.length);
        }
        // Parse it
        const cron = new CronDefinition({
            seconds: fields[0],
            minutes: fields[1],
            hours: fields[2],
            dayOfMonth: fields[3],
            month: fields[4],
            dayOfWeek: fields[5]
        });
        if (fields.length === 7) {
            cron.year = fields[6];
        }
        return cron;
    }
}
