import type * as ts from 'typescript/lib/tsserverlibrary';
import * as vscode from 'vscode-languageserver';
import type { TextDocument } from 'vscode-languageserver-textdocument';
export declare const renameInfoOptions: {
    allowRenameOfImportPath: boolean;
};
export declare function register(languageService: ts.LanguageService, getTextDocument: (uri: string) => TextDocument | undefined): (uri: string, position: vscode.Position) => vscode.Range | undefined | vscode.ResponseError<void>;
