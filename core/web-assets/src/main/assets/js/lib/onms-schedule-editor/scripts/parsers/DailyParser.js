import Types from '../Types';
import Time from '../Time';
import Range from '../Range';
import CronDefinition from '../CronDefintion';
import WeekdayOptions from '../WeekdayOptions';
import ScheduleOptions from '../ScheduleOptions';

export default class DailyParser {

    canParse(input) {
        const cron = CronDefinition.createFrom(input);
        const canParse = cron.year === undefined
            && cron.seconds === '0'
            && (cron.isConcreteMinutes() || (cron.isMinutesInterval() && cron.minutes.indexOf('0') === 0))
            && (cron.isConcreteHours() || cron.isHoursInterval() || cron.isHoursRange())
            && (cron.dayOfMonth === '*')
            && (cron.month === '*')
            && (cron.dayOfWeek === '?' || WeekdayOptions.createFrom(cron.dayOfWeek).getSelectedWeekdays().length > 0);
        return canParse;
    }

    parse(input) {
        const cron = CronDefinition.createFrom(input);
        const options = new ScheduleOptions({ type: Types.DAILY });
        options.interval = cron.interval;

        // Parse at, from and to
        if (options.interval === '0') {
            options.at = new Time({ hours: cron.hours, minutes: cron.minutes });
        } else {
            const range = new Range(cron.hours);

            // add one hour offset in case of minutes interval
            if ((options.interval % 60 !== 0) && cron.isHoursRange()) {
                range.end = parseInt(range.end, 10) + 1;
            }
            options.from = new Time({hours: range.start, minutes: 0});
            options.to = new Time({hours: range.end, minutes: 0});
        }

        // Check if days of week are set
        const weekdayOptions = WeekdayOptions.createFrom(cron.dayOfWeek);
        if (weekdayOptions.getSelectedWeekdays().length > 0) {
            options.type = Types.DAYS_PER_WEEK;
            options.daysOfWeek = weekdayOptions;
        }
        return options;
    }
}
