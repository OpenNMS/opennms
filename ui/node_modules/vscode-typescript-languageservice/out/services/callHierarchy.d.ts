import { TextDocument } from 'vscode-languageserver-textdocument';
import * as vscode from 'vscode-languageserver';
import type * as ts from 'typescript/lib/tsserverlibrary';
export declare function register(languageService: ts.LanguageService, getTextDocument: (uri: string) => TextDocument | undefined): {
    doPrepare: (uri: string, position: vscode.Position) => vscode.CallHierarchyItem[];
    getIncomingCalls: (item: vscode.CallHierarchyItem) => vscode.CallHierarchyIncomingCall[];
    getOutgoingCalls: (item: vscode.CallHierarchyItem) => vscode.CallHierarchyOutgoingCall[];
};
