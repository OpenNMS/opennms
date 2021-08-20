import * as vscode from 'vscode-languageserver';
import type { ApiLanguageServiceContext } from '../types';
export declare function register(context: ApiLanguageServiceContext): (uri: string, command: string, args: any[] | undefined, connection: vscode.Connection) => Promise<void>;
