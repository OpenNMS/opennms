import ScheduleOptions from '../../../main/assets/js/lib/onms-schedule-editor/scripts/ScheduleOptions';
import Types from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Types';
import Time from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Time';
import Weekdays from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Weekdays';
import WeekdayOptions from '../../../main/assets/js/lib/onms-schedule-editor/scripts/WeekdayOptions';
import ClockMode from '../../../main/assets/js/lib/onms-schedule-editor/scripts/ClockMode';
import Intervals from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Intervals';

describe('Verify construction', () => {
    test('Verify defaults', () => {
        const options = new ScheduleOptions();
        expect(options.type).toBe(Types.DAILY);
        expect(options.interval).toBe('0');
        expect(options.at).toEqual(new Time({hours: 0, minutes: 0}));
        expect(options.from).toEqual(new Time({hours: 0, minutes: 0, options: {disableMinutes: true}}));
        expect(options.to).toEqual(new Time({hours: 23, minutes: 0, options: {disableMinutes: true}}));
    });

    test('Verify converts time to objects', () => {
        const options = new ScheduleOptions({
            at: {hours: 9, minutes: 10},
            from: {hours: 10, minutes: 11},
            to: {hours: 12, minutes: 13}
        });

        expect(options.at).toBeInstanceOf(Time);
        expect(options.from).toBeInstanceOf(Time);
        expect(options.to).toBeInstanceOf(Time);
    });

    test('Verify daysOfWeek fully populated even if set', () => {
        const expectedWeekdaysOptions = new WeekdayOptions([ Weekdays.Monday, Weekdays.Friday ]);
        const options = new ScheduleOptions({
            daysOfWeek: {
                Monday: true,
                Friday: true
            }
        });
        expect(options.daysOfWeek).toEqual(expectedWeekdaysOptions);
    });
});

