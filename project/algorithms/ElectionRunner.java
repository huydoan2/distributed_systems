package algorithms;

import java.util.HashSet;
import java.util.Random;

public class ElectionRunner {

	public static void main(String[] args) {
		
		
		KYoungScheme [] kys = initOrigKYoung(15);
		for (KYoungScheme k: kys) {
			//System.out.println("Voter " + k.pid + ": " + k.ballot[k.pid]);
			k.Start();
		}
		
		
		/*BscAlpha nodes [] = initBSCAlgoOne(7);
		Thread threadHandlers [] = new Thread[7];
		for (int i = 0; i < nodes.length; ++i){
			
			threadHandlers[i] = new Thread(nodes[i]);
			threadHandlers[i].start();
			
		}
		
		
		for (int i = 0; i < threadHandlers.length; i++){
			try {
				threadHandlers[i].join();
				System.out.println("Votes in " + i + ":");
				System.out.print("[");
				for (String j: nodes[i].T){
					System.out.print(String.valueOf(j) + " ");
				}
				System.out.println();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
      
    }
	
	public static KYoungScheme[] initOrigKYoung(int nKYS){
		KYoungScheme [] kys = new KYoungScheme[nKYS];
		
		String host = "127.0.0.1";
        String[] peers = new String[nKYS];
        int[] ports = new int[nKYS];
        String k = "abcdef";
        HashSet<String> permutations = (HashSet<String>) KYoungScheme.permutationFinder(k);
        for(int i = 0 ; i < nKYS; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        
        
        Random r = new Random();
        String [] possible_choices = permutations.toArray(new String[permutations.size()]);
        for(int i = 0; i < nKYS; i++){
        	kys[i] = new OrigKYoung(i, peers, ports, possible_choices[Math.abs(r.nextInt()) % possible_choices.length], 1);
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
