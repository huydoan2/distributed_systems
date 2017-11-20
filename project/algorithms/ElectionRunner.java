package algorithms;

import java.util.HashSet;
import java.util.Random;

public class ElectionRunner {

	public static void main(String[] args) {
		
		
		KYoungScheme [] kys = initOrigKYoung(7, 2);
		for (KYoungScheme k: kys) {
			//System.out.println("Voter " + k.pid + ": " + k.ballot[k.pid]);
			k.Start();
		}
		
		// TODO: Implement pruned scheme
		//kys = initPrunedKYoung(7, 2);
		for (KYoungScheme k: kys) {
			//System.out.println("Voter " + k.pid + ": " + k.ballot[k.pid]);
			//k.Start();
		}

      
    }
	
	private static KYoungScheme[] initPrunedKYoung(int nKYS, int faulty) {
		KYoungScheme [] kys = new KYoungScheme[nKYS];
		
		String host = "127.0.0.1";
        String[] peers = new String[nKYS];
        int[] ports = new int[nKYS];
        String k = "abc";
        HashSet<String> permutations = (HashSet<String>) KYoungScheme.permutationFinder(k);
        for(int i = 0 ; i < nKYS; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        
        
        Random r = new Random();
        String [] possible_choices = permutations.toArray(new String[permutations.size()]);
        for(int i = 0; i < nKYS; i++){
        	/*if (i == 0 || i == 1 || i == 2){
        		kys[i] = new PrunedKYoung(i, peers, ports, "bac", faulty);
        	}
        	else if (i == 4)
        		kys[i] = new PrunedKYoung(i, peers, ports, "cba", faulty);
        	else
        		kys[i] = new PrunedKYoung(i, peers, ports, "cab", faulty);*/
        	kys[i] = new PrunedKYoung(i, peers, ports, possible_choices[Math.abs(r.nextInt()) % possible_choices.length], faulty);
        }
		
		
		return kys;
	}

	public static KYoungScheme[] initOrigKYoung(int nKYS, int faulty){
		KYoungScheme [] kys = new KYoungScheme[nKYS];
		
		String host = "127.0.0.1";
        String[] peers = new String[nKYS];
        int[] ports = new int[nKYS];
        String k = "abc";
        HashSet<String> permutations = (HashSet<String>) KYoungScheme.permutationFinder(k);
        for(int i = 0 ; i < nKYS; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        
        
        Random r = new Random();
        String [] possible_choices = permutations.toArray(new String[permutations.size()]);
        for(int i = 0; i < nKYS; i++){
/*        	if (i == 0 || i == 1 || i == 2){
        		kys[i] = new OrigKYoung(i, peers, ports, "bac", faulty);
        	}
        	else if (i == 4)
        		kys[i] = new OrigKYoung(i, peers, ports, "cba", faulty);
        	else
        		kys[i] = new OrigKYoung(i, peers, ports, "cab", faulty);*/
        	
        	kys[i] = new OrigKYoung(i, peers, ports, possible_choices[Math.abs(r.nextInt()) % possible_choices.length], faulty);
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
