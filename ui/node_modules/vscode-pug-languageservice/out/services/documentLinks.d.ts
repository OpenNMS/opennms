import type * as html from 'vscode-html-languageservice';
import type { PugDocument } from '../pugDocument';
export declare function register(htmlLs: html.LanguageService): (pugDoc: PugDocument, docContext: html.DocumentContext) => html.DocumentLink[];
