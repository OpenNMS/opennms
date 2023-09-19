import { createStore } from 'vuex'

// store modules
import appModule from './app'
import authModule from './auth'
import configuration from './configuration'
import deviceModule from './device'
import fileEditorModule from './fileEditor'
import graphModule from './graph'
import helpModule from './help'
import ifServicesModule from './ifServices'
import infoModule from './info'
import ipInterfacesModule from './ipInterfaces'
import logsModule from './logs'
import mapModule from './map'
import pluginModule from './plugin'
import resourceModule from './resource'
import scvModule from './scv'
import searchModule from './search'
import usageStatisticsModule from './usageStatistics'

export default createStore({
  modules: {
    appModule,
    authModule,
    configuration,
    deviceModule,
    fileEditorModule,
    graphModule,
    helpModule,
    ifServicesModule,
    infoModule,
    ipInterfacesModule,
    logsModule,
    mapModule,
    pluginModule,
    resourceModule,
    scvModule,
    searchModule,
    usageStatisticsModule
  }
})
