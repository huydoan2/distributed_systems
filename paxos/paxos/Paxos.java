package paxos;
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

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    AtomicInteger currentProposal;
    AtomicInteger highestInstanceSequence;
    AtomicInteger myMaxDone, paxosMaxDone;
    
    // Paxos DS
    ConcurrentHashMap<Integer, PaxosProposer> sequenceToProposer;
    ConcurrentHashMap<Integer, PaxosAcceptor> sequenceToAcceptor;
    ConcurrentHashMap<Integer, PaxosDecision> sequenceToDecision;
    ConcurrentHashMap<Integer, Integer> sequenceToDone;
    
    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);


        // Your initialization code here
        this.currentProposal = new AtomicInteger(1);
        highestInstanceSequence = new AtomicInteger(-1);
        myMaxDone = new AtomicInteger(-1);
        paxosMaxDone = new AtomicInteger(-1);
        // Testing
        sequenceToProposer = new ConcurrentHashMap<>();
        sequenceToAcceptor = new ConcurrentHashMap<>();
        sequenceToDecision = new ConcurrentHashMap<>();

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
        // Your code here
        try{
            mutex.lock();
            if (seq > highestInstanceSequence.get()) {
            	highestInstanceSequence.set(seq);
            }
            Thread t = new Thread(new PaxosRunner(this, seq, this.currentProposal.get(), value));
            t.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public void run(){
        // Using PaxosRunner to run it
        }


    // RMI handler
    public Response Prepare(Request req){
       Response resp;
       
       // Create acceptor for paxos instance if it doesn't exist
       if (! this.sequenceToAcceptor.containsKey(req.getSeq())) {
    	   PaxosAcceptor acceptor = new PaxosAcceptor();
    	   this.sequenceToAcceptor.put(req.getSeq(), acceptor);
       }
       
       // If request proposal higher than all seen proposal
       if (req.getProposal() > this.sequenceToAcceptor.get(req.getSeq()).preparedProposalNum) {
    	   this.sequenceToAcceptor.get(req.getSeq()).preparedProposalNum = req.getProposal();
    	   resp = new Response(req.getSeq(), this.sequenceToAcceptor.get(req.getSeq()).acceptedProposalNum, this.sequenceToAcceptor.get(req.getSeq()).value);
       }
       else {
    	   resp = new Response(req.getSeq(), req.getProposal(), null, true);
       }
       return resp;
       
    }

    public Response Accept(Request req){
        // ACCEPT IF THIS IS TRUE
        Response resp;
        if (req.getMaxDone() != -1 && req.getMaxDone() < myMaxDone.get()){
        	myMaxDone.set(req.getMaxDone());
        }
        
        if (req.getProposal() >= this.sequenceToAcceptor.get(req.getSeq()).preparedProposalNum){
        	this.sequenceToAcceptor.get(req.getSeq()).preparedProposalNum = req.getProposal();
        	this.sequenceToAcceptor.get(req.getSeq()).acceptedProposalNum = req.getProposal();
        	this.sequenceToAcceptor.get(req.getSeq()).value = req.getValue();
        	resp = new Response(req.getSeq(), req.getProposal(), myMaxDone.get(), req.getValue());
        }
        else {
        	resp = new Response(req.getSeq(), this.sequenceToAcceptor.get(req.getSeq()).preparedProposalNum, myMaxDone.get(), null, true);
        }

        return resp;

    }

    public Response Decide(Request req){
    	if (! this.sequenceToDecision.containsKey(req.getSeq())) {
    		PaxosDecision decision = new PaxosDecision();
    		decision.state = State.Decided;
    		decision.value = req.getValue();
    		this.sequenceToDecision.put(req.getSeq(), decision);
    	}
    	else {
    		this.sequenceToDecision.get(req.getSeq()).state = State.Decided;
    		this.sequenceToDecision.get(req.getSeq()).value = req.getValue();
    	}
    	
    	if (req.getMaxDone() != -1 && req.getMaxDone() <= this.myMaxDone.get()){
    		this.paxosMaxDone.set(req.getMaxDone());
    		removeStaleData();
    	}
    	return null;
    }

    public void removeSequence(int seq){
    	this.sequenceToProposer.remove(seq);
    	this.sequenceToAcceptor.remove(seq);
    	this.sequenceToDecision.remove(seq);
    	this.sequenceToDone.remove(seq);
    }
    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
    	
    	if (seq > myMaxDone.get()) {
    		myMaxDone.set(seq);
    	}
    	
    	
        
    }
    
    private void removeStaleData(){
    	
    	ArrayList<Integer> sequences = new ArrayList<Integer>(Max());
    	Collections.sort(sequences);
    	for (int i: sequences){
    		if (i > this.paxosMaxDone.get())
    			break;
    		removeSequence(i);
    	}
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Your code here
        return highestInstanceSequence.get();
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        return paxosMaxDone.get() + 1;

    }

    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Your code here
    	if (this.sequenceToDecision.containsKey(seq))
    		return new retStatus(this.sequenceToDecision.get(seq).state, this.sequenceToDecision.get(seq).value);
    	return new retStatus(State.Pending, null);
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }
   
    class PaxosProposer {
    	public int myProposalNum, numOfAccepts, highestProposalSeen;
    	public Object myValue = null, proposedValue = null;
    }
    
    class PaxosAcceptor {
    	public Object value = null;
    	public int acceptedProposalNum = -1;
    	public int preparedProposalNum = -1;
    }
    
    class PaxosDecision {
    	public Object value = null;
    	public State state = State.Pending;
    }
    
    class PaxosRunner implements Runnable {
    	private final int threadSequence;
    	private Paxos paxosInstance;
    	PaxosProposer proposer;
    	PaxosAcceptor acceptor;
    	PaxosDecision decision;
    	public PaxosRunner(Paxos paxos, int seq, int currentProposal, Object value) {
    		this.threadSequence = seq;
    		this.paxosInstance = paxos;

    		// Initialize the proposer, acceptor, decider for this paxos instance w/ sequence
    		proposer = new PaxosProposer();
    		proposer.myValue = value;
    		proposer.highestProposalSeen = -1;
    		proposer.myProposalNum = currentProposal;
    		proposer.numOfAccepts = 0;
    		acceptor = new PaxosAcceptor();
    		decision = new PaxosDecision();
    		decision.state = State.Pending;
    		decision.value = null;
    		paxosInstance.sequenceToProposer.put(seq, proposer);
    		paxosInstance.sequenceToAcceptor.put(seq, acceptor);
    		paxosInstance.sequenceToDecision.put(seq, decision);
    	}
    	
		@Override
		public void run() {
		
			// Decider on this instance is Pending
			while (paxosInstance.sequenceToDecision.get(threadSequence).state == State.Pending){
				if (paxosInstance.isDead()){
					return;
				}
				boolean startNewProposal = false;
				proposer.numOfAccepts = 0;
				Request proposalRequest = new Request(threadSequence, proposer.myProposalNum);
				for (int i = 0; i < paxosInstance.peers.length; ++i) {
					Response response;
					if (i == paxosInstance.me)
						response = paxosInstance.Prepare(proposalRequest);
					else
						response = Call("Prepare", proposalRequest, i);
					try{
						if (response.isRejected()) {
							proposer.myProposalNum = response.getProposal() + 1;
							startNewProposal = true;
							break;
						}
					}
					catch (Exception e) {
						continue;
					}
					
					proposer.numOfAccepts++;
					if (response.getValue() != null) {
						if (response.getProposal() > proposer.highestProposalSeen) {
							proposer.highestProposalSeen = response.getProposal();
							proposer.proposedValue = response.getValue();
						}
					}
				}
				// Start proposing again, if rejected by 1
				if (startNewProposal)
					continue;
				
				// Majority accepted proposal
				if (proposer.numOfAccepts > Math.floor(paxosInstance.peers.length/2.0)){
					// Count how many acceptances from the beginning
					proposer.numOfAccepts = 0;
					if (proposer.proposedValue == null)
						proposer.proposedValue = proposer.myValue;
					// Send maxDone here
					Request acceptRequest = new Request(threadSequence, proposer.myProposalNum, paxosInstance.myMaxDone.get(), proposer.proposedValue);
					// Send Accept messages to acceptors
					for (int i = 0; i < paxosInstance.peers.length; ++i){
						Response response;
						// Get max from all here
						if (i == paxosInstance.me)
							response = paxosInstance.Accept(acceptRequest);
						else
							response = Call("Accept", acceptRequest, i);
						if (response == null){
							continue;
						}
						if (!response.isRejected())
							proposer.numOfAccepts++;
						
						if (response.getMaxDone() != -1 && response.getMaxDone() < paxosInstance.myMaxDone.get()) {
							paxosInstance.myMaxDone.set(response.getMaxDone());
						}
							
					}
					
					if (proposer.numOfAccepts > Math.floor(paxosInstance.peers.length/2.0)){
						// Count how many acceptances from the beginning
						proposer.numOfAccepts = 0;
						
						Request decisionRequest = new Request(threadSequence, proposer.myProposalNum, paxosInstance.myMaxDone.get(), proposer.proposedValue);
						// Send Accept messages to acceptors
						for (int i = 0; i < paxosInstance.peers.length; ++i){
							// Run Done here, to remove old instances
							if (i == paxosInstance.me)
								paxosInstance.Decide(decisionRequest);
							else
								Call("Decide", decisionRequest, i);
							
								
						}
					}
				}
				
				proposer.myProposalNum++;

			}
			try {
				paxosInstance.mutex.lock();
				if (proposer.myProposalNum > paxosInstance.currentProposal.get())
					paxosInstance.currentProposal.set(proposer.myProposalNum + 1);
			}
			catch (Exception e) {
				
			}
			finally {
				paxosInstance.mutex.unlock();
			}
			
		}
    	
    }

}
