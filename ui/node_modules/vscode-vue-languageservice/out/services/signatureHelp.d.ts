import type * as vscode from 'vscode-languageserver';
import type { ApiLanguageServiceContext } from '../types';
export declare function register({ sourceFiles, getTsLs }: ApiLanguageServiceContext): (uri: string, position: vscode.Position, context?: vscode.SignatureHelpContext | undefined) => vscode.SignatureHelp | undefined;
