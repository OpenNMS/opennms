import { createStore } from 'vuex'

// store modules
import graphModule from './graph'
import mapModule from './map'
import resourceModule from './resource'
import scvModule from './scv'
import searchModule from './search'

export default createStore({
  modules: {
    graphModule,
    mapModule,
    resourceModule,
    scvModule,
    searchModule
  }
})
