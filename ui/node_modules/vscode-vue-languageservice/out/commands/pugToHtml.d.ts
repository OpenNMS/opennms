import type { TextDocument } from 'vscode-languageserver-textdocument';
import type { SourceFile } from '../sourceFile';
import * as vscode from 'vscode-languageserver';
export declare function execute(document: TextDocument, sourceFile: SourceFile, connection: vscode.Connection): void;
