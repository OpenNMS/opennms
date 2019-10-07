import DailyParser from "../../../../main/assets/js/lib/onms-schedule-editor/scripts/parsers/DailyParser";
import Intervals from "../../../../main/assets/js/lib/onms-schedule-editor/scripts/Intervals";

test('Verify unsupported minutes interval', () => {
    const supportedMinutes = Intervals.Minutes;
    for (let i = 1; i < 60; i++) {
        const canParse = new DailyParser().canParse('0 0/' + String(i) + ' 1-10 * * ?');
        const supported = supportedMinutes.indexOf(String(i)) >= 0;
        expect(canParse).toBe(supported);
    }
});