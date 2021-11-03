import axios from 'axios'
//import { BASE_REST_URL, BASE_REST_AUTH } from '../../../../devConfig'

export const axiosAuth = axios.create()
/*
axiosAuth.defaults.baseURL = BASE_REST_URL
axiosAuth.defaults.headers.common['Authorization'] = 'Basic ' + BASE_REST_AUTH
*/

axiosAuth.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  }
)

// Add a response interceptor
axiosAuth.interceptors.response.use(
  function (response) {
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  }
)
