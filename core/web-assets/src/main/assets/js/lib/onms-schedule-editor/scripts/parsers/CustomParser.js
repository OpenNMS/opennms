import Types from '../Types';
import ScheduleOptions from '../ScheduleOptions';

/**
 * If all fails, the custom parser will handle any cron expression as a Custom ScheduleOptions.
 */
export default class CustomParser {
    canParse(input) {
        return true;
    }

    parse(input) {
        return new ScheduleOptions({ type: Types.CUSTOM, cronExpression: input });
    }
}