///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

/* eslint no-console: 0 */

export default class OnmsLoader {
  public prefix = '/opennms/assets/';
  public extension = '.vaadin.js';
  private loaded = new Set<string>();
  private mappings: object;

  public constructor(prefix?: string, extension?: string) {
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
    if (check()) {
      console.log('OnmsLoader: ' + name + ' is ready.');
      onready();
    } else if (retry < 10000) {
      const nextRetry = Math.round(retry * 1.5);
      console.log('OnmsLoader: ' + name + ' is not ready. Trying again in ' + nextRetry + 'ms.');
      setTimeout(() => {
        this.waitFor(name, check, onready, nextRetry);
      }, nextRetry);
    } else {
      console.log('OnmsLoader: giving up on ' + name);
    }
  }
}
