package algorithms;

import java.util.HashMap;

public class OrigKYoung extends BSW{

	public OrigKYoung(int pid, String[] peers, int[] ports, String k, int faults) {
		super(pid, peers, ports, k, faults, new BSWScheme(){

			@Override
			public String runAlgoritm(String[] ballot, int faulty) {
				// TODO Auto-generated method stub
				return null;
			}});

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

	void runKemenyYoung(){
    	for (String ranking: P.toArray(new String[P.size()])){
    		int score = KYoungScore(ranking.split(""), finalBallot);
    		if (score > maxScore){
    			maxScore = score;
    			maxRank = ranking.split("");
    		}
    	}
    	
    }

	/*@Override
	void scheme() {
		runKemenyYoung();
	}*/

	
	
}
