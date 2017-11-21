package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import util.VoteScore;

public class PlacePlurality implements BSWScheme {

	@Override
	public String runAlgoritm(String[] ballot, int faulty) {
		
		ArrayList<VoteScore> ranking;
		HashSet<String> agreedValues = new HashSet<>();
		String result = "";
		
		for (int i = 0; i < ballot[0].length(); ++i){
			HashMap<String, Integer> mapCounter = new HashMap<>();
			ranking = new ArrayList<VoteScore>();
			
			for (int j = 0; j < ballot.length; ++j){
				Integer occur = mapCounter.get(ballot[j].substring(i, i+1));
				mapCounter.put(ballot[j].substring(i, i+1), occur != null ? occur + 1 : 1);
			}
			for (String s: mapCounter.keySet()){
				ranking.add(new VoteScore(s, mapCounter.get(s)));
			}
			Collections.sort(ranking, Collections.reverseOrder());
			for (VoteScore vs: ranking){
				if (!agreedValues.contains(vs.vote)){
					result += vs.vote;
					agreedValues.add(vs.vote);
					break;
				}
					
			}
		}
		return result;
	}

}
