import { Proposed } from 'vscode-languageserver-protocol';
import type { Feature, _Languages, ServerRequestHandler } from './server';
/**
 * Shape of the linked editing feature
 *
 * @since 3.16.0
 */
export interface DiagnosticsFeatureShape {
    diagnostics: {
        /**
        * Asks the client to refresh all diagnostics provided by this server by
        * pull for the corresponding documents again.
        */
        refresh(): void;
        /**
        * Installs a handler for the document diagnostic request.
        *
        * @param handler The corresponding handler.
        */
        on(handler: ServerRequestHandler<Proposed.DocumentDiagnosticParams, Proposed.DocumentDiagnosticReport, Proposed.DocumentDiagnosticReportPartialResult, Proposed.DiagnosticServerCancellationData>): void;
        /**
         * Installs a handler for the workspace diagnostic request.
         *
         * @param handler The corresponding handler.
         */
        onWorkspace(handler: ServerRequestHandler<Proposed.WorkspaceDiagnosticParams, Proposed.WorkspaceDiagnosticReport, Proposed.WorkspaceDiagnosticReportPartialResult, Proposed.DiagnosticServerCancellationData>): void;
    };
}
export declare const DiagnosticFeature: Feature<_Languages, DiagnosticsFeatureShape>;
