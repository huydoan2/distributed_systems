
public class VoteScore implements Comparable<VoteScore>{
	public int score;
	public String vote;
	public VoteScore(String vote, int score){
		this.score = score;
		this.vote = vote;
	}
	

	@Override
	public int compareTo(VoteScore other) {
		if (other.score != this.score)
			return Integer.compare(this.score, other.score);
		return vote.compareTo(other.vote);
	}
}