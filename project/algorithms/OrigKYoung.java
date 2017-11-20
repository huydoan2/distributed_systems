package algorithms;

import java.util.HashMap;

public class OrigKYoung extends KYoungScheme{

	public OrigKYoung(int pid, String[] peers, int[] ports, String k, int faults) {
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

	void runKemenyYoung(){
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
		runKemenyYoung();
	}

	
	
}
