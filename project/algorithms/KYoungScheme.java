package algorithms;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
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
	
	int [] binaryConsensus; // Used to choose 1 of 2 options for specific ballot slot

	static CyclicBarrier barrier = null;
	// Voting variables
	// Set of all permutations of k candidates
	public HashSet<String> P;
	ConcurrentHashMap<String, Integer> topChoices;
	HashSet<String> agreedBallot;
	String [] ballot;      // will contain agreed upon ballot
	String [] finalBallot;
	String [] myVoteRank;
    int pid;               // me
    int faults;
    Random randomGenerator;
    SecureTransfer secure;
    
    int maxScore;
    String maxRank;

    // Variables for exchanging votes
    AtomicInteger receivedVotes, receivedKeys, receivedDones, receivedChoices;
    Thread runner;
    
    static ReentrantLock mutex = new ReentrantLock();
    
    public KYoungScheme(int pid, String[] peers, int[] ports, String k, int faults){
        this.pid = pid;
        this.peers = peers;
        this.ports = ports;
        this.faults = faults;
        if (barrier == null || barrier.getParties() != peers.length)
        	barrier = new CyclicBarrier(peers.length);
        
        // Contain all permutations of voter ranks
        P = (HashSet<String>) permutationFinder(k);
        
        // Will contain top preference of all voters
        ballot = new String [peers.length];
        // Store your own top preference
        ballot[pid] = k.substring(0, 1);
        finalBallot = new String[k.length()];
        
        
        // Values in agreedBallot are not considered for topChoices
        // TODO
        agreedBallot = new HashSet<String>();
        
        // Contains my vote ranking
        myVoteRank = k.split("");
        
        topChoices = new ConcurrentHashMap<String, Integer>();
        
        maxScore = 0;
        maxRank = null;
        randomGenerator = new Random();
        receivedVotes = new AtomicInteger(0);
		receivedKeys = new AtomicInteger(0);
		receivedDones = new AtomicInteger(0);
		receivedChoices = new AtomicInteger(0);
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
				mutex.lock();
				Integer occur = topChoices.get(req.getFirstChoice());
				topChoices.put(req.getFirstChoice(), occur != null ? occur+1 : 1);
				occur = topChoices.get(req.getSecondChoice());
				topChoices.put(req.getSecondChoice(), occur != null ? occur+1 : 1);
				mutex.unlock();
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
	
	@Override
	public Response Choice(Request req) throws RemoteException {
		// Returns one or the other, which top choice it prefers
		// Used to convert vote to binary value
		// Only used when the vote from this pid is not one of top two choices
		ballot[req.getPid()] = req.getVoteValue();
		this.receivedChoices.getAndIncrement();
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
		try{
			/*exchangeVotes(ballot[pid], VoteType.MY_TOP);
			barrier.await();*/
			
			for (int i = 0; i < finalBallot.length; ++i){
				
				exchangeVotes(myVoteRank[i], VoteType.MY_TOP);
				barrier.await();
	 			String topTwoVotes = TopTwoVotes();
	 	 		binaryConsensus = new int [peers.length];
	 	 		exchangeVotes(topTwoVotes, VoteType.TOP_TWO);
	 			barrier.await();
	 			
	 			String first = null, second = null;
	 			int firstScore = 0, secondScore = 0;
	 			for (String a: topChoices.keySet()){
	 				if (agreedBallot.contains(a))
	 					continue;
	 				if (topChoices.get(a) > firstScore){
	 					second = first;
	 					secondScore = firstScore;
	 					first = a;
	 					firstScore = topChoices.get(a);
	 				}
	 				else if (topChoices.get(a) > secondScore){
	 					second = a;
	 					secondScore = topChoices.get(a);
	 				}
	 			}
	 			broadcastAndWaitForChoice(first, second);
	 			barrier.await();
	 			for (int j = 0; j < binaryConsensus.length; ++j){
	 				if (ballot[j].equals(first))
	 					binaryConsensus[j] = 0;
	 				else
	 					binaryConsensus[j] = 1;
	 			}
	 			//Run binary byzantine consensus
	 			runByzantine(i, first, second);
	 			barrier.await();
	 			//System.out.println("Finished top 2 vote exchange");
	 		}
			
			
			
		}
		catch (Exception e){
			
		}
 		
 		
		//System.out.println("Votes exchanged securely");
		
		
		// O(f) rounds of agreement to make sure all correct processes agree on the same ballot
		
		//System.out.println("Starting top 2 vote exchange");
		
		/*for (int i = 0; i < ballot.length - 1; ++i){
			exchangeVotes(topTwoVotes, VoteType.TOP_TWO);
		}*/
 		
 		
 		
		
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (String a: finalBallot){
			sb.append(a + " ");
		}
		sb.append(']');
		System.out.println("Votes in pid (after decryption): " + pid);
		System.out.println(sb.toString());
		System.out.println("");
	}
	
	
	private void runByzantine (int ballotNum, String first, String second) {
		// Odd number of votes, pick majority
		int countZero = 0, countOne = 0;
		for (int i: binaryConsensus){
			if (i == 0)
				countZero++;
			else
				countOne++;
		}
		if (countZero >= countOne){
			finalBallot[ballotNum] = first;
			agreedBallot.add(first);
		}
		else{
			finalBallot[ballotNum] = second;
			agreedBallot.add(second);
		}
		
		
		//broadcastAndWaitForDones();
	}


	private void broadcastAndWaitForChoice(String first, String second) {
		
		String choice = "";
		for (String a: myVoteRank){
			if (a.equals(first)){
				choice = first;
				break;
			}
			if (a.equals(second)){
				choice = second;
				break;
			}
		}
		
		for (int i = 0; i < peers.length; ++i){
			
			Request request = new Request(pid, choice);
			if (i == pid){
				ballot[pid] = choice;
			}
			else{
				Call("Choice", request, i);
			}
				
			
		}
		
		while (this.receivedChoices.get() == 0 || (this.receivedChoices.get() % (peers.length - 1)) != 0) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        }
		
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
			if (agreedBallot.contains(a))
				continue;
			
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
            else if (rmi.equals("Choice"))
            	callReply = stub.Choice(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }
	
	public void updateChoices(String first, String second){
		// Add both choices to topChoices map
		mutex.lock();
		Integer occur = topChoices.get(first);
		topChoices.put(first, occur != null ? occur+1 : 1);
		occur = topChoices.get(second);
		topChoices.put(second, occur != null ? occur+1 : 1);
		mutex.unlock();
		
	}
	
	public void exchangeVotes(String vote, VoteType voteType){

		
		String firstBallot = vote.split("")[0], secondBallot = null;
		if (voteType == VoteType.TOP_TWO){
			//TODO: Make sure this is correct
			secondBallot = vote.split("")[1];
			mutex.lock();
			Integer occur = topChoices.get(firstBallot);
			topChoices.put(firstBallot, occur != null ? occur+1 : 1); 
			occur = topChoices.get(secondBallot);
			topChoices.put(secondBallot, occur != null ? occur+1 : 1);
			mutex.unlock();
			
			
		}
			
				
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
	        		
	        		// TODO : Check this
	        		/*if (pid % 3 == 0){
	        			String vote1 = votes[0], vote2 = votes[1];
	        			Random r = new Random();
	        			while (true){
	        				vote1 = myVoteRank[r.nextInt(myVoteRank.length)];
	        				vote2 = myVoteRank[r.nextInt(myVoteRank.length)];
	        				if (!agreedBallot.contains(vote1) && !agreedBallot.contains(vote2))
	        					break;
	        			}
	        			Request request = new Request(pid, vote1, vote2);
		        		Response response = Call("Vote", request, j);
		        		updateChoices(response.getFirstChoice(), response.getSecondChoice());
	        		}*/
	        		//else {
	        			Request request = new Request(pid, votes[0], votes[1]);
		        		Response response = Call("Vote", request, j);
		        		updateChoices(response.getFirstChoice(), response.getSecondChoice());
	        		//}
	        		// Sending top 2 choices to everyone
	        		// TODO: Make this also encrypted??
	        		
	        	}
	        	
	        	receivedVotes.getAndIncrement();
   	
            } catch (Exception e) {
            	e.printStackTrace();
            }
            
        }
        
        
        // Wait to receive votes from everyone, inform everyone
        //System.out.println("Waiting to get all votes");
        waitForVotes();
 	
    	// Wait for everyone to send done
    	broadcastAndWaitForDones();
        
    	// No need for decryption
        if (voteType == VoteType.TOP_TWO){
        	return;
        }
        
		// All have received all votes, broadcast keys
        // Wait to receive votes from everyone, inform everyone
        broadcastAndWaitForKeys();
        
	}


	private void waitForVotes() {
		while (this.receivedVotes.get() == 0 || (this.receivedVotes.get() % (peers.length - 1)) != 0) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        }
		
	}


	private void broadcastAndWaitForDones() {
		
		for (int i = 0; i < peers.length; ++i){
    		if (i == this.pid)
    			continue;
    		// Send done request to all
    		Request request = new Request(this.pid, true);
    		Call("Done", request, i);
    	}
		while (this.receivedDones.get() == 0 || (this.receivedDones.get() % (peers.length - 1)) != 0) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
		
	}


	private void broadcastAndWaitForKeys() {

        for (int i = 0; i < peers.length; ++i){
        	if (i == this.pid)
        		continue;

        	Request request = new Request(this.pid, secure);
        	Call("Key", request, i);
        }
		while (this.receivedKeys.get() == 0 ||  (this.receivedKeys.get() % (peers.length - 1)) != 0) {
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        }
		
	}
	    
}
