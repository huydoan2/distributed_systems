package algorithms;

public class PairwiseComparison implements BSWScheme {

	@Override
	public String runAlgoritm(String[] ballot, int faulty) {
		String candidates = ballot[0];
		String result = candidates.substring(0, 1);
		for (int i = 1; i < candidates.length(); ++i){
			boolean added = false;
			for (int j = 0; j < result.length(); ++j){
				if (pairwiseScore(candidates.substring(i,i+1), result.substring(j, j+1), ballot) >
					pairwiseScore(result.substring(j, j+1), candidates.substring(i, i+1), ballot)) {
					result = result.substring(0, j) + candidates.charAt(i) + result.substring(j);
					++j;
					added = true;
				}
			}
			if (!added)
				result += candidates.charAt(i);
		}
		
		return result;
	}
	
	int pairwiseScore(String a, String b, String [] ballot){
		int score = 0;
		
		for (String s: ballot){
			String cleaned = s.replaceAll("[^" + a + b + "]", "");
			if (cleaned.equals(a+b))
				score++;
		}
		return score;
	}

}
