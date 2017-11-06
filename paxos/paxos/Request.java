package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=1L;
    // Your data here
    private int seq, proposal, maxDone = -1;
    private Object value;

    // Your constructor and methods here
    public Request(int seq, int proposal) {
        this.seq = seq;
        this.proposal = proposal;
        this.value = null;
    }

    public Request(int seq, int proposal, Object value) {
        this.seq = seq;
        this.proposal = proposal;
        this.value = value;

    }
    
    public Request(int seq, int proposal, int maxDone, Object value) {
    	this.seq = seq;
        this.proposal = proposal;
        this.value = value;
        this.maxDone = maxDone;
    }

    public int getSeq(){
        return this.seq;
    }

    public int getProposal() { return this.proposal; }

    public Object getValue() { return this.value; }
    
    public int getMaxDone() { return this.maxDone; }
}
