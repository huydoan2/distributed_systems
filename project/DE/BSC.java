/**
 * Created by huydo on 11/13/2017.
 */

package DE;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class BSC implements  Runnable{

    int[] T;        //container to store all the votes
    int[] vote;     // ranking of k candidates
    String[] peers; // hostname
    int[] ports;    // host port
    int pid;        // me

    BSC(int pid, String[] peers, int[] ports){
        this.pid = pid;
        this.peers = peers;
        this.ports = ports;
    }

    @Override
    public void run(){
        // setup T and vote first


        ServerSocket listener = new ServerSocket(ports[pid]);

        T[pid] = vote[0];
        for(int j = 0; j < T.length(); ++j){
            if(j == pid)
                continue;

            Socket s = new Socket(peers[j], ports[j]);

            T[j] = s.getInputStream().


            try{
                Socket socket = listener.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
