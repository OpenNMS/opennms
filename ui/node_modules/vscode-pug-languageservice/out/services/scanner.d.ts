import type * as html from 'vscode-html-languageservice';
import type { PugDocument } from '../pugDocument';
export declare function register(htmlLs: html.LanguageService): (pugDoc: PugDocument, initialOffset?: number) => {
    scan: () => html.TokenType;
    getTokenOffset: () => number;
    getTokenEnd: () => number;
    getTokenText: () => string;
    getTokenLength: () => number;
    getTokenError: () => string | undefined;
    getScannerState: () => html.ScannerState;
} | undefined;
