
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ElectionUtils {
    
    public static <String, Integer extends Comparable<? super Integer>> Map<String, Integer> 
        sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
            	if (o1.getValue() != o2.getValue())
            		return (( o1.getValue()).compareTo( o2.getValue() ));
            	
                return ( (java.lang.String) o1.getKey()).compareTo(  (java.lang.String) o2.getKey() );
            }
        });

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    
    
    public static String arrayToString(String [] arr){
    	
    	StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
           strBuilder.append(arr[i]);
        }
        
        return strBuilder.toString();	
    	
    	
    }
    
    // Creates a set of all permutations of a string
    public  static Set<String> permutationFinder(String str) {
        Set<String> perm = new HashSet<String>();
        //Handling error scenarios
        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            perm.add("");
            return perm;
        }
        char initial = str.charAt(0); // first character
        String rem = str.substring(1); // Full string without first character
        Set<String> words = permutationFinder(rem);
        for (String strNew : words) {
            for (int i = 0;i<=strNew.length();i++){
                perm.add(charInsert(strNew, initial, i));
            }
        }
        return perm;
    }
    

    public static String charInsert(String str, char c, int j) {
        String begin = str.substring(0, j);
        String end = str.substring(j);
        return begin + c + end;
    }
}