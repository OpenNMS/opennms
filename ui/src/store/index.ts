import { createStore } from 'vuex'

// store modules
import configuration from './configuration'
import deviceModule from './device'
import fileEditorModule from './fileEditor'
import graphModule from './graph'
import ifServicesModule from './ifServices'
import ipInterfacesModule from './ipInterfaces'
import logsModule from './logs'
import mapModule from './map'
import resourceModule from './resource'
import scvModule from './scv'
import searchModule from './search'
import usageStatisticsModule from './usageStatistics'

export default createStore({
  modules: {
    configuration,
    deviceModule,
    fileEditorModule,
    graphModule,
    ifServicesModule,
    ipInterfacesModule,
    logsModule,
    mapModule,
    resourceModule,
    scvModule,
    searchModule,
    usageStatisticsModule
  }
})
