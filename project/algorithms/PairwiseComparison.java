package algorithms;

import java.util.HashSet;

public class PairwiseComparison implements BSWScheme {

	@Override
	public String runAlgoritm(String[] ballot, int faulty) {
		//TODO: Fix this
		String candidates = ballot[0];
		HashSet<String> pairs = new HashSet<String>();
		for (int i = 0; i < candidates.length() - 1; ++i){
			for (int j = i + 1; j < candidates.length(); ++j){
				pairs.add(""+candidates.charAt(i)+candidates.charAt(j));
				pairs.add(""+candidates.charAt(j)+candidates.charAt(i));
			}
		}
		
		StringBuilder result = new StringBuilder();
		result.append(candidates.charAt(0));
		
		
		
		for (int i = 1; i < candidates.length(); ++i){
			boolean added = false;
			int score = 0;
			boolean before = false;
			char a = result.charAt(0);
			for (int j = 0; j < result.length(); ++j){
				
				
				int scoreBefore = pairwiseScore(candidates.charAt(i), result.charAt(j), ballot);
				int scoreAfter  = pairwiseScore(result.charAt(j), candidates.charAt(i), ballot);
				if (scoreBefore > score){
					before = true;
					score = scoreBefore;
					a = result.charAt(j);
				}
				if (scoreAfter > score){
					before = false;
					score = scoreAfter;
					a = result.charAt(j);
				}
				
				/*if (pairwiseScore(candidates.substring(i,i+1), result.substring(j, j+1), ballot) >
					pairwiseScore(result.substring(j, j+1), candidates.substring(i, i+1), ballot)) {
					result = result.substring(0, j) + candidates.charAt(i) + result.substring(j);
					++j;
					added = true;
				}*/
			}
			int index = result.toString().indexOf(a);
			if (before){
				result.insert(index, candidates.charAt(i));
			}
			else {
				result.insert(index+1, candidates.charAt(i));
			}
			/*
			if (!added)
				result += candidates.charAt(i);*/
		}
		
		return result.toString();
	}
	
	int pairwiseScore(char a, char b, String [] ballot){
		int score = 0;
		
		for (String s: ballot){
			String cleaned = s.replaceAll("[^" + a + b + "]", "");
			if (cleaned.equals(""+a+b))
				score++;
		}
		return score;
	}

}
