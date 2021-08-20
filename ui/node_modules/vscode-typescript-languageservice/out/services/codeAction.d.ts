import type * as ts from 'typescript/lib/tsserverlibrary';
import * as vscode from 'vscode-languageserver';
import type { TextDocument } from 'vscode-languageserver-textdocument';
import type { LanguageServiceHost } from '../';
export interface FixAllData {
    type: 'fixAll';
    uri: string;
    fileName: string;
    fixIds: {}[];
}
export interface RefactorData {
    type: 'refactor';
    uri: string;
    fileName: string;
    refactorName: string;
    actionName: string;
    range: {
        pos: number;
        end: number;
    };
}
export interface OrganizeImportsData {
    type: 'organizeImports';
    uri: string;
    fileName: string;
}
export declare type Data = FixAllData | RefactorData | OrganizeImportsData;
export declare function register(languageService: ts.LanguageService, getTextDocument: (uri: string) => TextDocument | undefined, host: LanguageServiceHost): (uri: string, range: vscode.Range, context: vscode.CodeActionContext) => Promise<vscode.CodeAction[] | undefined>;
