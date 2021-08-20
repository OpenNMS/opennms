import type { HtmlLanguageServiceContext } from '../types';
import type * as vscode from 'vscode-languageserver';
import type { TextDocument } from 'vscode-languageserver-textdocument';
export declare function register({ getHtmlDocument, htmlLs }: HtmlLanguageServiceContext): (document: TextDocument, position: vscode.Position) => string | undefined | null;
