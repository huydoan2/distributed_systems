package DE;

import java.io.Serializable;

public class Response implements Serializable {
	static final long serialVersionUID=2L;
	private Integer vote;

	Response(Integer value){
		this.vote = value;
	}

	public int getVote(){
		return this.vote;
	}
}
