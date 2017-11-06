package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    private int seq, proposal, maxDone = -1;
    private Object value;
    boolean rejected;

    public Response(int seq, int proposal, Object value){
        this.proposal = proposal;
        this.seq = seq;
        this.value = value;
        this.rejected = false;
    }

    public Response(int seq, int proposal, int maxDone, Object value){
        this.proposal = proposal;
        this.seq = seq;
        this.value = value;
        this.rejected = false;
        this.maxDone = maxDone;
    }
    
    public Response(int seq, int proposal, Object value, boolean rejected){
        this.proposal = proposal;
        this.seq = seq;
        this.value = value;
        this.rejected = rejected;
    }
    
    public Response(int seq, int proposal, int maxDone, Object value, boolean rejected){
        this.proposal = proposal;
        this.seq = seq;
        this.value = value;
        this.rejected = rejected;
        this.maxDone = maxDone;
    }
    
    

    Object getValue() {
        return this.value;
    }

    int getProposal() {
        return this.proposal;
    }

    int getSeq() {
        return this.seq;
    }

    boolean isRejected() {
        return rejected;
    }
    
    public int getMaxDone(){
    	return maxDone;
    }

    // Your constructor and methods here
}
