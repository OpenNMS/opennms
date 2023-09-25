import { createStore } from 'vuex'

// store modules
import mapModule from './map'
import resourceModule from './resource'
import scvModule from './scv'
import searchModule from './search'

export default createStore({
  modules: {
    mapModule,
    resourceModule,
    scvModule,
    searchModule
  }
})
