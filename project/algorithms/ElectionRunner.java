package algorithms;

import java.util.HashSet;
import java.util.Random;

import util.ElectionUtils;

public class ElectionRunner {

	public static void main(String[] args) {
		
		
		BSW [] kys = initOrigKYoung(7, 2);
		for (BSW k: kys) {
			//System.out.println("Voter " + k.pid + ": " + k.ballot[k.pid]);
			k.Start();
		}
		
		for (BSW k: kys) {
			try {
				k.runner.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		// TODO: Implement pruned scheme
		/*kys = initPrunedKYoung(7, 2);
		for (BSW k: kys) {
			//System.out.println("Voter " + k.pid + ": " + k.ballot[k.pid]);
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
	
	private static BSW[] initPrunedKYoung(int nKYS, int faulty) {
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
        
        
        //Random r = new Random();
        //String [] possible_choices = permutations.toArray(new String[permutations.size()]);
        for(int i = 0; i < nKYS; i++){
        	if (i == 0 || i == 1 || i == 2){
        		kys[i] = new PrunedKYoung(i, peers, ports, "bac", faulty);
        	}
        	else if (i == 6)
        		kys[i] = new PrunedKYoung(i, peers, ports, "abc", faulty);
        	else
        		kys[i] = new PrunedKYoung(i, peers, ports, "cab", faulty);
        	//kys[i] = new PrunedKYoung(i, peers, ports, possible_choices[Math.abs(r.nextInt()) % possible_choices.length], faulty);
        }
		
		
		return kys;
	}

	public static BSW[] initOrigKYoung(int nKYS, int faulty){
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
        
        
        Random r = new Random();
        String [] possible_choices = permutations.toArray(new String[permutations.size()]);
        for(int i = 0; i < nKYS; i++){
        	if (i == 0 || i == 1 || i == 2){
        		kys[i] = new BSW(i, peers, ports, "bac", faulty, new PrunedKemenyYoung());
        	}
        	else if (i == 6)
        		kys[i] = new BSW(i, peers, ports, "abc", faulty, new PrunedKemenyYoung());
        	else
        		kys[i] = new BSW(i, peers, ports, "cab", faulty, new PrunedKemenyYoung());
        	
        	//kys[i] = new OrigKYoung(i, peers, ports, possible_choices[Math.abs(r.nextInt()) % possible_choices.length], faulty);
        }
		
		
		return kys;
		
		
	}
	
    public static BscAlpha[] initBSCAlgoOne(int nBSC){
    	
    	
        String host = "127.0.0.1";
        String[] peers = new String[nBSC];
        int[] ports = new int[nBSC];
        
        BscAlpha[] bsc = new BscAlpha[nBSC];
        for(int i = 0 ; i < nBSC; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        
        for(int i = 0; i < nBSC; i++){
        	bsc[i] = new BscAlpha(i, peers, ports, 3, 1);
        }
        return bsc;
    }
	
	
}
