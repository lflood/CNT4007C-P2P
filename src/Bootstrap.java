//bootsrapping provides entry point for p2p networks
//deals with peer connectivity
//specifically connecting peers to previous peers by their IDs

import java.util.Hashtable;

public class Bootstrap{
    public static void bootstrappingPeer(Peer peer, int peerID, Hashtable<Integer, String> peerInfo){
        int prev = peerID - 1;

        while (prev > 0){
            String address = peerInfo.get(prev);
            System.out.println("hostname+port = " + address);
            String hostname = address.split(":")[0];
            String port = address.split(":")[1];

        }
    }



}