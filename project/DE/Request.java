package DE;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Request implements Serializable {
	static final long serialVersionUID=1L;
	
	private String voteValue;
	private int pid;
	private boolean doneReq;
	private byte[] secretKey;
	private byte [] IV;
	
	private SecureTransfer secure;
	private VoteType voteType = VoteType.MY_VOTE;
	private String firstChoice, secondChoice;
	private String [] ballot;
	private HashMap<Integer, List<String>> topChoices;
	
	
	
	public Request(int pid, String voteValue){
		this.pid = pid;
		this.voteValue = voteValue;
		
	}
	
	public Request(int pid, boolean done){
		this.pid = pid;
		this.doneReq = done;
		
	}
	
	public Request(int pid, byte[] key){
		this.pid = pid;
		this.secretKey = key;
	}
	
	public Request(int pid, SecureTransfer secure){
		this.pid = pid;
		this.secure = secure;
	}
	
	public Request(int pid, byte [] key, byte [] IV){
		this.IV = IV;
		this.secretKey = key;
		this.pid = pid;
	}

	
	public Request(int pid, String firstChoice, String secondChoice) {
		voteType = VoteType.TOP_TWO;
		this.firstChoice = firstChoice;
		this.secondChoice = secondChoice;
	}
	
	/*public Request(int pid, ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> topChoices) {
		voteType = VoteType.TOP_TWO;
		this.pid = pid;
		this.topChoices = topChoices;
	}*/
	

	public Request(int pid, String[] ballot) {
		this.pid = pid;
		this.ballot = ballot;
	}

	public Request(int pid, HashMap<Integer, List<String>> topTwoVotes) {
		this.pid = pid;
		this.topChoices = topTwoVotes;
		this.voteType = VoteType.TOP_TWO;
	}
	
	public HashMap<Integer, List<String>> getTopTwoVotes(){
		return this.topChoices;
	}

	public int getPid(){
		return pid;
	}
	
	
	
	public String getVoteValue() {
		return voteValue;
	}
	
	public boolean isDoneReq(){
		return doneReq;
	}
	
	public byte[] getKey(){
		return secretKey;
	}
	
	public byte [] getIV(){
		return IV;
	}
	
	public SecureTransfer getSecure(){
		return secure;
	}
	
	public String getFirstChoice(){
		return firstChoice;
	}
	
	public String getSecondChoice(){
		return secondChoice;
	}
	
	public VoteType getVoteType(){
		return voteType;
	}
	
	public String [] getBallot(){
		return ballot;
	}
}
