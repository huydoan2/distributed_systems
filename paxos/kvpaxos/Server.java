package kvpaxos;
import paxos.Paxos;
import paxos.Paxos.retStatus;
import paxos.State;
// You are allowed to call Paxos.Status to check if agreement was made.

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
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
    
    // serverDB contains actual commits to hashmap
    HashMap<String, Integer> serverDB;
    
    // serverLog contains log of messages
    HashMap<Integer, Op> serverLog;

    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        
        // Your initialization code here
        this.sequence = new AtomicInteger(0);
        serverDB = new HashMap<>();
        serverLog = new HashMap<>();
        

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
    	// TODO: Maybe use this to update log on server as well if lagging?
    	

    	int serverSequence = sequence.getAndIncrement();
		Op operation = new Op(req.getOp(), serverSequence, req.getKey(), req.getValue());
    	px.Start(serverSequence, operation);
    	
    	Op result = wait(serverSequence);
    	while (!result.equals(operation)){
    		serverSequence = sequence.getAndIncrement();
    		operation = new Op(req.getOp(), serverSequence, req.getKey(), req.getValue());
        	px.Start(serverSequence, operation);
    	}
    	

    	executeLogCommands();
    	Response response = new Response(req.getKey(), serverDB.get(req.getKey()));
    	return response;
    	
    	
    }

    void executeLogCommands(){
    	
    	ArrayList<Integer> log_ops = new ArrayList<Integer>(serverLog.keySet());
    	Collections.sort(log_ops);
    	for (int i: log_ops){
    		if (i > px.Min()){
    			break;
    		}
    		Op exec_command = serverLog.get(i);
			if (exec_command.op.equals("Put")){
				serverDB.put(exec_command.key, exec_command.value);
			}
    	}
    }
    public Response Put(Request req){
        // Your code here
    	int serverSequence = sequence.getAndIncrement();
    	Op operation = new Op(req.getOp(), serverSequence, req.getKey(), req.getValue());
    	px.Start(serverSequence, operation);

    	Op result = wait(serverSequence);
    	serverLog.put(serverSequence, result);
    	
    	while (!result.equals(operation)){
    		serverSequence = sequence.getAndIncrement();
    		operation = new Op(req.getOp(), serverSequence, req.getKey(), req.getValue());
        	px.Start(serverSequence, operation);
        	result = wait(serverSequence);
        	serverLog.put(serverSequence, result);
    	}
    	
    	Response response = new Response(req.getKey(), true);
    	// logs before this number should be committed to serverDB
    	executeLogCommands();
    	px.Done(serverSequence);
    	return response;
    }


    public Op wait (int seq){
    	int to = 10;
    	while (true){
    		Paxos.retStatus ret = this.px.Status(seq);
    		if(ret.state == State.Decided ){
    			return Op.class.cast(ret.v) ;
    		}
    		try{
    			Thread.sleep(to) ;
    		}
    		catch ( Exception e ){
    			e.printStackTrace() ;
    		}
    		if (to < 1000){
    			to = to * 2 ;   	
    		}
    	}
    	}
}
