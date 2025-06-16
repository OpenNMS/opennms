/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
require('lib/onms-http');
require('../../onms-date-formatter');
require('../../onms-default-apps');
require('../lib/scripts/controllers/Asset.js');
require('../lib/scripts/controllers/CloneForeignSource.js');
require('../lib/scripts/controllers/Detector.js');
require('../lib/scripts/controllers/ForeignSource.js');
require('../lib/scripts/controllers/Interface.js');
require('../lib/scripts/controllers/MetaData.js');
require('../lib/scripts/controllers/Move.js');
require('../lib/scripts/controllers/Node.js');
require('../lib/scripts/controllers/Policy.js');
require('../lib/scripts/controllers/QuickAddNode.js');
require('../lib/scripts/controllers/QuickAddNodeModal.js');
require('../lib/scripts/controllers/Requisition.js');
require('../lib/scripts/controllers/Requisitions.js');
require('../lib/scripts/directives/requisitionConstraints.js');
require('../lib/scripts/filters/startFrom.js');
require('../lib/scripts/model/QuickNode.js');
require('../lib/scripts/model/Requisition.js');
require('../lib/scripts/model/RequisitionInterface.js');
require('../lib/scripts/model/RequisitionNode.js');
require('../lib/scripts/model/RequisitionService.js');
require('../lib/scripts/model/RequisitionsData.js');
require('../lib/scripts/services/Requisitions.js');
require('../lib/scripts/services/Synchronize.js');
