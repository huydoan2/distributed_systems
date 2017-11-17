package algorithms;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import DE.BSCRMI;
import DE.Request;
import DE.Response;
import DE.SecureTransfer;
import DE.VoteType;

public abstract class KYoungScheme implements Runnable, BSCRMI{

	// RMI Variables
	Registry registry;
	BSCRMI stub;
	String[] peers; // hostname
	int[] ports;    // host port

	// Voting variables
	// Set of all permutations of k candidates
	public HashSet<String> P;
	
	String [] ballot;      // will contain agreed upon ballot
	String [] myVoteRank;
    int pid;               // me
    int faults;
    Random randomGenerator;
    SecureTransfer secure;
    
    int maxScore;
    String maxRank;
    
    
    // Variables for exchanging votes
    int receivedVotes, receivedKeys, receivedDones;
    Thread runner;
    
    static ReentrantLock mutex = new ReentrantLock();
    
    public KYoungScheme(int pid, String[] peers, int[] ports, String k, int faults){
        this.pid = pid;
        this.peers = peers;
        this.ports = ports;
        this.faults = faults;
        
        // Contain all permutations of voter ranks
        P = (HashSet<String>) permutationFinder(k);
        
        // Will contain top preference of all voters
        ballot = new String [peers.length];
        // Store your own top preference
        ballot[pid] = k.substring(0, 1);
        
        // Contains my vote ranking
        myVoteRank = k.split("");
        
  
        maxScore = 0;
        maxRank = null;
        randomGenerator = new Random();
        
        secure = new SecureTransfer();
        
        
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.pid]);
            registry = LocateRegistry.createRegistry(this.ports[this.pid]);
            stub = (BSCRMI) UnicastRemoteObject.exportObject(this, this.ports[this.pid]);
            registry.rebind("BSC", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        
    }
    
    
    public  static Set<String> permutationFinder(String str) {
        Set<String> perm = new HashSet<String>();
        //Handling error scenarios
        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            perm.add("");
            return perm;
        }
        char initial = str.charAt(0); // first character
        String rem = str.substring(1); // Full string without first character
        Set<String> words = permutationFinder(rem);
        for (String strNew : words) {
            for (int i = 0;i<=strNew.length();i++){
                perm.add(charInsert(strNew, initial, i));
            }
        }
        return perm;
    }

    public static String charInsert(String str, char c, int j) {
        String begin = str.substring(0, j);
        String end = str.substring(j);
        return begin + c + end;
    }
    
    


	@Override
	public Response Vote(Request req) throws RemoteException {
		ballot[req.getPid()] = req.getVoteValue();
		try {
			// Received encrypted vote
			// Respond with your own encrypted vote
			Response resp = new Response(pid, secure.encrypt(ballot[pid]));
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
						for (String a: ballot){
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
					ballot[req.getPid()] = req.getSecure().decrypt(ballot[req.getPid()]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
	}
	
	
	
    public void Start(){
        // Your code here
        try{
            runner = new Thread(this);
            runner.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
        }
    }
    
    
	@Override
	public void run() {
		// TODO
		/*
		After exchanging their votes using the secret voting mechanism (briefly discussed in Section
				7), the processes participate in O(f) rounds of agreement to ensure that all the good processes agree on the
				same ballot. If the agreement protocol takes m rounds, then the overall message complexity is O(mn3
				).
				
		*/
		
		System.out.println("Starting vote exchange");
		exchangeVotes(ballot[pid], VoteType.MY_TOP);
		System.out.println("Votes exchanged securely");
		
		
		// TODO: O(f) rounds of agreement to make sure all correct processes agree on the same ballot
		String topTwoVotes = TopTwoVotes();
		exchangeVotes(topTwoVotes, VoteType.TOP_TWO);
		
		// a a a a b b b c   a a a
		// a a a a b b b c   c c c
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (String a: ballot){
			sb.append(a + " ");
		}
		sb.append(']');
		System.out.println("Votes in pid (after decryption): " + pid);
		System.out.println(sb.toString());
		System.out.println("");
	}
	
	
	private String TopTwoVotes() {
		
		return null;
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
	
	
	
	public void exchangeVotes(String vote, VoteType voteType){
		receivedVotes = receivedKeys = receivedDones = 0;
	
        // Step 1: Send first choice with all
        // We assume that all processes start the democratic election
        // This assumption should not affect the outcome as we can always trigger votes
        // if a random node starts the election instead of all of them
        for(int j = pid; j < peers.length; ++j){
        	
            if(j == pid)
                continue;

            try{
            	 Request request = new Request(pid, secure.encrypt(ballot[pid]));
            	 secure.decrypt(secure.getKey(), secure.encrypt(ballot[pid]));
            	 Response response = Call("Vote", request, j);
            	 // Received encrypted vote as response
            	 // Store this vote
            	 ballot[response.getPid()] = response.getVote();
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
				e.printStackTrace();
			}
        	
        }
	}
	    
}
