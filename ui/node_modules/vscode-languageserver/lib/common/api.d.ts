import { _, Features, _Connection } from './server';
import { SemanticTokensBuilder } from './semanticTokens';
import type { WorkDoneProgressReporter, WorkDoneProgressServerReporter, ResultProgressReporter } from './progress';
export * from 'vscode-languageserver-protocol/';
export { WorkDoneProgressReporter, WorkDoneProgressServerReporter, ResultProgressReporter };
export { SemanticTokensBuilder };
export * from './server';
import { DiagnosticsFeatureShape } from './proposed.diagnostic';
export declare namespace ProposedFeatures {
    const all: Features<_, _, _, _, _, _, DiagnosticsFeatureShape>;
    type Connection = _Connection<_, _, _, _, _, _, DiagnosticsFeatureShape>;
}
