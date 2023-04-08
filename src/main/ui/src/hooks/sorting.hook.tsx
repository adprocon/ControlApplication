
const objCompare = (a: any, b: any, compField: string, reverse: boolean) => {
    // @ts-ignore
    if (a[compField] > b[compField]) {
        return reverse ? -1 : 1
    }
    // @ts-ignore
    if (a[compField] < b[compField]) {
        return reverse ? 1 : -1
    }
    return 0
}

export default objCompare;