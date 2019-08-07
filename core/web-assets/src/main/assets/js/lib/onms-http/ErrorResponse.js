import ContextError from "./ContextError";

export default class ErrorResponse {

    constructor(response) {
        this.response = response;
    }

    get status() {
        return this.response.status;
    }

    isContextError() {
        return this.response.status === 400 && this.response.data && this.response.data.context && this.response.data.message;
    }

    asContextError() {
        if (this.isContextError()) {
            return new ContextError(this.response.data.context, this.response.data.message);
        }
        if (this.response.status === 400) {
            if (this.response.data && this.response.data.message) {
                return new ContextError('entity', this.response.data.message);
            }
            return new ContextError('entity', 'Unexpected error occurred. No details about the nature of the error were provided');
        }
        throw new Error('Cannot convert to ContextError');
    }

    isBadRequest() {
        return this.status === 400;
    }
}