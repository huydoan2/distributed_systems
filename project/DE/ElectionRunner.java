package DE;

public class ElectionRunner {

	public static void main(String[] args) {
		
		BscAlpha nodes [] = initBSCAlgoOne(7);
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
		}
      
    }
	
	
	
    static private BscAlpha[] initBSCAlgoOne(int nBSC){
    	
    	
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
