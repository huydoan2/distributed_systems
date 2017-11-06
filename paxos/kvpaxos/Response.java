package kvpaxos;

import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=22L;
    // your data here
    private String key;
    private Integer value;
    boolean successful;
    
    Response(String key, Integer value){
    	this.key = key;
    	this.value = value;
    }

    Response(String key, Integer value, boolean success){
    	this.key = key;
    	this.value = value;
    	this.successful = success;
    }
    
    String getKey(){
    	return key;
    }
    
    Integer getValue(){
    	return value;
    }
    

    boolean isSuccessful(){
    	return successful;
    }

    // Your constructor and methods here
}
