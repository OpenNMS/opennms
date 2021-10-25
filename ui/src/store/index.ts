import { createStore } from 'vuex'

// store modules
import searchModule from './search'
import nodesModule from './nodes'
import eventsModule from './events'
import ifServicesModule from './ifServices'
import configuration from './configuration'
import spinnerModule from './spinner'

export default createStore({
  modules: {
    searchModule,
    nodesModule,
    eventsModule,
    ifServicesModule,
    configuration,
    spinnerModule
  }
})
