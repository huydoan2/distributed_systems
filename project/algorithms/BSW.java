package algorithms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import DE.BSCRMI;
import DE.Request;
import DE.Response;
import DE.SecureTransfer;
import DE.VoteType;
import util.ElectionUtils;

public class BSW implements Runnable, BSCRMI{

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
	ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> topChoices;
	ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> ballotCounter;
	
	//HashSet<String> agreedBallot;
	String [] ballot;      // will contain agreed upon ballot
	String [] finalBallot;
	//String [] myVoteRank;
    int pid;               // me
    int faulty;
    SecureTransfer secure;
    
    int maxScore;
    String [] maxRank;

    // Variables for exchanging votes
    AtomicInteger receivedVotes, receivedKeys;
    AtomicInteger receivedDones, receivedChoices;
    AtomicInteger receivedBallots;
    Thread runner;
    
    static ReentrantLock mutex = new ReentrantLock();
    BSWScheme bswScheme;
    //abstract void scheme();
    
    // Timeout for getting response
    // Makes sure node can tolerate byzantine node not sending any value
    static {
    	try {
			RMISocketFactory.setSocketFactory( new RMISocketFactory()
			{
			    public Socket createSocket( String host, int port )
			        throws IOException
			    {
			        Socket socket = new Socket();
			        socket.setSoTimeout(3000);
			        socket.setSoLinger( false, 0 );
			        socket.connect( new InetSocketAddress( host, port ), 3000);
			        return socket;
			    }

			    public ServerSocket createServerSocket( int port )
			        throws IOException
			    {
			        return new ServerSocket( port );
			    }
			} );
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    public BSW(int pid, String[] peers, int[] ports, String k, int faulty, BSWScheme algo){
        this.pid = pid;
        this.peers = peers;
        this.ports = ports;
        this.faulty = faulty;
        this.bswScheme = algo;
        if (barrier == null || barrier.getParties() != peers.length)
        	barrier = new CyclicBarrier(peers.length);
        
        // Contain all permutations of voter ranks
        P = (HashSet<String>) ElectionUtils.permutationFinder(k);
        
        // Will contain top preference of all voters
        ballot = new String [peers.length];
        // Store your own preference
        ballot[pid] = k;
        finalBallot = new String [peers.length];
        ballotCounter = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>>();
        for (int i = 0; i < peers.length; ++i)
        	ballotCounter.put(i, new ConcurrentHashMap<>());
        
       
        topChoices = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>>();
        
        maxScore = 0;
        maxRank = null;
        receivedVotes = new AtomicInteger(0);
		receivedKeys = new AtomicInteger(0);
		receivedDones = new AtomicInteger(0);
		receivedChoices = new AtomicInteger(0);
		receivedBallots = new AtomicInteger(0);
        secure = new SecureTransfer();
        
        
        
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.pid]);

           	registry = LocateRegistry.createRegistry(this.ports[this.pid]);
           
            stub = (BSCRMI) UnicastRemoteObject.exportObject(this, this.ports[this.pid]);
            registry.rebind("BSC", stub);
        } catch(Exception e){
        	try {
				registry = LocateRegistry.getRegistry(this.ports[this.pid]);
				stub = (BSCRMI) UnicastRemoteObject.exportObject(this, this.ports[this.pid]);
	            registry.rebind("BSC", stub);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
            //e.printStackTrace();
        }
        
        
    }
    

