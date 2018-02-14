/* eslint no-console: 0 */

export default class OnmsLoader {
  public prefix = '/opennms/assets/';
  public extension = '.vaadin.js';
  private loaded = new Set<string>();
  private mappings: object;

  constructor(prefix?: string, extension?: string) {
    if (prefix) {
      this.prefix = prefix;
    }
    if (extension) {
      this.extension = extension;
    }

    this.mappings = {
      'backshift': this.prefix + 'backshift-js' + this.extension,
      'bootstrap': this.prefix + 'bootstrap-js' + this.extension,
      'd3': this.prefix + 'd3-js' + this.extension,
      'flot': this.prefix + 'flot-js' + this.extension,
      'global': this.prefix + 'global' + this.extension,
      'holder': this.prefix + 'holder-js' + this.extension,
      'ionicons-css': this.prefix + 'ionicons-css' + this.extension,
      'jquery': this.prefix + 'jquery-js' + this.extension,
      'jquery-ui': this.prefix + 'jquery-ui-js' + this.extension,
      'leaflet': this.prefix + 'leaflet-js' + this.extension,
      'manifest': this.prefix + 'manifest' + this.extension,
      'onms-graph': this.prefix + 'onms-graph' + this.extension,
//      'openlayers': this.prefix + 'legacy/openlayers-2.10/OpenLayers.js',
      'vendor': this.prefix + 'vendor' + this.extension,
    };
  }

  public load(...modules: string[]) {
    console.log('OnmsLoader: loading ' + modules.length + ' modules.');

    const fragment = document.createDocumentFragment();
    for (const module of modules) {
      if (this.loaded[module]) {
        console.log('OnmsLoader: skipping ' + module + ' (already loaded)');
      } else {
        if (this.mappings[module]) {
          console.debug('OnmsLoader: loading ' + module);
          const el = document.createElement('script');
          el.src = this.mappings[module];
          el.async = false;
          fragment.appendChild(el);
          this.loaded[module] = true;
        } else {
          console.error('OnmsLoader: no mapping for module "' + module + '"!');
        }
      }
    }
    document.body.appendChild(fragment);
    console.log('OnmsLoader: finished loading modules');
  }

  public waitFor(name: string, check: () => boolean, onready: () => void, retry = 50) {
    const self = this;
    if (check()) {
      console.log('OnmsLoader: ' + name + ' is ready.');
      onready();
    } else if (retry < 10000) {
      const nextRetry = Math.round(retry * 1.5);
      console.log('OnmsLoader: ' + name + ' is not ready. Trying again in ' + nextRetry + 'ms.');
      setTimeout(() => {
        self.waitFor(name, check, onready, nextRetry);
      }, nextRetry);
    } else {
      console.log('OnmsLoader: giving up on ' + name);
    }
  }
}
