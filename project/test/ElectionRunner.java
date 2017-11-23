import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;


public class ElectionRunner {

	static void runScheme(String schemeName, String ideal, double goodProb){
		
		
		HashMap<String, Integer> ballotResultCount = new HashMap<>();
		HashSet<String> ballotResult = new HashSet<String>();
		System.out.println("==========================================================");

		System.out.println("Running " + schemeName);
		for (int i = 0; i < 50; ++i){
			BSW [] kys = initIdealVoters(100, ideal, 33, goodProb, 0.9);
			
			
			for (BSW k: kys) {
				if (schemeName.equals("KY"))
					k.bswScheme = new KemenyYoung();
				else if (schemeName.equals("PKY"))
					k.bswScheme = new PrunedKemenyYoung();
				else if (schemeName.equals("PW"))
					k.bswScheme = new PairwiseComparison();
				else if (schemeName.equals("BC"))
					k.bswScheme = new BordaCount();
				else
					k.bswScheme = new PlacePlurality();
				k.Start();
			}
			for (BSW k: kys) {
				try {
					k.runner.join();
					if (k.pid == 0)
						ballotResult.add(String.join("", k.maxRank));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (String s: ballotResult.toArray(new String[ballotResult.size()])){
				Integer count = ballotResultCount.get(s);
				ballotResultCount.put(s, count != null ? count+1 : 1);
			}
			ballotResult.clear();
		}
		int score  = 0;
		for (String s: ballotResultCount.keySet()){
			System.out.println(schemeName + " [" + s + "]: " + ballotResultCount.get(s));
			score += (ballotResultCount.get(s) * distance(ideal, s));
		}
		System.out.println("Good Probability: " + goodProb);
		System.out.println("Candidates: " + ideal.length());
		System.out.println("Total Score: " + score);
		System.out.println("Avg Distance Overall: " + (score / 50.0));
		if (ballotResultCount.get(ideal) != null)
			System.out.println("Avg Distance Bad: " + (score / (50.0 - ballotResultCount.get(ideal))));
		else
			System.out.println("Avg Distance Bad: " + (score / 50.0));
		
		
		System.out.println("==========================================================");
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		
		String schemes [] = {"KY"/*, "PKY", "PW", "BC", "PP"*/};
		String ideals  [] = {/*"abc", "abcd", */"abcde"/*, "abcdef", "abcdefg", "abcdefgh"*/};
		for (String scheme: schemes){
			for (String ideal: ideals){
				for (double goodProb = 0.55; goodProb < 0.95; goodProb += 0.05){
					runScheme(scheme, ideal, goodProb);
				}				
			}
		}
		
		
		/*
		HashMap<String, Integer> ballotResultCount = new HashMap<>();
		HashSet<String> ballotResult = new HashSet<String>();
		System.out.println("Running Kemeny Young Scheme");
		for (int i = 0; i < 50; ++i){
			BSW [] kys = initIdealVoters(100, "abc", 33, 0.55, 0.9);
			
			
			for (BSW k: kys) {
				k.Start();
			}
			for (BSW k: kys) {
				try {
					k.runner.join();
					ballotResult.add(String.join("", k.maxRank));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (String s: ballotResult.toArray(new String[ballotResult.size()])){
				Integer count = ballotResultCount.get(s);
				ballotResultCount.put(s, count != null ? count+1 : 1);
			}
			ballotResult.clear();
		}
		int score  = 0;
		for (String s: ballotResultCount.keySet()){
			System.out.println("Kemeny Young Scheme [" + s + "]: " + ballotResultCount.get(s));
			score += (ballotResultCount.get(s) * distance("abc", s));
		}
		System.out.println("Total Score: " + score);
		System.out.println("Avg Distance Overall: " + (score / 50.0));
		System.out.println("Avg Distance Bad: " + (score / (50.0 - ballotResultCount.get("abc"))));
		ballotResultCount.clear();
		
		
		
		System.out.println("Running Pruned Kemeny Young Scheme");
		for (int i = 0; i < 50; ++i){
			BSW [] kys = initIdealVoters(100, "abc", 33, 0.55, 0.9);
			
			
			for (BSW k: kys) {
				k.bswScheme = new PrunedKemenyYoung();
				k.Start();
			}
			for (BSW k: kys) {
				try {
					k.runner.join();
					ballotResult.add(String.join("", k.maxRank));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (String s: ballotResult.toArray(new String[ballotResult.size()])){
				Integer count = ballotResultCount.get(s);
				ballotResultCount.put(s, count != null ? count+1 : 1);
			}
			ballotResult.clear();
		}
		score  = 0;
		for (String s: ballotResultCount.keySet()){
			System.out.println("Pruned Kemeny Young Scheme [" + s + "]: " + ballotResultCount.get(s));
			score += (ballotResultCount.get(s) * distance("abc", s));
		}
		System.out.println("Total Score: " + score);
		System.out.println("Avg Distance Overall: " + (score / 50.0));
		System.out.println("Avg Distance Bad: " + (score / (50.0 - ballotResultCount.get("abc"))));
		ballotResultCount.clear();
		
		
		
		
		System.out.println("Running Pairwise Comparison Scheme");
		for (int i = 0; i < 50; ++i){
			BSW [] kys = initIdealVoters(100, "abc", 33, 0.55, 0.9);
			
			
			for (BSW k: kys) {
				k.bswScheme = new PairwiseComparison();
				k.Start();
			}
			for (BSW k: kys) {
				try {
					k.runner.join();
					ballotResult.add(String.join("", k.maxRank));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (String s: ballotResult.toArray(new String[ballotResult.size()])){
				Integer count = ballotResultCount.get(s);
				ballotResultCount.put(s, count != null ? count+1 : 1);
			}
			ballotResult.clear();
		}
		score  = 0;
		for (String s: ballotResultCount.keySet()){
			System.out.println("Pairwise Comparison [" + s + "]: " + ballotResultCount.get(s));
			score += (ballotResultCount.get(s) * distance("abc", s));
		}
		System.out.println("Total Score: " + score);
		System.out.println("Avg Distance Overall: " + (score / 50.0));
		System.out.println("Avg Distance Bad: " + (score / (50.0 - ballotResultCount.get("abc"))));
		ballotResultCount.clear();
		
		
		
		
		System.out.println("Running Borda Count Scheme");
		for (int i = 0; i < 50; ++i){
			BSW [] kys = initIdealVoters(100, "abc", 33, 0.55, 0.9);
			
			
			for (BSW k: kys) {
				k.bswScheme = new BordaCount();
				k.Start();
			}
			for (BSW k: kys) {
				try {
					k.runner.join();
					ballotResult.add(String.join("", k.maxRank));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (String s: ballotResult.toArray(new String[ballotResult.size()])){
				Integer count = ballotResultCount.get(s);
				ballotResultCount.put(s, count != null ? count+1 : 1);
			}
			ballotResult.clear();
		}
		score  = 0;
		for (String s: ballotResultCount.keySet()){
			System.out.println("Borda Count [" + s + "]: " + ballotResultCount.get(s));
			score += (ballotResultCount.get(s) * distance("abc", s));
		}
		System.out.println("Total Score: " + score);
		System.out.println("Avg Distance Overall: " + (score / 50.0));
		System.out.println("Avg Distance Bad: " + (score / (50.0 - ballotResultCount.get("abc"))));
		ballotResultCount.clear();
		
		
		
		
		System.out.println("Running Place Plurality Scheme");
		for (int i = 0; i < 50; ++i){
			BSW [] kys = initIdealVoters(100, "abc", 33, 0.55, 0.9);
			
			
			for (BSW k: kys) {
				k.bswScheme = new PlacePlurality();
				k.Start();
			}
			for (BSW k: kys) {
				try {
					k.runner.join();
					ballotResult.add(String.join("", k.maxRank));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (String s: ballotResult.toArray(new String[ballotResult.size()])){
				Integer count = ballotResultCount.get(s);
				ballotResultCount.put(s, count != null ? count+1 : 1);
			}
			ballotResult.clear();
		}
		score  = 0;
		for (String s: ballotResultCount.keySet()){
			System.out.println("Place Plurality [" + s + "]: " + ballotResultCount.get(s));
			score += (ballotResultCount.get(s) * distance("abc", s));
		}
		System.out.println("Total Score: " + score);
		System.out.println("Avg Distance Overall: " + (score / 50.0));
		System.out.println("Avg Distance Bad: " + (score / (50.0 - ballotResultCount.get("abc"))));
		ballotResultCount.clear();
		
		
		
		System.out.println("Running Pruned Kemeny Young Scheme");
		for (BSW k: kys){
			k.bswScheme = new PrunedKemenyYoung();
			//k.cleanUp();
		}
		for (BSW k: kys) {
			k.Start();
		}
		for (BSW k: kys) {
			try {
				k.runner.join();
				resultBallot.add(String.join("", k.maxRank));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (String s: resultBallot.toArray(new String[resultBallot.size()]))
			System.out.println(s);
		resultBallot.clear();
		
		
		
		
		System.out.println("Running Pairwise Comparison Scheme");
		for (BSW k: kys){
			k.bswScheme = new PairwiseComparison();
			//k.cleanUp();
		}
		for (BSW k: kys) {
			k.Start();
		}
		for (BSW k: kys) {
			try {
				k.runner.join();
				resultBallot.add(String.join("", k.maxRank));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (String s: resultBallot.toArray(new String[resultBallot.size()]))
			System.out.println(s);
		resultBallot.clear();
		
		
		System.out.println("Running Borda Count Scheme");
		for (BSW k: kys){
			k.bswScheme = new BordaCount();
			//k.cleanUp();
		}
		for (BSW k: kys) {
			k.Start();
		}
		for (BSW k: kys) {
			try {
				k.runner.join();
				resultBallot.add(String.join("", k.maxRank));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (String s: resultBallot.toArray(new String[resultBallot.size()]))
			System.out.println(s);
		resultBallot.clear();
		
		
		
		System.out.println("Running Place Plurality Scheme");
		for (BSW k: kys){
			k.bswScheme = new PlacePlurality();
			//k.cleanUp();
		}
		for (BSW k: kys) {
			k.Start();
		}
		for (BSW k: kys) {
			try {
				k.runner.join();
				resultBallot.add(String.join("", k.maxRank));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (String s: resultBallot.toArray(new String[resultBallot.size()]))
			System.out.println(s);
		resultBallot.clear();
		
		*/
		
    }
	

	private static int distance(String ideal, String ranking) {
		if (ideal.equals(ranking))
			return 0;
		int score = 0;
		HashSet<String> pairs = new HashSet<String>();
		for (int i = 0; i < ideal.length()-1; ++i){
			for (int j = i+1; j < ideal.length(); ++j){
				pairs.add(""+ideal.charAt(i)+ideal.charAt(j));
			}
		}
		
		for (String pair: pairs.toArray(new String[pairs.size()])){
			String cleaned = ranking.replaceAll("[^" + pair + "]", "");
			if (!pair.equals(cleaned))
				score++;
		}
		return score;
	}


	public static BSW[] initBSWVoters (int nKYS, int faulty){
		BSW [] kys = new BSW[nKYS];
		
		String host = "127.0.0.1";
        String[] peers = new String[nKYS];
        int[] ports = new int[nKYS];
        String k = "abc";
        HashSet<String> permutations = (HashSet<String>) ElectionUtils.permutationFinder(k);
        for(int i = 0 ; i < nKYS; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        
        
        //String [] possible_choices = permutations.toArray(new String[permutations.size()]);
        for(int i = 0; i < nKYS; i++){
        	if (i == 0 || i == 1 || i == 2){
        		kys[i] = new BSW(i, peers, ports, "bac", faulty, new KemenyYoung());
        	}
        	else if (i == 6)
        		kys[i] = new BSW(i, peers, ports, "abc", faulty, new KemenyYoung());
        	else
        		kys[i] = new BSW(i, peers, ports, "cab", faulty, new KemenyYoung());
        	
        	//kys[i] = new OrigKYoung(i, peers, ports, possible_choices[Math.abs(r.nextInt()) % possible_choices.length], faulty);
        }
		
		
		return kys;
		
		
	}
	
	
	public static BSW[] initIdealVoters (int nBWS, String idealRanking, int faulty, double goodProb, double badProb){
		BSW [] kys = new BSW[nBWS];
		String host = "127.0.0.1";
        String[] peers = new String[nBWS];
        int[] ports = new int[nBWS];
        int f = faulty;
        for(int i = 0 ; i < nBWS; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        
        for (int i = 0; i < nBWS; ++i){
        	// Add a bad voter
        	if (f > 0){
        		--f;
        		String badVote = createVote(idealRanking, false, badProb);
        		kys[i] = new BSW(i, peers, ports, badVote, faulty, new KemenyYoung());
        		
        	}
        	// Add a good voter
        	else {
        		String goodVote = createVote(idealRanking, true,  goodProb);
        		kys[i] = new BSW(i, peers, ports, goodVote, faulty, new KemenyYoung());
        	}
        }
        
        
		return kys;
		
	}



	private static String createVote (String idealRanking, boolean goodVoter, double probability) {
		HashSet<String> pairwiseRanks = createPairwiseRanks(idealRanking);
		HashSet<Character> addedToVote = new HashSet<Character>();
		ArrayList<String> pairs = new ArrayList<String>(pairwiseRanks);
		Collections.shuffle(pairs);
		
		boolean [] pairRules = new boolean[pairs.size()];
		for (int i = 0; i < pairRules.length; ++i){
			
			// Set true if goodVoter, else set to false
			if (new Random().nextInt(100) < ((int) (probability*100))){
				pairRules[i] = goodVoter;
			}
			else
				pairRules[i] = !goodVoter;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pairs.size(); ++i){
			// New pair
			if (!addedToVote.contains(pairs.get(i).charAt(0)) && !addedToVote.contains(pairs.get(i).charAt(1))){
				if (pairRules[i]){
					sb.append(pairs.get(i));
				}
				else {
					// Reverse string and put in
					sb.append(new StringBuilder(pairs.get(i)).reverse().toString());
				}
				
				addedToVote.add(pairs.get(i).charAt(0));
				addedToVote.add(pairs.get(i).charAt(1));
			}
			
			// First is new, second already exists in StringBuilder
			else if (!addedToVote.contains(pairs.get(i).charAt(0)) && addedToVote.contains(pairs.get(i).charAt(1))) {
				int index = sb.indexOf(""+pairs.get(i).charAt(1));
				if (pairRules[i]){
					sb.insert(index, pairs.get(i).charAt(0));					
				}
				else {
					sb.insert(index+1, pairs.get(i).charAt(0));
				}
				addedToVote.add(pairs.get(i).charAt(0));
			}
			
			// First exists, second is new in StringBuilder
			else if (addedToVote.contains(pairs.get(i).charAt(0)) && !addedToVote.contains(pairs.get(i).charAt(1))) {
				int index = sb.indexOf(""+pairs.get(i).charAt(0));
				if (pairRules[i]){
					sb.insert(index+1, pairs.get(i).charAt(1));					
				}
				else {
					sb.insert(index, pairs.get(i).charAt(1));
				}
				addedToVote.add(pairs.get(i).charAt(1));
			}
			
			// Both exist in StringBuilder
			else if (addedToVote.contains(pairs.get(i).charAt(0)) && addedToVote.contains(pairs.get(i).charAt(1))) {
				
				// If they're already in order, move on, otherwise change order
				String cleaned = sb.toString().replaceAll("[^" + pairs.get(i) + "]", "");
				if (pairRules[i]){
					if (pairs.get(i).equals(cleaned))
						continue;
				}
				
				int indexFirst = sb.indexOf(""+pairs.get(i).charAt(0));
				int indexSecond = sb.indexOf(""+pairs.get(i).charAt(1));
				if (pairRules[i]){
					sb.delete(indexSecond, indexSecond+1);
					indexFirst = sb.indexOf(""+pairs.get(i).charAt(0));
					sb.insert(indexFirst+1, pairs.get(i).charAt(1));					
				}
				else {
					sb.delete(indexFirst, indexFirst+1);
					indexSecond = sb.indexOf(""+pairs.get(i).charAt(1));
					sb.insert(indexSecond+1, pairs.get(i).charAt(0));
				}
				addedToVote.add(pairs.get(i).charAt(1));
			}
			
		}
		
		
		
		/*for (String pair: pairwiseRanks.toArray(new String [pairwiseRanks.size()])){
			String cleaned = idealRanking.replaceAll("[^" + pair + "]", "");
			if (cleaned.equals(pair)){
				// Reverse the order of this pair for Vote
				if(new Random().nextInt((int)probability*100) <= ((int) probability*100)){
					if (!addedToVote.contains(pair.charAt(1))){
						sb.insert(0, pair.charAt(1));
						addedToVote.add(pair.charAt(1));
					}
					if (!addedToVote.contains(pair.charAt(0))){
						sb.append(pair.charAt(0));
						addedToVote.add(pair.charAt(0));
					}
				}
				// Add as is to Vote
				else {
					if (!addedToVote.contains(pair.charAt(0))){
						sb.append(pair.charAt(0));
						addedToVote.add(pair.charAt(0));
					}
					if (!addedToVote.contains(pair.charAt(1))){
						sb.append(pair.charAt(1));
						addedToVote.add(pair.charAt(1));
					}
				}
			} 
		}*/
		return sb.toString();
	}


	private static HashSet<String> createPairwiseRanks(String idealRanking) {
		HashSet<String> pairwiseRanks = new HashSet<String>();
		for (int i = 0; i < idealRanking.length() - 1; ++i){
			for (int j = i + 1; j < idealRanking.length(); ++j){
				pairwiseRanks.add(idealRanking.charAt(i) + "" + idealRanking.charAt(j));
			}
		}
		return pairwiseRanks;
	}
	
	
	
}
