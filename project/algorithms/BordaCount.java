package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import util.VoteScore;

public class BordaCount implements BSWScheme {

	@Override
	public String runAlgoritm(String[] ballot, int faulty) {
		String result = "";
		ArrayList<VoteScore> ranking = new ArrayList<VoteScore>();
		HashMap<String, Integer> scores = new HashMap<>();
		for (String s: ballot){
			if (s == null)
				continue;
			for (int i = 0; i < s.length(); ++i){
				Integer occur = scores.get(s.substring(i, i+1));
				scores.put(s.substring(i, i+1), occur != null ? occur + i : i);
			}
		}
		for (String s: scores.keySet()){
			ranking.add(new VoteScore(s, scores.get(s)));
		}
		Collections.sort(ranking);
		for (VoteScore vs: ranking){
			result += vs.vote;
		}
		return result;
	}

}
