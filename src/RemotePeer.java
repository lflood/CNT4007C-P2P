public class RemotePeer{


    public RemotePeer(int localPeerId, int remotePeerid, String hostname, int port){
        this.peerid = remotePeerid;
        this.localPeerId = localPeerId;

        this.hostname = hostname;
        this.port = port;
    }
}


