package kvpaxos;
import paxos.Paxos;
import paxos.Paxos.retStatus;
import paxos.State;
// You are allowed to call Paxos.Status to check if agreement was made.

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements KVPaxosRMI {

    ReentrantLock mutex;
    Registry registry;
    Paxos px;
    int me;

    String[] servers;
    int[] ports;
    KVPaxosRMI stub;
    // Your definitions here
    AtomicInteger sequence;
    HashMap<String, Integer> serverDB;

    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        
        // Your initialization code here
        this.sequence = new AtomicInteger(0);
        serverDB = new HashMap<>();

        try{
            System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
            registry = LocateRegistry.getRegistry(this.ports[this.me]);
            stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("KVPaxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    // RMI handlers
    public Response Get(Request req){
        // Your code here
    	Op operation = new Op(req.getOp(), 1, req.getKey(), req.getValue());
    	int serverSequence = sequence.get();
    	px.Start(sequence.get(), operation);
    	retStatus status = px.Status(serverSequence);
    	while(status.state == State.Pending){
    		status = px.Status(serverSequence);
    		try{
    			Thread.sleep(10);
    		}
    		catch (Exception e){
    			
    		}
    	}
    	 
    	return new Response(req.getKey(), ((Op)status.v).value);
    }

    public Response Put(Request req){
        // Your code here
    	Op operation = new Op(req.getOp(), 1, req.getKey(), req.getValue());
    	px.Start(sequence.get(), operation);
    	int serverSequence = sequence.get();

    	retStatus status = px.Status(serverSequence);
    	while(status.state == State.Pending){
    		status = px.Status(serverSequence);
    		try{
    			Thread.sleep(10);
    		}
    		catch (Exception e){
    			
    		}
    	}
    	 
    	return new Response(req.getKey(), ((Op)status.v).value);
    }


}
