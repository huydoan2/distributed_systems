package DE;

import java.io.Serializable;

public class Response implements Serializable {
	static final long serialVersionUID=2L;
	
	private int pid;
	String vote;
	String firstChoice, secondChoice;
	
	public Response(int pid, String value){
		this.vote = value;
		this.pid = pid;
	}

	public Response(int pid, String firstChoice, String secondChoice){
		this.pid = pid;
		this.firstChoice = firstChoice;
		this.secondChoice = secondChoice;
	}
	
	public String getVote(){
		return vote;
	}
	
	public int getPid(){
		return pid;
	}
	
	public String getFirstChoice(){
		return firstChoice;
	}
	
	public String getSecondChoice(){
		return secondChoice;
	}
}
