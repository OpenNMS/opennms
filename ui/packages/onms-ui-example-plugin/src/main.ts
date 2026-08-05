import ExampleApp from './ExampleApp.vue'

// OpenNMS plugin contract: the host's externalComponent() injects this module
// via <script type="module"> and then mounts window[extensionId] as the root
// component (see ui/src/components/Plugin/utils.ts). The extensionId is
// derived from the module URL's second-to-last path segment.
;(window as unknown as Record<string, unknown>).exampleUiExtension = ExampleApp

export default ExampleApp
