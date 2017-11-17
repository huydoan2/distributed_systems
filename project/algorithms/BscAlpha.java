/**
 * Created by huydo on 11/13/2017.
 */

package algorithms;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import DE.BSCRMI;
import DE.Request;
import DE.Response;
import DE.SecureTransfer;


public class BscAlpha implements  Runnable, BSCRMI{

	// RMI Variables
	Registry registry;
	BSCRMI stub;
	String[] peers; // hostname
	int[] ports;    // host port
	
	
	
	// Voting variables
    String [] T;           // container to store all the votes
    String [] myVoteRanking;   // ranking of k candidates
    int pid;               // me
    int finalValue;
    int faults;
    int numCandidates;     // size of myVoteRanking
    int receivedVotes;
    int receivedDones;
    int receivedKeys;
    Random randomGenerator;
    SecureTransfer secure;
    
    static ReentrantLock mutex = new ReentrantLock();
    
    
    BscAlpha(int pid, String[] peers, int[] ports, int candidates, int faults){
        this.pid = pid;
        this.peers = peers;
        this.ports = ports;
        this.faults = faults;
        numCandidates = candidates;
        receivedVotes = 0;
        receivedDones = 0;
        receivedKeys = 0;
        randomGenerator = new Random();
              
        secure = new SecureTransfer();
        T = new String [peers.length];
        
        myVoteRanking = new String [numCandidates];
        
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
            else if (rmi.equals("Done"))
            	callReply = stub.Done(req);
            else if (rmi.equals("Key"))
            	callReply = stub.Key(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }
    
    

    @Override
    public void run(){
        // setup T and vote first
        //T[pid] = vote[0];
    	
        T[pid] = String.valueOf(randomGenerator.nextInt());
        
              
        // Step 1: Send first choice with all
        // We assume that all processes start the democratic election
        // This assumption should not affect the outcome as we can always trigger votes
        // if a random node starts the election instead of all of them
        for(int j = pid; j < peers.length; ++j){
        	
            if(j == pid)
                continue;

            try{
            	 Request request = new Request(pid, secure.encrypt(T[pid]));
            	 secure.decrypt(secure.getKey(), secure.encrypt(T[pid]));
            	 Response response = Call("Vote", request, j);
            	 // Received encrypted vote as response
            	 // Store this vote
            	 T[response.getPid()] = response.getVote();
            	 receivedVotes++;
            } catch (Exception e) {
            	e.printStackTrace();
            }
            
        }
        
        // Wait to receive votes from everyone, inform everyone
        //System.out.println("Waiting to get all votes");
        while (this.receivedVotes != peers.length - 1) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        
        //System.out.println("Sending Done to all");

    	for (int i = 0; i < peers.length; ++i){
    		if (i == this.pid)
    			continue;
    		// Send done request to all
    		Request request = new Request(this.pid, true);
    		Call("Done", request, i);
    	}
    
        
        //System.out.println("Waiting to get Done from all");
        while (this.receivedDones != peers.length - 1) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        // All have received all votes, broadcast keys
        for (int i = 0; i < peers.length; ++i){
        	if (i == this.pid)
        		continue;

        	Request request = new Request(this.pid, secure);
        	Call("Key", request, i);
        }
        
        
        // Wait to receive votes from everyone, inform everyone
        //System.out.println("Waiting to get all votes");
        while (this.receivedKeys != peers.length - 1) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        
        // Step 2: Agree on T vector: ballot of all votes
        for (int j = 0; j < peers.length; ++j){
        	// Run Standard Byzantine Agreement on T[j]
        }
        
        // Step 3
        //finalValue = leastValue(T);
        
        
    }
    
    int leastValue(int [] T){
    	HashMap<Integer, Integer> valueMap = new HashMap<>();
    	int maxCount = 0, maxValue = T[0];
    	for (int i = 0; i < T.length; ++i) {
    		if (valueMap.containsKey(T[i])){
    			valueMap.put(T[i], valueMap.get(T[i]) + 1);
    		}
    		else
    			valueMap.put(T[i], 1);
    		
    		if (valueMap.get(T[i]) > maxCount){
    			maxCount = valueMap.get(T[i]);
    			maxValue = T[i];
    		}
    		else if (valueMap.get(T[i]) == maxCount && T[i] < maxValue){
    			maxValue = T[i];
    		}
    	}
    	
    	return maxValue;
    }

	@Override
	public Response Vote(Request req) throws RemoteException {
		T[req.getPid()] = req.getVoteValue();
		try {
			// Received encrypted vote
			// Respond with your own encrypted vote
			Response resp = new Response(pid, secure.encrypt(T[pid]));
			this.receivedVotes++;
			return resp;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Response Done(Request req) throws RemoteException {
		this.receivedDones++;
		return null;
	}

	@Override
	public Response Key(Request req) throws RemoteException {
		// Print out votes when all received and non decrypted
		if (this.receivedKeys == 0) {
			try{
				BscAlpha.mutex.lock();
				StringBuilder sb = new StringBuilder();
				sb.append("[ ");
				for (String a: T){
					sb.append(a + " ");
				}
				sb.append(']');
				System.out.println("Votes in pid (before decryption): " + pid);
				System.out.println(sb.toString());
				System.out.println("");
			} catch (Exception e){
				
			}
			finally {
				BscAlpha.mutex.unlock();
			}
			
		}
		try {
			this.receivedKeys++;
			T[req.getPid()] = req.getSecure().decrypt(T[req.getPid()]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
