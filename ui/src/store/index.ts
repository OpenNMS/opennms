import { createStore } from 'vuex'

// store modules
import searchModule from './search'
import nodesModule from './nodes'
import eventsModule from './events'
import ifServicesModule from './ifServices'
import spinnerModule from './spinner'
import inventoryModule from './inventory'
import locationsModule from './locations'
import fileEditorModule from './fileEditor'
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
    fileEditorModule,
    appModule
  }
})
