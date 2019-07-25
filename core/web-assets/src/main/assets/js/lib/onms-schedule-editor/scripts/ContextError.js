export default class ContextError extends Error {
    constructor(context='entity', ...params) { // TODO MVR this is not ES2015/2016 compatible
        super(...params);
        this.context = context;
    }
}