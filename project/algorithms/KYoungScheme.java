package algorithms;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
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
	ConcurrentHashMap<String, Integer> topChoices;
	
	String [] ballot;      // will contain agreed upon ballot
	String [] myVoteRank;
    int pid;               // me
    int faults;
    Random randomGenerator;
    SecureTransfer secure;
    
    int maxScore;
    String maxRank;
    
    
    // Variables for exchanging votes
    AtomicInteger receivedVotes, receivedKeys, receivedDones;
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
        
        topChoices = new ConcurrentHashMap<String, Integer>();
        
        maxScore = 0;
        maxRank = null;
        randomGenerator = new Random();
        receivedVotes = new AtomicInteger(0);
		receivedKeys = new AtomicInteger(0);
		receivedDones = new AtomicInteger(0);
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
		
		try {
			// Received encrypted vote
			// Respond with your own encrypted vote
			Response resp;
			if (req.getVoteType() == VoteType.MY_TOP){
				ballot[req.getPid()] = req.getVoteValue();
				resp = new Response(pid, secure.encrypt(ballot[pid]));

			}
			else {
				String topVotes = TopTwoVotes();
				Integer occur = topChoices.get(req.getFirstChoice());
				topChoices.put(req.getFirstChoice(), occur != null ? occur+1 : 1);
				occur = topChoices.get(req.getSecondChoice());
				topChoices.put(req.getSecondChoice(), occur != null ? occur+1 : 1);
				
				resp = new Response(pid, topVotes.split("")[0],topVotes.split("")[1]);
			}
				
			this.receivedVotes.getAndIncrement();
			return resp;	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Response Done(Request req) throws RemoteException {
		this.receivedDones.getAndIncrement();
		return null;
	}

	@Override
	public Response Key(Request req) throws RemoteException {
		// Print out votes when all received and non decrypted
				if (this.receivedKeys.get() == 0) {
					try{
						KYoungScheme.mutex.lock();
						/*StringBuilder sb = new StringBuilder();
						sb.append("[ ");
						for (String a: ballot){
							sb.append(a + " ");
						}
						sb.append(']');
						System.out.println("Votes in pid (before decryption): " + pid);
						System.out.println(sb.toString());
						System.out.println("");*/
					} catch (Exception e){
						
					}
					finally {
						KYoungScheme.mutex.unlock();
					}
					
				}
				try {
					this.receivedKeys.getAndIncrement();
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
		/*
		After exchanging their votes using the secret voting mechanism (briefly discussed in Section
				7), the processes participate in O(f) rounds of agreement to ensure that all the good processes agree on the
				same ballot. If the agreement protocol takes m rounds, then the overall message complexity is O(mn3
				).
				
		*/
		
		//System.out.println("Starting vote exchange");
		
 		exchangeVotes(ballot[pid], VoteType.MY_TOP);
		//System.out.println("Votes exchanged securely");
		
		
		// O(f) rounds of agreement to make sure all correct processes agree on the same ballot
		String topTwoVotes = TopTwoVotes();
		//System.out.println("Starting top 2 vote exchange");
		
		exchangeVotes(topTwoVotes, VoteType.TOP_TWO);
		//System.out.println("Finished top 2 vote exchange");
		
		
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
		HashMap<String, Integer> counterMap = new HashMap<String, Integer>();
		
		for (String str: ballot){
			Integer c = counterMap.get(str);
			counterMap.put(str, c != null ? c+1 : 1);
		}
		int first = 0, second = 0;
		String f = null, s = null;
		for (String a: counterMap.keySet()){
			if (counterMap.get(a) > first){
				second = first;
				s = f;
				first = counterMap.get(a);
				f = a;
			}
			else if (counterMap.get(a) > second){
				second = counterMap.get(a);
				s = a;
			}
		}
		return f+s;
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
	
	public void updateChoices(String firstChoice, String secondChoice){
		// TODO: Move code here
	}
	
	public void exchangeVotes(String vote, VoteType voteType){

		
		String firstBallot = ballot[pid], secondBallot = null;
		int topFirst = 0, topSecond = 0;
		
        // Step 1: Send first choice with all
        // We assume that all processes start the democratic election
        // This assumption should not affect the outcome as we can always trigger votes
        // if a random node starts the election instead of all of them
        for(int j = pid; j < peers.length; ++j){
        	
            if(j == pid)
                continue;

            String [] votes = vote.split("");
            
            try{
            	
	        	if (voteType == VoteType.MY_TOP){
	        		
	        		//Byzantine
	        		if (pid % 3 == 0) {
	        			Random r = new Random();
	        			String voteVal = myVoteRank[r.nextInt(myVoteRank.length)];
	        			Request request = new Request(pid, secure.encrypt(voteVal));
	        			Response response = Call("Vote", request, j);
	        			ballot[response.getPid()] = response.getVote();
	        		}
	        		else {
	        			Request request = new Request(pid, secure.encrypt(votes[0]));
		        		Response response = Call("Vote", request, j);
			        	// Received encrypted vote as response
			        	// Store this vote
		        		ballot[response.getPid()] = response.getVote();
	        		}
	        		
	        		
	        	}
	        	else {
	        		// Sending top 2 choices to everyone
	        		// TODO: Make this also encrypted??
	        		Request request = new Request(pid, votes[0], votes[1]);
	        		Response response = Call("Vote", request, j);
	        		
	        		// Assign value to second choice if second == null and value isn't equal to first
	        		if (secondBallot == null && !response.getFirstChoice().equals(firstBallot))
	        			secondBallot = response.getFirstChoice();
	        		if (secondBallot == null && !response.getSecondChoice().equals(firstBallot))
	        			secondBallot = response.getSecondChoice();
	        		
	        		
	        		
	        		// Add both choices to topChoices map
	        		Integer occur = topChoices.get(response.getFirstChoice());
	        		topChoices.put(response.getFirstChoice(), occur != null ? occur+1 : 1);
	        		occur = topChoices.get(response.getSecondChoice());
	        		topChoices.put(response.getSecondChoice(), occur != null ? occur+1 : 1);
	        		
	        		
	        		
	        		// If received first choice is better than current first choice, replace it.
	        		// Put old first choice as 2nd choice
	        		if (topChoices.get(response.getFirstChoice()) > topFirst
	        				|| (topChoices.get(response.getFirstChoice()) == topFirst 
	        					&& firstBallot.compareTo(response.getFirstChoice()) < 0)){
	        			
	        			// Move most popular to 2nd most popular
	        			topSecond = topFirst;
	        			secondBallot = firstBallot;
	        			
	        			topFirst = topChoices.get(response.getFirstChoice());
	        			firstBallot = response.getFirstChoice();
	        		}
	        		// If received first choice is better than current second choice, replace it
	        		else if (topChoices.get(response.getFirstChoice()) > topSecond
	        				|| (topChoices.get(response.getFirstChoice()) == topSecond 
        					&& secondBallot.compareTo(response.getFirstChoice()) < 0)){
	        			
	        			topSecond = topChoices.get(response.getFirstChoice());
	        			secondBallot = response.getFirstChoice();
	        			
	        		}
	        		
	        		// If received 2nd choice is better than current first choice, replace it.
	        		// Put old first choice as 2nd choice
	        		if (topChoices.get(response.getSecondChoice()) > topFirst
	        				|| (topChoices.get(response.getSecondChoice()) == topFirst 
	        					&& firstBallot.compareTo(response.getSecondChoice()) < 0)){
	        			
	        			// Move most popular to 2nd most popular
	        			topSecond = topFirst;
	        			secondBallot = firstBallot;
	        			
	        			topFirst = topChoices.get(response.getFirstChoice());
	        			firstBallot = response.getFirstChoice();
	        		}
	        		// If received 2nd is better than current second choice, replace it
	        		else if (topChoices.get(response.getSecondChoice()) > topSecond
	        				|| (topChoices.get(response.getSecondChoice()) == topSecond 
        					&& secondBallot.compareTo(response.getSecondChoice()) < 0)){
	        			
	        			topSecond = topChoices.get(response.getSecondChoice());
	        			secondBallot = response.getSecondChoice();
	        			
	        		}
	        		
	        	}
	        	
	        	receivedVotes.getAndIncrement();

	        		
	        	
            } catch (Exception e) {
            	e.printStackTrace();
            }
            
        }
        
        System.out.println("Top Choice " + pid + ": " + firstBallot);
        System.out.println("Second Choice " + pid + ": " + secondBallot);
        
        // Wait to receive votes from everyone, inform everyone
        //System.out.println("Waiting to get all votes");
        while (this.receivedVotes.get() == 0 || (this.receivedVotes.get() % (peers.length - 1)) != 0) {
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
        while (this.receivedDones.get() == 0 || (this.receivedDones.get() % (peers.length - 1)) != 0) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        if (voteType == VoteType.TOP_TWO){
        	// No need for decryption
        	return;
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
        while (this.receivedKeys.get() == 0 ||  (this.receivedKeys.get() % (peers.length - 1)) != 0) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        }
        
	}
	    
}
