import { TextDocument } from 'vscode-languageserver-textdocument';
import type * as css from 'vscode-css-languageservice';
import { LanguageServiceContext } from '../types';
export declare function parse(css: typeof import('vscode-css-languageservice'), styleDocuments: {
    textDocument: TextDocument;
    stylesheet: css.Stylesheet | undefined;
    links: {
        textDocument: TextDocument;
        stylesheet: css.Stylesheet;
    }[];
}[], context: LanguageServiceContext): Map<string, Map<string, Set<[number, number]>>>;
