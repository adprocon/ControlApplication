export class Constant {
    // static beurl = 'http://127.0.0.1:8080/'
    // eslint-disable-next-line no-restricted-globals
    static beurl = "http://" + location.host.toString() + "/";

    // eslint-disable-next-line no-restricted-globals
    // static beurl = "http://" + location.host.toString().split(":")[0] + ':8080/';
}