	@Override
	public Response Vote(Request req) throws RemoteException {
		
		try {
			// Received encrypted vote
			// Respond with your own encrypted vote
			Response resp;
			if (req.getVoteType() == VoteType.MY_VOTE){
				ballot[req.getPid()] = req.getVoteValue();
				resp = new Response(pid, secure.encrypt(ballot[pid]));

			}
			else {
				HashMap<Integer, List<String>> topVotes = TopTwoVotes();
				//mutex.lock();
				// Update topChoices
				// TODO: Make this concurrent instead of using locks
				for (Integer i: req.getTopTwoVotes().keySet()){

					for (String s: req.getTopTwoVotes().get(i)){
						if (!topChoices.containsKey(i))
							topChoices.put(i, new ConcurrentHashMap<String, Integer>());
						Integer occur = topChoices.get(i).get(s);
						topChoices.get(i).put(s, occur != null ? occur + 1 : 1);
					}
				}
				//mutex.unlock();
				resp = new Response(pid, topVotes);
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
		try {
			this.receivedKeys.getAndIncrement();
			ballot[req.getPid()] = req.getSecure().
									decrypt(ballot[req.getPid()]);
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
	
	@Override
	public Response Ballot(Request req) throws RemoteException {
		String [] receivedBallot = req.getBallot();
		addBallotToCounter(receivedBallot);
		receivedBallots.incrementAndGet();
		Response response = new Response(pid, ballot);
		return response;
		
	}
    public void Start(){
        // Your code here
        try{
        	maxRank = null;
        	if (runner == null)
        		runner = new Thread(this);
            runner.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
        }
    }
    
    public void cleanUp(){
    	// Will contain top preference of all voters
        //ballot = new String [peers.length];
        // Store your own preference
        
        finalBallot = new String [peers.length];
        ballotCounter = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>>();
        for (int i = 0; i < peers.length; ++i)
        	ballotCounter.put(i, new ConcurrentHashMap<>());
        
       
        topChoices = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>>();
        
        maxScore = 0;
        maxRank = null;
        receivedVotes = new AtomicInteger(0);
		receivedKeys = new AtomicInteger(0);
		receivedDones = new AtomicInteger(0);
		receivedChoices = new AtomicInteger(0);
		receivedBallots = new AtomicInteger(0);
        secure = new SecureTransfer();
    }
    
	@Override
	public void run() {
		
		// Agreeing on final Ballot
		try{
			boolean ballotComplete = true;
			for (String s: ballot)
				if (s == null)
					ballotComplete = false;
			if (!ballotComplete){
				cleanUp();
				barrier.await();
				exchangeVotes();
				exchangeBallots();
					
			}
			
 			/*HashMap<Integer, List<String>> topTwoVotes = TopTwoVotes();
 	 		
 	 		if (ballotNotFinal())
 	 			exchangeTopChoices(topTwoVotes);
 	 		barrier.await();
 	 		for (int i = 0; i < finalBallot.length; ++i){
 	 			if (finalBallot[i] == null) {
 	 				String topChoice = getTopChoice(i);
 	 				finalBallot[i] = topChoice;
 	 			}
 	 				
 	 		}*/
 	 		
 	 		
 	 		
 			barrier.await();

		}
		catch (Exception e){
			e.printStackTrace();
		}
 		
 		
		// Scheme implemented in derived classes
		//scheme();
		if (pid == 0)
			maxRank = bswScheme.runAlgoritm(ballot, faulty).split("");
		
		/*StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (String a: maxRank){
			sb.append(a + " ");
		}
		sb.append(']');
		System.out.println("Kemeny Young Score Result: " + pid);
		System.out.println(sb.toString());
		System.out.println("");*/
	}
	
	

	private String getTopChoice(int i) {
		String firstStr = null;
		int firstScore = 0;
		ConcurrentHashMap<String, Integer> choiceMap = topChoices.get(i);
		for (String s: choiceMap.keySet()){
			if (choiceMap.get(s) > firstScore || (choiceMap.get(s) == firstScore && s.compareTo(firstStr) < 0)){
				firstStr = s;
				firstScore = choiceMap.get(s);
			}
		}
		return firstStr;
	}

	private boolean ballotNotFinal() {
		for (String a : finalBallot)
			if (a == null)
				return true;
		return false;
	}


	private void exchangeBallots() {
		
		for (int i = pid + 1; i < peers.length; ++i){
			Request request = new Request(pid, ballot);
			Response response = Call("Ballot", request, i);
			if (response != null)
				addBallotToCounter(response.getBallot());
			receivedBallots.incrementAndGet();
		}
		
		while (receivedBallots.get() != (peers.length - 1)){
			try {
				Thread.sleep(1000);
				boolean done = true;
				for (String s: ballot){
					if (s == null)
						done = false;
				}
				if (done)
					break;
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	
	}


	private void addBallotToCounter(String [] receivedBallot) {
		for (int i = 0; i < receivedBallot.length; ++i){
			if (ballotCounter.get(i).containsKey(receivedBallot[i])){
				Integer occur = ballotCounter.get(i).get(receivedBallot[i]);
				ballotCounter.get(i).put(receivedBallot[i],occur+1);
				if ((occur + 1) >= (peers.length - faulty)){
					finalBallot[i] = receivedBallot[i];
				}
			}
			else
				ballotCounter.get(i).put(receivedBallot[i], 1);
		}
		
	}


	/*private void runByzantine (int ballotNum, String first, String second) {
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
		
		
	}*/


	/*private void broadcastAndWaitForChoice(String first, String second) {
		
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
			
			
			if (i == pid){
				ballot[pid] = choice;
			}
			else{
				Request request = new Request(pid, choice);
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
		
	}*/


	private HashMap<Integer, List<String>> TopTwoVotes() {
		HashMap<Integer, List<String>> result = new HashMap<>();
		for (int i = 0; i < finalBallot.length; ++i){
			// Only do for ballots which aren't agreed upon
			if (finalBallot[i] == null){
				System.out.println("FOUND NULL BALLOT");
				List<String> res = new ArrayList<String>();
				int firstCount = 0, secondCount = 0;
				String firstStr = null, secondStr = null;
				 for (String possibleChoice: ballotCounter.get(i).keySet()){
					 
					 
					 if (ballotCounter.get(i).get(possibleChoice) > firstCount || 
							 (ballotCounter.get(i).get(possibleChoice) == firstCount && possibleChoice.compareTo(firstStr) < 0)){
						 secondCount = firstCount;
						 secondStr = firstStr;
						 firstCount = ballotCounter.get(i).get(possibleChoice);
						 firstStr = possibleChoice;
					 }
					 else if (ballotCounter.get(i).get(possibleChoice) > secondCount || 
							 (ballotCounter.get(i).get(possibleChoice) == secondCount && possibleChoice.compareTo(secondStr) < 0)){
						 secondCount = ballotCounter.get(i).get(possibleChoice);
						 secondStr = possibleChoice;
					 }
				 }
				
				res.add(firstStr);
				res.add(secondStr);
				result.put(i, res);		
			}
		}
		return result;
	
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
            else if (rmi.equals("Ballot"))
            	callReply = stub.Ballot(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }
	
	public void updateChoices(HashMap<Integer, List<String>> topVotes){
		// Add both choices to topChoices map
		
		//mutex.lock();
		for (Integer i: topVotes.keySet()){
			String first = topVotes.get(i).get(0);
			String second = topVotes.get(i).get(1);
			
			if (!topChoices.containsKey(i))
				topChoices.put(i, new ConcurrentHashMap<String, Integer>());
			
			
			Integer occur = topChoices.get(i).get(first);
			topChoices.get(i).put(first, occur != null ? occur+1 : 1);
			occur = topChoices.get(i).get(second);
			topChoices.get(i).put(second, occur != null ? occur+1 : 1);
		}
		
		//mutex.unlock();
		
	}
	
	public void exchangeTopChoices(HashMap<Integer, List<String>> topTwoVotes){
		
		for (int i = pid + 1; i < peers.length; ++i){
			
			Request request = new Request(pid, topTwoVotes);
			Response response = Call("Vote", request, i);
			if (response != null)
				updateChoices(response.getTopVotes());
		}
		
	}
	
	
	public void exchangeVotes(){		
				
        // Step 1: Send first choice with all
        // We assume that all processes start the democratic election
        // This assumption should not affect the outcome as we can always trigger votes
        // if a random node starts the election instead of all of them
        for(int j = pid + 1; j < peers.length; ++j){
        	            
            try{

        		// Byzantine
        		/*if (pid != 0 && pid % 3 == 0) {
        			// Generate random permutation of vote
        			List<String> byzantineList = Arrays.asList(ballot[pid].split(""));
        			Collections.shuffle(byzantineList);
        			String byzantineVote = String.join("", byzantineList);
        			Request request = new Request(pid, secure.encrypt(byzantineVote));
        			Response response = Call("Vote", request, j);
        			ballot[response.getPid()] = response.getVote();
        		}
        		else {*/
        			Request request = new Request(pid, secure.encrypt(ballot[pid]));
	        		Response response = Call("Vote", request, j);
		        	// Received encrypted vote as response
		        	// Store this vote
	        		
	        		if (response == null){
	        			ballot[j] = null;
	        			System.out.println("FOUND NULL BALLOT");
	        		}
	        		else
	        			ballot[response.getPid()] = response.getVote();
    //    		}
 	
	        	receivedVotes.getAndIncrement();
   	
            } catch (Exception e) {
            	e.printStackTrace();
            }
            
        }
        // Wait to receive votes from everyone, inform everyone
        //System.out.println("Waiting to get all votes");
        //waitForVotes();
    	// Wait for everyone to send done
    	broadcastAndWaitForDones();     
    	// No need for decryption    
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
