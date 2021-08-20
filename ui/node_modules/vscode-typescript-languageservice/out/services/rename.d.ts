import type * as ts from 'typescript/lib/tsserverlibrary';
import * as vscode from 'vscode-languageserver';
import { TextDocument } from 'vscode-languageserver-textdocument';
import type { LanguageServiceHost } from '../';
export declare function register(languageService: ts.LanguageService, getTextDocument: (uri: string) => TextDocument | undefined, host: LanguageServiceHost): (uri: string, position: vscode.Position, newName: string) => Promise<vscode.WorkspaceEdit | undefined>;
export declare function fileTextChangesToWorkspaceEdit(changes: readonly ts.FileTextChanges[], getTextDocument: (uri: string) => TextDocument | undefined): vscode.WorkspaceEdit;
