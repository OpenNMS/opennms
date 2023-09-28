import { createStore } from 'vuex'

// store modules
import searchModule from './search'

export default createStore({
  modules: {
    searchModule
  }
})
