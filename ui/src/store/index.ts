import { createStore } from 'vuex'

// store modules
import searchModule from './search'
import nodesModule from './nodes'
import eventsModule from './events'
import ifServicesModule from './ifServices'
import spinnerModule from './spinner'
import inventoryModule from './inventory'
import locationsModule from './locations'
import mapModule from './map'
import fileEditorModule from './fileEditor'
import authModule from './auth'
import logsModule from './logs'
import appModule from './app'

export default createStore({
  modules: {
    searchModule,
    nodesModule,
    eventsModule,
    ifServicesModule,
    spinnerModule,
    inventoryModule,
    locationsModule,
    mapModule,
    fileEditorModule,
    logsModule,
    appModule,
    authModule
  }
})
