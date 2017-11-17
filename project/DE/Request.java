package DE;

import java.io.Serializable;

public class Request implements Serializable {
	static final long serialVersionUID=1L;
	
	private String voteValue;
	private int pid;
	private boolean doneReq;
	private byte[] secretKey;
	private SecureTransfer secure;
	private VoteType voteType = VoteType.MY_TOP;
	private String firstChoice, secondChoice;
	
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
	
	public Request(int pid, String firstChoice, String secondChoice) {
		voteType = VoteType.TOP_TWO;
		this.firstChoice = firstChoice;
		this.secondChoice = secondChoice;
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
}
