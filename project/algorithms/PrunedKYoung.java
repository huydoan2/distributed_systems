package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PrunedKYoung extends BSW{

	public PrunedKYoung(int pid, String[] peers, int[] ports, String k, int faults) {
		super(pid, peers, ports, k, faults, new BSWScheme(){

			@Override
			public String runAlgoritm(String[] ballot, int faulty) {
				// TODO Auto-generated method stub
				return null;
			}
			
		});

	}
	
	/*int KYoungScore(String[] ranking, String[] ballot){
		int score = 0;
	 	  HashMap<String, Integer> mRank;
	 	   for(int i = 0;i < ballot.length; ++i){
	 		   
	 		   mRank = new HashMap<String, Integer>();
	 		   String [] bal = ballot[i].split("");
	 		   for (int j = 0; j < bal.length; ++j){
	 			  mRank.put(bal[j], j);
	 		   }
	 		   
	 		  for(int x = 0; x < ranking.length - 1; ++x){
	 	 	      for(int y = x+1; y < ranking.length; ++y){
	 	 	         if(mRank.get(ranking[x]) < mRank.get(ranking[y]))
	 	 	            ++score;
	 	 	      }
	 	 	   } 
	 	   }


	 	   return  score;
	 	}*/

		List<String> prunedBallot(String ranking){
			List<VoteScore> scoreList = new ArrayList<VoteScore>();
			
			for (String vote: finalBallot){
				scoreList.add(new VoteScore(vote, BSWAlgos.KYoungScore(ranking.split(""), new String[] {vote})));		
			}
			VoteScore [] resArray = scoreList.toArray(new VoteScore[scoreList.size()]);					
			Arrays.sort(resArray, Collections.reverseOrder());
			int i = 0;
			List<String> resultArray = new ArrayList<String>();
			for (VoteScore s: resArray){
				
				resultArray.add(s.vote);
				i++;
				if (i == (finalBallot.length - this.faulty))
					break;
			}
				
			return resultArray;
			
		}
		void runPrunedKemenyYoung(){
			int score = 0;
			
			maxRank = null;
	    	for (String ranking: P.toArray(new String[P.size()])){
	    		score = 0;
	    		List<String> prunedBallot = prunedBallot(ranking);
	    		score += BSWAlgos.KYoungScore(ranking.split(""), prunedBallot.toArray(new String[prunedBallot.size()]));
	    		
	    		if (score > maxScore){
	    			maxScore = score;
	    			maxRank = ranking.split("");
	    		}
	    	}
	    	
	    }

	/*@Override
	void scheme() {
		//maxRank = BSWAlgos.placePlurality(finalBallot).split("");
		//BSWAlgos.pairwiseComparison(finalBallot);
		//BSWAlgos.bordaCount(finalBallot);
		runPrunedKemenyYoung();
	}*/
	

}

class VoteScore implements Comparable<VoteScore>{
	int score;
	String vote;
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
