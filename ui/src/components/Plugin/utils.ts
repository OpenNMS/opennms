const externalComponent = (url: string) => {
  try {
    const name = (
      url
        .split('/')
        .reverse()[0]
        .match(/^(.*?)\.es/) as any[]
    )[1]

    if (window[name]) return window[name]

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
    window[name] = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.type = 'module'
      script.async = true

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
  const head = document.head
  const link = document.createElement('link')

  link.type = 'text/css'
  link.rel = 'stylesheet'
  link.href = url

  head.appendChild(link)
}

export { externalComponent, addStylesheet }
