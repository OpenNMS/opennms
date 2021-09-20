import WeekdayOptions from '../../../main/assets/js/lib/onms-schedule-editor/scripts/WeekdayOptions';
import Weekdays from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Weekdays';

test('Verify construct empty', () => {
    const options = new WeekdayOptions();
    const expected = {
        Sunday: false,
        Monday: false,
        Tuesday: false,
        Wednesday: false,
        Thursday: false,
        Friday: false,
        Saturday: false
    };
    expect(options).toEqual(expected);
});

test('Verify creating from array', () => {
    let options = new WeekdayOptions([ Weekdays.Tuesday, Weekdays.Friday ]);
    const expected = {
        Sunday: false,
        Monday: false,
        Tuesday: true,
        Wednesday: false,
        Thursday: false,
        Friday: true,
        Saturday: false
    };
    expect(options).toEqual(expected);

    const options2 = new WeekdayOptions([Weekdays.Tuesday.label, Weekdays.Friday.label ]);
    expect(options2).toEqual(expected);
});

test('Verify createFrom(String) with unsupported expression', () => {
    let options = WeekdayOptions.createFrom('');
    const expected = {
        Sunday: false,
        Monday: false,
        Tuesday: false,
        Wednesday: false,
        Thursday: false,
        Friday: false,
        Saturday: false
    };
    expect(options).toEqual(expected);

    options = WeekdayOptions.createFrom('*');
    expect(options).toEqual(expected);

    options = WeekdayOptions.createFrom('MON-FRI');
    expect(options).toEqual(expected);

    options = WeekdayOptions.createFrom('?');
    expect(options).toEqual(expected);
});

test('Verify createFrom(String) with supported expression', () => {
    const expected = {
        Sunday: true,
        Monday: true,
        Tuesday: true,
        Wednesday: true,
        Thursday: true,
        Friday: true,
        Saturday: true
    };
    let options = WeekdayOptions.createFrom('SUN,MON,TUE,WED,THU,FRI,SAT');
    expect(options).toEqual(expected);

    // Now verify partial parsing
    expected.Sunday = false;
    expected.Wednesday = false;
    expected.Saturday = false;

    options = WeekdayOptions.createFrom('MON,TUE,THU,FRI');
    expect(options).toEqual(expected);

    // Now try random order
    options = WeekdayOptions.createFrom('FRI,THU,MON,TUE');
    expect(options).toEqual(expected);
});

test('Verify getSelectedWeekdays', () => {
    expect(new WeekdayOptions().getSelectedWeekdays()).toEqual([]);
    expect(new WeekdayOptions([Weekdays.Monday, Weekdays.Sunday]).getSelectedWeekdays()).toEqual([ Weekdays.Sunday, Weekdays.Monday ]);
    expect(new WeekdayOptions([Weekdays.Monday.label, Weekdays.Sunday.label]).getSelectedWeekdays()).toEqual([ Weekdays.Sunday, Weekdays.Monday ]);
});
