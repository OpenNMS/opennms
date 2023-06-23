export const findByText = (wrap: any, selector: string, text: string) => {
  return wrap.findAll(selector).filter((n: any) => n.text().match(text))[0]
}
