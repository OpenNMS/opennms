import { createStore } from 'vuex'

// store modules
import deviceModule from './device'
import graphModule from './graph'
import mapModule from './map'
import resourceModule from './resource'
import scvModule from './scv'
import searchModule from './search'

export default createStore({
  modules: {
    deviceModule,
    graphModule,
    mapModule,
    resourceModule,
    scvModule,
    searchModule
  }
})
