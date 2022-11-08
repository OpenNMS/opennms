const externalComponent = (url: string) => {
  try {
    const name = (
      url
        .split('/')
        .reverse()[0]
        .match(/^(.*?)\.es/) as any[]
    )[1]

    if (window[name]) {
      return new Promise((resolve) => {
        resolve(window[name])
      })
    }

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
    window[name] = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.type = 'module'
      script.async = true
      script.crossOrigin = 'use-credentials'

      script.addEventListener('load', () => {
        resolve(window[name])
      })
      script.addEventListener('error', () => {
        reject(new Error(`Error loading ${url}`))
      })
      script.src = url
      document.head.appendChild(script)
    })

    return window[name] as any
  } catch (err) {
    console.log('Errors with component url.', url)
  }
}

const addStylesheet = (url: string) => {
  const exists = document.querySelector(`link[href='${url}']`)
  if (exists) return

  const head = document.head
  const link = document.createElement('link')

  link.type = 'text/css'
  link.rel = 'stylesheet'
  link.href = url

  head.prepend(link)
}

const getJSPath = (baseUrl: string, extensionId: string, rootPath: string, fileName: string) => {
  return `${baseUrl}/plugins/ui-extension/module/${extensionId}?path=${rootPath}/${fileName}`
}

const getCSSPath = (baseUrl: string, extensionId: string) => {
  return `${baseUrl}/plugins/ui-extension/css/${extensionId}`
}

export { externalComponent, addStylesheet, getJSPath, getCSSPath }
