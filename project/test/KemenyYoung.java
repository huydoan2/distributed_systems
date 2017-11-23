
import java.util.HashMap;
import java.util.HashSet;

//import ElectionUtils.*;

public class KemenyYoung implements BSWScheme {

	@Override
	public String runAlgoritm(String[] ballot, int faulty) {
		
		HashSet<String> P = (HashSet<String>) ElectionUtils.permutationFinder(ballot[0]);
		
		String maxRank = null;
		int maxScore = 0;
		for (String ranking: P.toArray(new String[P.size()])){
    		int score = KYoungScore(ranking.split(""), ballot);
    		if (score > maxScore){
    			maxScore = score;
    			maxRank = ranking;
    		}
    	}
		
		return maxRank;
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
