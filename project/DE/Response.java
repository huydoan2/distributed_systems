package DE;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Response implements Serializable {
	static final long serialVersionUID=2L;
	
	private int pid;
	String vote;
	String firstChoice, secondChoice;
	String [] ballot;
	HashMap<Integer, List<String>> topVotes;
	public Response(int pid, String value){
		this.vote = value;
		this.pid = pid;
	}

	public Response(int pid, String firstChoice, String secondChoice){
		this.pid = pid;
		this.firstChoice = firstChoice;
		this.secondChoice = secondChoice;
	}
	
	public Response(int pid, String[] ballot) {
		this.pid = pid;
		this.ballot = ballot;
	}

	public Response(int pid, HashMap<Integer, List<String>> topVotes) {
		this.pid = pid;
		this.topVotes = topVotes;
	}
	
	public HashMap<Integer, List<String>> getTopVotes(){
		return topVotes;
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
	
	public String [] getBallot(){
		return ballot;
	}
}
