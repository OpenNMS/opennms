export default class Range {
    constructor(cronHours) {
        const intervalIndex = cronHours.indexOf('/');
        const rangeIndex = cronHours.indexOf('-');
        const range = cronHours.substr(0, intervalIndex === -1 ? cronHours.length : intervalIndex); // remove interval
        this.start = range.substr(0, rangeIndex === -1 ? range.length : rangeIndex);
        this.end = rangeIndex === -1 ? this.start : range.substr(rangeIndex + 1);
    }
}