import Weekdays from './Weekdays';

export default class WeekdayOptions {
    constructor(input) {
        let options = {};

        // in case of an array, convert it to an object
        if (Array.isArray(input)) {
            let newOptions = {};
            input.forEach((item) => {
                if (item instanceof Weekdays.Weekdays) {
                    newOptions[item.label] = true;
                } else {
                    newOptions[item] = true;
                }
            });
            options = newOptions;
        } else if (typeof input !== 'undefined') {
            options = input;
        }

        // Initialize
        Object.values(Weekdays.all).forEach((weekday) => {
            if (options.hasOwnProperty(weekday.label) === false) {
                this[weekday.label] = false;
            } else {
                this[weekday.label] = options[weekday.label] && true; // enforce boolean
            }
        }, this);
    }

    getSelectedWeekdays() {
        const selectedWeekdays = [];
        Object.values(Weekdays.all).forEach((weekday) => {
            if (this[weekday.label] === true) {
                selectedWeekdays.push(weekday);
            }
        });
        return selectedWeekdays;
    }

    static createFrom(daysOfWeekExpression) {
        const days = daysOfWeekExpression.split(',');
        const weekdays = Object.values(Weekdays.all);
        const selectedWeekdays = [];
        days.forEach((eachDay) => {
            for (let i = 0; i < weekdays.length; i++) {
                if (eachDay === weekdays[i].shortcut) {
                    selectedWeekdays.push(weekdays[i])
                }
            }
        });
        const options = new WeekdayOptions(selectedWeekdays);
        return options;
    }
}
