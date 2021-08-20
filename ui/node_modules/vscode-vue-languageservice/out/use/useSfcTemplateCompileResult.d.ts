import * as vscode from 'vscode-languageserver';
import { TextDocument } from 'vscode-languageserver-textdocument';
import { Ref } from '@vue/reactivity';
import * as CompilerDOM from '@vue/compiler-dom';
export declare function useSfcTemplateCompileResult(htmlDocument: Ref<TextDocument | undefined>, isVue2Mode: boolean): import("@vue/reactivity").ComputedRef<{
    errors: vscode.Diagnostic[];
    ast: CompilerDOM.RootNode | undefined;
} | undefined>;
