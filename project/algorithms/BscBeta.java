package algorithms;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import DE.BSCRMI;
import DE.Request;
import DE.Response;

public class BscBeta implements Runnable, BSCRMI{

    int[] T;        //container to store all the votes
    int[] vote;     // ranking of k candidates
    String[] peers; // hostname
    int[] ports;    // host port
    int pid;        // me
    int finalValue;
    int f;
    int numCandidates;
    Registry registry;
    BSCRMI stub;
    
    BscBeta(int pid, String[] peers, int[] ports, int faults, int candidates){
        this.pid = pid;
        this.peers = peers;
        this.ports = ports;
        this.numCandidates = candidates;
        this.f = faults;
        
        T = new int [peers.length];
        vote = new int [candidates];
        
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.pid]);
            registry = LocateRegistry.createRegistry(this.ports[this.pid]);
            stub = (BSCRMI) UnicastRemoteObject.exportObject(this, this.ports[this.pid]);
            registry.rebind("BSC", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        
    }
    
    public String encrypt(String strClearText, String strKey) throws Exception{
    	String strData="";
    	
    	try {
    		SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"Blowfish");
    		Cipher cipher=Cipher.getInstance("Blowfish");
    		cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
    		byte[] encrypted=cipher.doFinal(strClearText.getBytes());
    		strData=new String(encrypted);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new Exception(e);
    	}
    	return strData;
    }
    
    
    public String decrypt(String strEncrypted, String strKey) throws Exception{
    	String strData="";
    	
    	try {
    		SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"Blowfish");
    		Cipher cipher=Cipher.getInstance("Blowfish");
    		cipher.init(Cipher.DECRYPT_MODE, skeyspec);
    		byte[] decrypted=cipher.doFinal(strEncrypted.getBytes());
    		strData=new String(decrypted);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new Exception(e);
    	}
    	return strData;
    }
    
    
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        BSCRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub= (BSCRMI) registry.lookup("BSC");
            if(rmi.equals("Vote"))
                callReply = stub.Vote(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }
    


    @Override
    public void run(){
    	
        // setup T and vote first
        T[pid] = vote[0];
        
        // Step 1: Send first choice with all
        for(int j = 0; j < peers.length; ++j){
        	
            if(j == pid)
                continue;

            /*Request request = new Request(this.pid, T[pid]);
            Call("Vote", request, j);*/
            
        }
        
        // Step 2: Agree on T vector: ballot of all votes
        for (int j = 0; j < peers.length; ++j){
        	// Run Standard Byzantine Agreement on T[j]
        }
        
        // Step 3: Eliminate unqualified choices
        HashSet<Integer> discard = new HashSet<>();
        for (int j = 0; j < numCandidates; ++j){
        	if (count(T, vote[j]) >= (Math.floor((peers.length - f)/2) + 1)){
        		discard.add(vote[j]);
        	}
        }
        
        
        // Step 4: Run agreement on remaining choices not in discard
        finalValue = leastValue(T);
        
        
    }
    
    int count(int [] T, int x){
    	int count = 0;
    	for (int a: T)
    		if (a == x)
    			++count;
    	return count;
    }
    
    
    int leastValue(int [] T){
    	HashMap<Integer, Integer> valueMap = new HashMap<>();
    	int maxCount = 0, maxValue = T[0];
    	for (int i = 0; i < T.length; ++i) {
    		if (valueMap.containsKey(T[i])){
    			valueMap.put(T[i], valueMap.get(T[i]) + 1);
    		}
    		else
    			valueMap.put(T[i], 1);
    		
    		if (valueMap.get(T[i]) > maxCount){
    			maxCount = valueMap.get(T[i]);
    			maxValue = T[i];
    		}
    		else if (valueMap.get(T[i]) == maxCount && T[i] < maxValue){
    			maxValue = T[i];
    		}
    	}
    	
    	return maxValue;
    }

	@Override
	public Response Vote(Request req) throws RemoteException {
		 /*Response resp = new Response(pid, T[pid]);
	        return resp;*/
		return null;
	}

	@Override
	public Response Done(Request req) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response Key(Request req) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response Choice(Request req) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response Ballot(Request req) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
}
