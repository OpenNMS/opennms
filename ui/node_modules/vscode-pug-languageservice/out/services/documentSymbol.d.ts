import type * as html from 'vscode-html-languageservice';
import * as vscode from 'vscode-languageserver';
import type { PugDocument } from '../pugDocument';
export declare function register(htmlLs: html.LanguageService): (pugDoc: PugDocument) => vscode.SymbolInformation[];