describe('Verify daily cron generation', () => {
    test('Verify defaults', () => {
        let options = new ScheduleOptions();
        expect(options.getCronExpression()).toBe('0 0 0 * * ?');
    });

    test('Verify once per day', () => {
        let options = new ScheduleOptions();
        options.at = new Time({hours: 10, minutes: 15});
        expect(options.getCronExpression()).toBe('0 15 10 * * ?');
    });

    describe('Verify every 3 hours', () => {
        test('Verify from = to', () => {
            let options = new ScheduleOptions();
            options.from = new Time({ hours:1, minutes: 1 });
            options.to = new Time(options.from);
            options.interval = 180;
            expect(options.getCronExpression()).toBe('0 0 1-1/3 * * ?');
        });

        test('Verify from != to and range != 3', () => {
            let options = new ScheduleOptions({interval: 180, from: new Time({hours: 10}), to: new Time({hours: 12})});
            expect(options.getCronExpression()).toBe('0 0 10-12/3 * * ?');
        });

        test('Verify from != to and range > 3', () => {
            let options = new ScheduleOptions({interval: 180, from: new Time({hours: 10}), to: new Time({hours: 16})});
            expect(options.getCronExpression()).toBe('0 0 10-16/3 * * ?');
        });

        test('Verify from after to', () => {
            let options = new ScheduleOptions({interval: 180, from: new Time({hours: 10}), to: new Time({hours: 8})});
            expect(() => options.getCronExpression()).toThrow();
        });
    });

    describe('Verify every 2 hours', () => {
        test('Verify from = to', () => {
            let options = new ScheduleOptions();
            options.from = new Time({ hours: 1, minutes: 1 });
            options.to = new Time(options.from);
            options.interval = 120;
            expect(options.getCronExpression()).toBe('0 0 1-1/2 * * ?');
        });

        test('Verify from != to and range != 2', () => {
            let options = new ScheduleOptions({interval: 120, from: new Time({hours: 10}), to: new Time({hours: 12})});
            expect(options.getCronExpression()).toBe('0 0 10-12/2 * * ?');
        });

        test('Verify from != to and range > 2', () => {
            let options = new ScheduleOptions({interval: 120, from: new Time({hours: 10}), to: new Time({hours: 16})});
            expect(options.getCronExpression()).toBe('0 0 10-16/2 * * ?');
        });

        test('Verify from after to', () => {
            let options = new ScheduleOptions({interval: 120, from: new Time({hours: 10}), to: new Time({hours: 8})});
            expect(() => options.getCronExpression()).toThrow();
        });
    });

    describe('Verify every hour', () => {
        test('Verify from = to', () => {
            let options = new ScheduleOptions({ interval: 60 });
            options.from = new Time({hours:1, minutes: 1});
            options.to = new Time(options.from);
            expect(options.getCronExpression()).toBe('0 0 1 * * ?');
        });

        test('Verify from != to and range = 1', () => {
            let options = new ScheduleOptions({interval: 60, from: new Time({hours: 10}), to: new Time({hours: 11})});
            expect(options.getCronExpression()).toBe('0 0 10-11 * * ?');
        });

        test('Verify from != to and range > 1', () => {
            let options = new ScheduleOptions({interval: 60, from: new Time({hours: 10}), to: new Time({hours: 16})});
            expect(options.getCronExpression()).toBe('0 0 10-16 * * ?');
        });

        test('Verify from after to', () => {
            let options = new ScheduleOptions({interval: 60, from: new Time({hours: 10}), to: new Time({hours: 8})});
            expect(() => options.getCronExpression()).toThrow();
        });
    });

    describe('Verify every 30 minutes', () => {
        test('Verify from = to', () => {
            let options = new ScheduleOptions({interval: 30, from: new Time({hours: 1}), to: new Time({hours: 1})});
            expect(options.getCronExpression()).toBe('0 0 1 * * ?');
        });

        test('Verify from != to and range = 1', () => {
            let options = new ScheduleOptions({interval: 30, from: new Time({hours: 10}), to: new Time({hours: 11})});
            expect(options.getCronExpression()).toBe('0 0/30 10-10 * * ?');
        });

        test('Verify from != to and range > 1', () => {
            let options = new ScheduleOptions({interval: 30, from: new Time({hours: 10}), to: new Time({hours: 16})});
            expect(options.getCronExpression()).toBe('0 0/30 10-15 * * ?');
        });

        test('Verify from after to', () => {
            let options = new ScheduleOptions({interval: 30, from: new Time({hours: 10}), to: new Time({hours: 8})});
            expect(() => options.getCronExpression()).toThrow();
        });
    });

    describe('Verify every 15 minutes', () => {
        test('Verify from = to', () => {
            let options = new ScheduleOptions({interval: 15, from: new Time({hours: 1}), to: new Time({hours: 1})});
            expect(options.getCronExpression()).toBe('0 0 1 * * ?');
        });

        test('Verify from != to and range = 1', () => {
            let options = new ScheduleOptions({interval: 15, from: new Time({hours: 10}), to: new Time({hours: 11})});
            expect(options.getCronExpression()).toBe('0 0/15 10-10 * * ?');
        });

        test('Verify from != to and range > 1', () => {
            let options = new ScheduleOptions({interval: 15, from: new Time({hours: 10}), to: new Time({hours: 16})});
            expect(options.getCronExpression()).toBe('0 0/15 10-15 * * ?');
        });

        test('Verify from after to', () => {
            let options = new ScheduleOptions({interval: 15, from: new Time({hours: 10}), to: new Time({hours: 8})});
            expect(() => options.getCronExpression()).toThrow();
        });
    });

    describe('Verify every 10 minutes', () => {
        test('Verify from = to', () => {
            let options = new ScheduleOptions({interval: 10, from: new Time({hours: 1}), to: new Time({hours: 1})});
            expect(options.getCronExpression()).toBe('0 0 1 * * ?');
        });

        test('Verify from != to and range = 1', () => {
            let options = new ScheduleOptions({interval: 10, from: new Time({hours: 10}), to: new Time({hours: 11})});
            expect(options.getCronExpression()).toBe('0 0/10 10-10 * * ?');
        });

        test('Verify from != to and range > 1', () => {
            let options = new ScheduleOptions({interval: 10, from: new Time({hours: 10}), to: new Time({hours: 16})});
            expect(options.getCronExpression()).toBe('0 0/10 10-15 * * ?');
        });

        test('Verify from after to', () => {
            let options = new ScheduleOptions({interval: 10, from: new Time({hours: 10}), to: new Time({hours: 8})});
            expect(() => options.getCronExpression()).toThrow();
        });
    });

    describe('Verify every 5 minutes', () => {
        test('Verify from = to', () => {
            let options = new ScheduleOptions({interval: 10, from: new Time({hours: 1}), to: new Time({hours: 1})});
            expect(options.getCronExpression()).toBe('0 0 1 * * ?');
        });

        test('Verify from != to and range = 1', () => {
            let options = new ScheduleOptions({interval: 5, from: new Time({hours: 10}), to: new Time({hours: 11})});
            expect(options.getCronExpression()).toBe('0 0/5 10-10 * * ?');
        });

        test('Verify from != to and range > 1', () => {
            let options = new ScheduleOptions({interval: 5, from: new Time({hours: 10}), to: new Time({hours: 16})});
            expect(options.getCronExpression()).toBe('0 0/5 10-15 * * ?');
        });

        test('Verify from after to', () => {
            let options = new ScheduleOptions({interval: 5, from: new Time({hours: 10}), to: new Time({hours: 8})});
            expect(() => options.getCronExpression()).toThrow();
        });
    });
});

