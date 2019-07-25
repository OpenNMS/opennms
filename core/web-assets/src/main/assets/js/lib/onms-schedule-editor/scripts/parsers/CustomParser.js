import Types from '../Types';
import ScheduleOptions from '../ScheduleOptions';

// If all fails, the custom parser will take it
export default class CustomParser {
    canParse(input) {
        return true;
    }

    parse(input) {
        return new ScheduleOptions({ type: Types.CUSTOM, cronExpression: input });
    }
}