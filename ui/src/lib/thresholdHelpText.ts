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

import { ThresholdType } from '@/lib/thresholdValidator'

/**
 * Help copy for the threshold configuration screens, carried over from the JSP editor this replaces.
 *
 * Kept in one module rather than inline in the templates so the wording is testable and cannot drift
 * between the two definition forms.
 */

/** Short hints shown under individual form fields. */
export const THRESHOLD_FIELD_HINTS = {
  dsName: 'Maximum 19 characters, the longest datasource name RRDtool can store.',
  expression: 'A mathematical expression over one or more collected datasources.',
  dsType:
    'node applies to node-level data and ignores resource filters. if applies to interface-level data. ' +
    'Other entries are generic resource types from datacollection-config.xml.',
  dsLabel: 'A collected string attribute used to label the resource in generated events.',
  exprLabel: 'A readable label for this expression, used in generated events.',
  value: 'A number, or a metadata reference such as ${scv:key:value}.',
  rearm: 'The value at which the threshold re-arms. Not used by relativeChange thresholds.',
  trigger: 'How many polls in a row must exceed the value before the threshold triggers.',
  triggeredUEI:
    'Leave blank to use the standard threshold event. A custom UEI follows the form ' +
    'uei.opennms.org/<category>/<name>; using a one-word company name as the category avoids collisions ' +
    'with future OpenNMS events.',
  rearmedUEI: 'Leave blank to use the standard re-arm event.',
  filterOperator: 'or applies the threshold when any filter matches. and requires every filter to match.',
  rrdRepository: 'The directory holding the RRD files this group thresholds against.',
  packageFilter: 'A filter rule selecting the interfaces this package applies to.',
  serviceInterval: 'Milliseconds between threshold evaluations.',
  thresholdingGroup: 'The threshold group applied to the interfaces this package selects.'
}

/** Shown against the Type field and changes with the selected type. */
export const THRESHOLD_TYPE_HELP: Record<ThresholdType, string> = {
  [ThresholdType.High]: 'Triggers when the value rises above the threshold, and re-arms when it falls below the re-arm value.',
  [ThresholdType.Low]: 'Triggers when the value falls below the threshold, and re-arms when it rises above the re-arm value.',
  [ThresholdType.RelativeChange]:
    'Triggers when the value changes by more than the given factor between two polls. Re-arm and trigger are not used.',
  [ThresholdType.AbsoluteChange]:
    'Triggers when the value changes by more than the given amount between two polls. It never re-arms, so a re-armed UEI is ignored.',
  [ThresholdType.RearmingAbsoluteChange]:
    'Triggers on an absolute change like absoluteChange, but also sends a re-arm event once the value settles.'
}

export const THRESHOLD_DEFINITION_HELP = `
A threshold watches one collected datasource, or in the case of an expression threshold a mathematical
expression over several of them, and sends an event when the value crosses the configured boundary.

Type decides what "crossing" means. High triggers above the value and re-arms below the re-arm value; low
is the mirror image. relativeChange triggers on a proportional change between two polls and absoluteChange
on a fixed change; neither re-arms, so a re-armed UEI is ignored for them. rearmingAbsoluteChange behaves
like absoluteChange but also sends a re-arm event.

Datasource type decides what the threshold is evaluated against. node covers node-level data and ignores
resource filters entirely. if covers interface-level data. Any other entry is a generic resource type
declared in datacollection-config.xml on this system.

Value, re-arm and trigger accept a number or a metadata reference such as \${scv:key:value}. Trigger is the
number of consecutive polls that must exceed the value before the threshold fires. Re-arm and trigger are
required by the schema even where the daemon ignores them.

Triggered UEI and re-armed UEI override the standard threshold events. Leave them blank unless you need a
distinct event; if you set one that OpenNMS does not know yet, a matching event definition is created for
you, cloned from the standard threshold event.
`.trim()

export const RESOURCE_FILTER_HELP = `
Resource filters narrow a threshold to particular resources. They only apply to interface-level and
generic-resource thresholds; a node-level datasource type ignores them.

Each filter names a field of the resource and a regular expression matched against it. Filters are applied
in the order listed.

The filter operator decides how several filters combine: or applies the threshold when any one of them
matches, and requires every one of them to match.
`.trim()

export const GROUP_HELP = `
A threshold group collects the thresholds evaluated against one RRD repository.

The upper table holds basic thresholds, each watching a single collected datasource. The lower table holds
expression-based thresholds, which evaluate a mathematical expression over one or more datasources.

A group only takes effect once a threshd package references it: add a service to a package and give it a
thresholding-group parameter naming this group.

Where a threshold declares its own triggered or re-armed UEI, the UEI is shown as a link into the
notification wizard, so you can decide who gets told when it fires.
`.trim()

export const PACKAGE_HELP = `
A threshd package decides which interfaces get thresholded and how often.

The filter rule selects the interfaces the package applies to. Specifics, include ranges and include URLs
add addresses to that selection, and exclude ranges remove them again.

Each service in the package names a threshold group through its thresholding-group parameter, and an
interval in milliseconds controlling how often thresholds are evaluated.

Outage calendars suspend thresholding during a scheduled outage. They are also maintained from the
scheduled outages page, so re-read a package before saving it if someone may have changed them there.
`.trim()