// Mostly the same as Daily, so we only Verify certain aspects of the day selection
describe('Verify days per week cron generation', () => {
    describe('Verify once per day (interval = 0)', () => {
        let options = new ScheduleOptions({type: Types.DAYS_PER_WEEK});

        test('Verify is failing by default', () => {
            const expectedWeekDayOptions = new WeekdayOptions();
            expect(options.daysOfWeek).toEqual(expectedWeekDayOptions);
            expect(() => options.getCronExpression()).toThrow();
        });

        test('Verify all weekdays selected', () => {
            options.daysOfWeek = new WeekdayOptions(Weekdays.all);
            expect(options.getCronExpression()).toBe('0 0 0 ? * SUN,MON,TUE,WED,THU,FRI,SAT');
        });

        test('Verify specific weekdays selected', () => {
            options.daysOfWeek = { Tuesday: true, Thursday: true };
            options.at = new Time({hours: 10, minutes: 15});
            expect(options.getCronExpression()).toBe('0 15 10 ? * TUE,THU');
        });
    });

    describe('Verify every 2 hours', () => {
        let options = new ScheduleOptions({interval: 120, type: Types.DAYS_PER_WEEK});

        test('Verify is failing by default', () => {
            const expectedWeekDayOptions = new WeekdayOptions();
            expect(options.daysOfWeek).toEqual(expectedWeekDayOptions);
            expect(() => options.getCronExpression()).toThrow();
        });

        test('Verify all weekdays selected', () => {
            options.daysOfWeek = new WeekdayOptions(Weekdays.all);
            options.from = new Time({hours: 1, minutes: 0});
            options.to = new Time({hours: 2, minutes: 0});
            expect(options.getCronExpression()).toBe('0 0 1-2/2 ? * SUN,MON,TUE,WED,THU,FRI,SAT');
        });

        test('Verify specific weekdays selected', () => {
            options.daysOfWeek = {Tuesday: true, Thursday: true};
            options.to = new Time({hours: 15, minutes: 0});
            options.from = new Time({hours: 13, minutes: 0});
            expect(options.getCronExpression()).toBe('0 0 13-15/2 ? * TUE,THU');
        });
    });

    describe('Verify every 15 minutes', () => {
        let options = new ScheduleOptions({interval: 15, type: Types.DAYS_PER_WEEK});

        test('Verify is failing by default', () => {
            const expectedWeekDayOptions = new WeekdayOptions();
            expect(options.daysOfWeek).toEqual(expectedWeekDayOptions);
            expect(() => options.getCronExpression()).toThrow();
        });

        test('Verify all weekdays selected', () => {
            options.daysOfWeek = new WeekdayOptions(Weekdays.all);
            options.from = new Time({hours: 1, minutes: 0});
            options.to = new Time({hours: 2, minutes: 0});
            expect(options.getCronExpression()).toBe('0 0/15 1-1 ? * SUN,MON,TUE,WED,THU,FRI,SAT');
        });

        test('Verify specific weekdays selected', () => {
            options.daysOfWeek = {Tuesday: true, Thursday: true};
            options.to = new Time({hours: 15, minutes: 0});
            options.from = new Time({hours: 13, minutes: 0});
            expect(options.getCronExpression()).toBe('0 0/15 13-14 ? * TUE,THU');
        });
    });
});

