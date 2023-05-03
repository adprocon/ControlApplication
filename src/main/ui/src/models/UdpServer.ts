import {Tag} from "./Tag";

class UdpServer {
    port: number = 8000;
    read: Tag[] = [];
    write: Tag[] = [];
}

export default UdpServer;