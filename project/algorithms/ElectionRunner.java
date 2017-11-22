package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import util.ElectionUtils;

public class ElectionRunner {

	public static void main(String[] args) {
		
		BSW [] kys = initIdealVoters(100, "cba", 33, 0.55, 0.9);
		
		/*for (BSW k: kys){
			k.Start();
			//System.out.println("Process " + k.pid + ": " + k.ballot[k.pid]);
		}*/
		//System.out.println(kys);
		//BSW [] kys = initBSWVoters(7, 2);
		System.out.println("Running Kemeny Young Scheme");
		for (BSW k: kys) {
			k.Start();
		}
		
		
		HashSet<String> resultBallot = new HashSet<String>();
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
		/*
		
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
     
		
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
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
			if (new Random().nextInt(100) <= ((int) (probability*100))){
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
				if (pairs.get(i).equals(cleaned))
					continue;
				
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