describe('Verify day per month cron generation', () => {
    let options = new ScheduleOptions({type: Types.DAYS_PER_MONTH, at: new Time({hours: 13, minutes: 25})});

    test('Verify defaults', () => {
        expect(options.getCronExpression()).toBe('0 25 13 1 * ?');
    });

    test('Verify each day of every month', () => {
        for (let i = 1; i <= 32; i++) {
            options.dayOfMonth = i;
            if (i === 32) {
                options.dayOfMonth = 'L';
            }
            expect(options.getCronExpression()).toBe('0 25 13 ' + options.dayOfMonth + ' * ?');
        }
    });

    describe('Verify specific weekday of every month', () => {
        let options = new ScheduleOptions({
            type: Types.DAYS_PER_MONTH,
            at: new Time({hours: 15, minutes: 35}),
            dayOfMonthToggle: 'dayOfWeek'
        });

        test('Verify defaults', () => {
            expect(new ScheduleOptions({type: Types.DAYS_PER_MONTH}).getCronExpression()).toBe('0 0 0 1 * ?'); // 1st of every month
        });

        test('Verify last friday', () => {
            options.weekOfMonth = 'L';
            options.dayOfWeek = Weekdays.Friday.id;
            expect(options.getCronExpression()).toBe('0 35 15 ? * 6L');
        });

        test('Verify third friday', () => {
            options.weekOfMonth = 3;
            options.dayOfWeek = Weekdays.Friday.id;
            expect(options.getCronExpression()).toBe('0 35 15 ? * 6#3');
        });

        test('Verify all iterations', () => {
            const weekIndicators = [1, 2, 3, 4, 'L'];

            for (let i = 0; i < weekIndicators.length; i++) {
                for (let a = 0; a < Weekdays.all.length; a++) {
                    options.weekOfMonth = weekIndicators[i];
                    options.dayOfWeek = Weekdays.all[a].id;

                    var expectedDayOfWeek = options.dayOfWeek + (options.weekOfMonth !== 'L' ? '#' : '') + options.weekOfMonth;
                    expect(options.getCronExpression()).toBe('0 35 15 ? * ' + expectedDayOfWeek);
                }
            }
        })
    });
});

describe('Verify global clock mode', () => {
    test('Verify invalid clock mode', () => {
        expect(() => new ScheduleOptions({clockMode: 'undefined'})).toThrow();
    });

    test('Verify converts properly', () => {
        let options = new ScheduleOptions({
            at: new Time({hours: 23, minutes: 17}),
            from: new Time({hours: 22, minutes: 16}),
            to: new Time({hours: 21, minutes: 15}),
            clockMode: ClockMode.HALF_CLOCK_SYSTEM
        });

        expect(options.at.hours).toBe(11);
        expect(options.at.minutes).toBe(17);
        expect(options.at.suffix).toBe('pm');

        expect(options.from.hours).toBe(10);
        expect(options.from.minutes).toBe(16);
        expect(options.from.suffix).toBe('pm');

        expect(options.to.hours).toBe(9);
        expect(options.to.minutes).toBe(15);
        expect(options.to.suffix).toBe('pm');
    });
});

