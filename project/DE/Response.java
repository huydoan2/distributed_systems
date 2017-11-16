package DE;

import java.io.Serializable;

public class Response implements Serializable {
	static final long serialVersionUID=2L;
	
	private int pid;
	String vote;
	Response(int pid, String value){
		this.vote = value;
		this.pid = pid;
	}

	public String getVote(){
		return vote;
	}
	
	public int getPid(){
		return pid;
	}
}
