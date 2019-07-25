import Types from '../Types';
import Time from '../Time';
import CronDefinition from '../CronDefintion';
import ScheduleOptions from '../ScheduleOptions';

export default class DayOfMonthParser {
    constructor() {
        this.regExp = new RegExp('[1-7]#[1,2,3]|L');
    }

    canParse(input) {
        const cron = CronDefinition.createFrom(input);
        const canParse = cron.year === undefined
            && cron.seconds === '0'
            && cron.isConcreteMinutes()
            && cron.isConcreteHours()
            && cron.month === '*'
            && (cron.dayOfMonth === '*' || cron.dayOfMonth === 'L' || parseInt(cron.dayOfMonth, 10) >= 1)
            && cron.dayOfMonth.indexOf(',') === -1
            && cron.dayOfMonth.indexOf('-') === -1
            && cron.dayOfMonth.indexOf('/') === -1
            && (cron.dayOfWeek == '?' || this.regExp.test(cron.dayOfWeek));
        return canParse;
    }

    parse(input) {
        const cron = CronDefinition.createFrom(input);
        const options = new ScheduleOptions({
            type: Types.DAYS_PER_MONTH,
            at: new Time({ hours: cron.hours, minutes: cron.minutes })
        });

        // Determine the toggle
        if (cron.dayOfMonth === '*') {
            options.dayOfMonthToggle = 'dayOfWeek';
        } else {
            options.dayOfMonthToggle = 'dayOfMonth';
        }

        // Set the values according ot the toggle
        if (options.dayOfMonthToggle === 'dayOfMonth') {
            options.dayOfMonth = cron.dayOfMonth;
        } else {
            options.weekOfMonth = cron.dayOfWeek.substr(-1);
            options.dayOfWeek = cron.dayOfWeek.substr(0, 1);
        }
        return options;
    }
}