describe('Verify custom cron expression', () => {
    test('Verify defaults', () => {
        let options = new ScheduleOptions({type: Types.CUSTOM});
        expect(options.getCronExpression()).toBe('0 0/5 * * * ?')
    });

    test('Verify including the optional year will also work', () => {
        let options = new ScheduleOptions({type: Types.CUSTOM, cronExpression: '0 0/5 * * * ? ?'});
        expect(options.getCronExpression()).toBe(options.cronExpression);
    });

    test('Verify invalid expression', () => {
        let options = new ScheduleOptions({type: Types.CUSTOM, cronExpression: '0 0/5 * * * ? ? xxx'});
        expect(() => options.getCronExpression()).toThrow();
    });
});

describe('Verify parsing cron expression', () => {
    describe('Verify daily', () => {
        test('Verify once per day', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 15 10 * * ?');

            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe('0');
            expect(parsedOptions.at).toEqual(new Time({hours: 10, minutes: 15}));
        });

        test('Verify every 3 hours with from = to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0 1-1/3 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_3_HOURS);
            expect(parsedOptions.from).toEqual(new Time({hours: 1, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 1, minutes: 0}));
        });

        test('Verify every 3 hours with from < to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0 15-20/3 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_3_HOURS);
            expect(parsedOptions.from).toEqual(new Time({hours: 15, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 20, minutes: 0}));
        });

        test('Verify every 2 hours with from = to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0 10-10/2 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_2_HOURS);
            expect(parsedOptions.from).toEqual(new Time({hours: 10, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 10, minutes: 0}));
        });

        test('Verify every 2 hours with from < to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0 17-21/2 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_2_HOURS);
            expect(parsedOptions.from).toEqual(new Time({hours: 17, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 21, minutes: 0}));
        });

        test('Verify every hour with from = to', () => {
            // This should fall back to once per day
            const parsedOptions = ScheduleOptions.createFrom('0 0 5 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe('0');
            expect(parsedOptions.at).toEqual(new Time({hours: 5, minutes: 0}));
        });

        test('Verify every hour with from < to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0 3-23 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_HOUR);
            expect(parsedOptions.from).toEqual(new Time({hours: 3, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 23, minutes: 0}));
        });

        test('Verify every 30 minutes with from < to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0/30 4-21 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_30_MINUTES);
            expect(parsedOptions.from).toEqual(new Time({hours: 4, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 22, minutes: 0}));
        });

        test('Verify every 15 minutes with from < to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0/15 4-4 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_15_MINUTES);
            expect(parsedOptions.from).toEqual(new Time({hours: 4, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 5, minutes: 0}));
        });

        test('Verify every 10 minutes with from < to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0/10 4-4 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_10_MINUTES);
            expect(parsedOptions.from).toEqual(new Time({hours: 4, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 5, minutes: 0}));
        });

        test('Verify every 5 minutes with from < to', () => {
            const parsedOptions = ScheduleOptions.createFrom('0 0/5 4-4 * * ?');
            expect(parsedOptions.type).toBe(Types.DAILY);
            expect(parsedOptions.interval).toBe(Intervals.EVERY_5_MINUTES);
            expect(parsedOptions.from).toEqual(new Time({hours: 4, minutes: 0}));
            expect(parsedOptions.to).toEqual(new Time({hours: 5, minutes: 0}));
        });
    });

    // Mostly identical to daily, so we focus on parsing the 'Day of week' field
    describe('Verify Days per Week', () => {
        test('Verify parsing all days per week', () => {
            let options = ScheduleOptions.createFrom('0 0/15 4-4 ? * SUN,MON,TUE,WED,THU,FRI,SAT');
            let expectedDaysOfWeek = {
                Sunday: true,
                Monday: true,
                Tuesday: true,
                Wednesday: true,
                Thursday: true,
                Friday: true,
                Saturday: true
            };
            expect(options.type).toBe(Types.DAYS_PER_WEEK);
            expect(options.interval).toBe(Intervals.EVERY_15_MINUTES);
            expect(options.from).toEqual(new Time({hours: 4, minutes: 0}));
            expect(options.to).toEqual(new Time({hours: 5, minutes: 0}));
            expect(options.daysOfWeek).toEqual(expectedDaysOfWeek);
        });

        test('Verify parsing some days per week', () => {
            let options = ScheduleOptions.createFrom('0 0 4-8/2 ? * MON,WED,SAT');
            let expectedDaysOfWeek = {
                Sunday: false,
                Monday: true,
                Tuesday: false,
                Wednesday: true,
                Thursday: false,
                Friday: false,
                Saturday: true
            };
            expect(options.type).toBe(Types.DAYS_PER_WEEK);
            expect(options.interval).toBe(Intervals.EVERY_2_HOURS);
            expect(options.from).toEqual(new Time({hours: 4, minutes: 0}));
            expect(options.to).toEqual(new Time({hours: 8, minutes: 0}));
            expect(options.daysOfWeek).toEqual(expectedDaysOfWeek);
        });
    });

    describe('Verify Days Per Month', () => {
        test('Verify n th of month', () => {
            for (let i=1; i<=32; i++) {
                const options = ScheduleOptions.createFrom('0 15 10 ' + (i === 32 ? 'L' : i) + ' * ?');
                expect(options.type).toBe(Types.DAYS_PER_MONTH);
                expect(options.at).toEqual(new Time({ hours: 10, minutes: 15 }));
                expect(options.dayOfMonthToggle).toBe('dayOfMonth');
                expect(options.dayOfMonth).toBe('' + (i === 32 ? 'L' : i));
            }
        });

        test('Verify first Sunday of month', () => {
            const options = ScheduleOptions.createFrom('0 17 12 ? * 1#1');
            expect(options.type).toBe(Types.DAYS_PER_MONTH);
            expect(options.at).toEqual(new Time({ hours: 12, minutes: 17 }));
            expect(options.dayOfMonthToggle).toBe('dayOfWeek');
            expect(options.weekOfMonth).toBe('1');
            expect(options.dayOfWeek).toBe('1');
        });

        test('Verify last Friday of month', () => {
            const options = ScheduleOptions.createFrom('0 17 12 ? * 6L');
            expect(options.type).toBe(Types.DAYS_PER_MONTH);
            expect(options.at).toEqual(new Time({ hours: 12, minutes: 17 }));
            expect(options.dayOfMonthToggle).toBe('dayOfWeek');
            expect(options.weekOfMonth).toBe('L');
            expect(options.dayOfWeek).toBe('6');
        });

        test('Verify all iterations of month', () => {
            const weekIndicators = [1, 2, 3, 4, 'L'];

            for (let i = 0; i < weekIndicators.length; i++) {
                for (let a = 0; a < Weekdays.all; a++) {
                    const weekOfMonth = weekIndicators[i];
                    const dayOfWeek = Weekdays.all[a].id;
                    const dayOfWeekField = dayOfWeek + (weekOfMonth !== 'L' ? '#' : '') + weekOfMonth;
                    const cron = '0 15 10 ? * ' + dayOfWeekField;
                    const options = ScheduleOptions.createFrom(cron);

                    expect(options.type).toBe(Types.DAYS_PER_MONTH);
                    expect(options.at).toEqual(new Time({ hours: 10, minutes: 15 }));
                    expect(options.dayOfMonthToggle).toBe('dayOfWeek');
                    expect(options.weekOfMonth).toBe('' + weekOfMonth);
                    expect(options.dayOfWeek).toBe('' + dayOfWeek);
                }
            }
        });
    });

    // See Examples at http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html
    test('Verify various expressions', () => {
        expect(ScheduleOptions.createFrom('0 0 12 * * ?').type).toBe(Types.DAILY);
        expect(ScheduleOptions.createFrom('0 15 10 ? * *').type).toBe(Types.CUSTOM); // We expect * at month instead of a ?
        expect(ScheduleOptions.createFrom('0 15 10 * * ? *').type).toBe(Types.CUSTOM); // We only expect 6 fields
        expect(ScheduleOptions.createFrom('0 15 10 * * ? 2005').type).toBe(Types.CUSTOM); // We only expect 6 fields
        expect(ScheduleOptions.createFrom('0 * 14 * * ?').type).toBe(Types.CUSTOM); // We expect a number minutes
        expect(ScheduleOptions.createFrom('0 0/5 14 * * ?').type).toBe(Types.DAILY);
        expect(ScheduleOptions.createFrom('0 0/5 14,18 * * ?').type).toBe(Types.CUSTOM); // We expect a range for hours
        expect(ScheduleOptions.createFrom('0 0-5 14 * * ?').type).toBe(Types.CUSTOM); // We don't allow to set minutes ranges
        expect(ScheduleOptions.createFrom('0 10,44 14 ? 3 WED').type).toBe(Types.CUSTOM); // We only allow concrete minutes (or interval)
        expect(ScheduleOptions.createFrom('0 15 10 ? * MON-FRI').type).toBe(Types.CUSTOM); // day of week must be comma seperated field
        expect(ScheduleOptions.createFrom('0 15 10 15 * ?').type).toBe(Types.DAYS_PER_MONTH);
        expect(ScheduleOptions.createFrom('0 15 10 L * ?').type).toBe(Types.DAYS_PER_MONTH);
        expect(ScheduleOptions.createFrom('0 15 10 L-2 * ?').type).toBe(Types.CUSTOM); // We don't allow this
        expect(ScheduleOptions.createFrom('0 15 10 ? * 6L').type).toBe(Types.DAYS_PER_MONTH);
        expect(ScheduleOptions.createFrom('0 15 10 * * 6L').type).toBe(Types.CUSTOM); // Month must be ?
        expect(ScheduleOptions.createFrom('0 15 10 ? * 6L 2002-2005').type).toBe(Types.CUSTOM); // We dont allow years
        expect(ScheduleOptions.createFrom('0 15 10 ? * 6#3').type).toBe(Types.DAYS_PER_MONTH);
        expect(ScheduleOptions.createFrom('0 15 10 * * 6#3').type).toBe(Types.CUSTOM); // Month must be ?
        expect(ScheduleOptions.createFrom('0 0 12 1/5 * ?').type).toBe(Types.CUSTOM); // We dont allow every 5 days
        expect(ScheduleOptions.createFrom('0 11 11 11 11 ?').type).toBe(Types.CUSTOM); // We dont allow to set the concrete month
        expect(ScheduleOptions.createFrom('0 11 11 11 * ?').type).toBe(Types.DAYS_PER_MONTH);
    });

    test('Should bail if expression cannot be parsed', () => {
        expect(() => ScheduleOptions.createFrom('')).toThrow();
        expect(() => ScheduleOptions.createFrom(null)).toThrow();
        expect(() => ScheduleOptions.createFrom()).toThrow();
        expect(() => ScheduleOptions.createFrom(undefined)).toThrow();
    });

    test('Should bail if expression has less than 6 and more than 7 fields', () => {
        expect(() => ScheduleOptions.createFrom('')).toThrow();
        expect(() => ScheduleOptions.createFrom('1')).toThrow();
        expect(() => ScheduleOptions.createFrom('1 2')).toThrow();
        expect(() => ScheduleOptions.createFrom('1 2 3')).toThrow();
        expect(() => ScheduleOptions.createFrom('1 2 3 4')).toThrow();
        expect(() => ScheduleOptions.createFrom('1 2 3 4 5')).toThrow();
        expect(() => ScheduleOptions.createFrom('1 2 3 4 5 6 7 8')).toThrow();


        // Also verify it works if field count is correct
        expect(() => ScheduleOptions.createFrom('1 2 3 4 5 6')).not.toThrow();
        expect(() => ScheduleOptions.createFrom('1 2 3 4 5 6 7')).not.toThrow();
    })
});
