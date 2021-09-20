export default class ContextError extends Error {
    constructor(context='entity', ...params) {
        super(...params);
        this.context = context;
    }
}