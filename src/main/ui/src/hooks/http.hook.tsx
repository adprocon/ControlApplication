export function useHttp() {
    const request = async (url: any,
                           method: string = 'GET',
                           body: BodyInit | null | undefined = null,
                           headers = {'Content-Type': 'application/json'}) => {
        try {
            const response: Response = await fetch(url, {method, body, headers});
            if (!response.ok) {
                throw new Error(`Could not fetch ${url}, status: ${response.status}`);
            }
            const data = await response;
            if (method === 'POST' || method === 'DELETE') {
                return;
            } else {
                return data.json();
            }
        } catch (e) {
            throw e;
        }
    }

    return {request};
}