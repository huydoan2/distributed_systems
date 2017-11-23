
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//import ElectionUtils.*;
//import VoteScore.*;

public class PrunedKemenyYoung implements BSWScheme {

	@Override
	public String runAlgoritm(String[] ballot, int faulty) {
		HashSet<String> P = (HashSet<String>) ElectionUtils.permutationFinder(ballot[0]);
		int score = 0;
		int maxScore = 0;
		String maxRank = null;
    	for (String ranking: P.toArray(new String[P.size()])){
    		score = 0;
    		List<String> prunedBallot = prunedBallot(ranking, ballot, faulty);
    		score += KYoungScore(ranking.split(""), prunedBallot.toArray(new String[prunedBallot.size()]));
    		
    		if (score > maxScore){
    			maxScore = score;
    			maxRank = ranking;
    		}
    	}
		return maxRank;
	}

	List<String> prunedBallot(String ranking, String [] ballot, int faulty){
		List<VoteScore> scoreList = new ArrayList<VoteScore>();
		
		for (String vote: ballot){
			if (vote == null)
				System.out.println("FOUND NULL BALLOT");
			scoreList.add(new VoteScore(vote, KYoungScore(ranking.split(""), new String[] {vote})));		
		}
		VoteScore [] resArray = scoreList.toArray(new VoteScore[scoreList.size()]);					
		Arrays.sort(resArray, Collections.reverseOrder());
		int i = 0;
		List<String> resultArray = new ArrayList<String>();
		for (VoteScore s: resArray){
			
			resultArray.add(s.vote);
			i++;
			if (i == (ballot.length - faulty))
				break;
		}
			
		return resultArray;
		
	}
	
	
	int KYoungScore(String[] ranking, String[] ballot){
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
	 	}
}
