package algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import util.MapUtil;

public class PrunedKYoung extends KYoungScheme{

	public PrunedKYoung(int pid, String[] peers, int[] ports, String k, int faults) {
		super(pid, peers, ports, k, faults);

	}
	
	int KYoungScore(String[] ranking, String[] ballot){
	 	   int score = 0;

	 	   HashMap<String, Integer> mRank = new HashMap<String, Integer>();
	 	   for(int i = 0;i < ballot.length; ++i)
	 	      mRank.put(ballot[i], i);


	 	   for(int i = 0; i < ranking.length - 1; ++i){
	 	      for(int j = i+1; j < ranking.length; ++j){
	 	         if(mRank.get(ranking[i]) > mRank.get(ranking[j]))
	 	            ++score;
	 	      }
	 	   }

	 	   return  score;
	 	}

		// TODO: Fix this
		HashSet<String> prunedBallot(){
			Map<String, Integer> F = new HashMap<String, Integer>();
			
			for (String ranking: P.toArray(new String[P.size()])){
				F.put(ranking, KYoungScore(ranking.split(""), finalBallot));
			}
			F =  MapUtil.sortByValue(F);
			
			return P;
			
		}
		void runPrunedKemenyYoung(){
			maxScore = 0;
			maxRank = null;
	    	for (String ranking: P.toArray(new String[P.size()])){
	    		int score = KYoungScore(ranking.split(""), finalBallot);
	    		if (score > maxScore){
	    			maxScore = score;
	    			maxRank = ranking.split("");
	    		}
	    	}
	    	
	    }

	@Override
	void scheme() {
		runPrunedKemenyYoung();
	}

}
