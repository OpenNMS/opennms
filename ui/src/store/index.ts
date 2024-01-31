import { createStore } from 'vuex'

// store modules
import searchModule from './search'
import nodesModule from './nodes'
import ipInterfacesModule from './ipInterfaces'
import eventsModule from './events'
import ifServicesModule from './ifServices'
import configuration from './configuration'
import mapModule from './map'
import fileEditorModule from './fileEditor'
import authModule from './auth'
import logsModule from './logs'
import appModule from './app'
import infoModule from './info'
import helpModule from './help'
import resourceModule from './resource'
import graphModule from './graph'
import pluginModule from './plugin'
import deviceModule from './device'
import scvModule from './scv'
import menuModule from './menu'
import usageStatisticsModule from './usageStatistics'

export default createStore({
  modules: {
    searchModule,
    nodesModule,
    ipInterfacesModule,
    eventsModule,
    ifServicesModule,
    configuration,
    mapModule,
    fileEditorModule,
    logsModule,
    authModule,
    appModule,
    infoModule,
    helpModule,
    resourceModule,
    graphModule,
    pluginModule,
    deviceModule,
    scvModule,
    menuModule,
    usageStatisticsModule
  }
})
