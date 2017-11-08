package kvpaxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=11L;
    // Your data here
    private String op, key;
    private Integer value;
    
    // Your constructor and methods here
    Request(String op, String key){
    	this.op = op;
    	this.key = key;
    	value = null;
    }
    Request(String op, String key, Integer value){
    	this.op = op;
    	this.key = key;
    	this.value = value;
    }
    
    String getOp(){
    	return op;
    }
    
    String getKey(){
    	return key;
    }
    Integer getValue(){
    	return value;
    }

}
