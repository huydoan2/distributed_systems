package DE;

import java.io.Serializable;

public class Request implements Serializable {
	static final long serialVersionUID=1L;
	
	private int voteValue;
	private int pid;
	
	Request(int pid, int voteValue){
		this.pid = pid;
		this.voteValue = voteValue;
	}
	
	public int getPid(){
		return pid;
	}
	
	public int getVoteValue() {
		return voteValue;
	}
}
