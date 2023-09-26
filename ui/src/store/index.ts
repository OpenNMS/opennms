import { createStore } from 'vuex'

// store modules
import mapModule from './map'
import scvModule from './scv'
import searchModule from './search'

export default createStore({
  modules: {
    mapModule,
    scvModule,
    searchModule
  }
})
