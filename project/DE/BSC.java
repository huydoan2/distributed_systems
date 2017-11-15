/**
 * Created by huydo on 11/13/2017.
 */

package DE;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import paxos.PaxosRMI;
import paxos.Request;
import paxos.Response;

public class BSC implements  Runnable, BSCRMI{

    int[] T;        //container to store all the votes
    int[] vote;     // ranking of k candidates
    String[] peers; // hostname
    int[] ports;    // host port
    int pid;        // me

    Registry registry;
    BSCRMI stub;
    
    BSC(int pid, String[] peers, int[] ports){
        this.pid = pid;
        this.peers = peers;
        this.ports = ports;
        
        
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.pid]);
            registry = LocateRegistry.createRegistry(this.ports[this.pid]);
            stub = (BSCRMI) UnicastRemoteObject.exportObject(this, this.ports[this.pid]);
            registry.rebind("BSC", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        
    }
    
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        BSCRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub= (BSCRMI) registry.lookup("BSC");
            if(rmi.equals("Vote"))
                callReply = stub.Vote(req);
            else if(rmi.equals("Send"))
                callReply = stub.Send(req);
            else if(rmi.equals("Receive"))
                callReply = stub.Receive(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }
    
    public Response Vote(Request req){
        DE.Response resp = new Response(T[pid]);
        return resp;
    }

    @Override
    public void run(){
        // setup T and vote first



        T[pid] = vote[0];
        for(int j = 0; j < peers.length; ++j){
        	
            if(j == pid)
                continue;

            Request request = new Request(this.pid, T[pid]);
            Response resp = Call("Vote", request, j);
            T[j] = resp.getVote();
        }
    }

	@Override
	public Response Vote(Request req) throws RemoteException {
		// Req contains pid of process which sent vote and what vote is
		return null;
	}
}
