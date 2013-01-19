/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evotingcommon;

import java.util.Scanner;

/**
 *
 * @author Maciek
 */
public class ServerKiller extends Thread{

    @Override
    public void run() {
        System.out.println("Aby zakonczyc dzialanie serwera, wprowadz \"zamknij\".");
        Scanner sc = new Scanner(System.in);
        String input = null;
        while(true){
            try{
                if(sc.hasNext()){
                    input = sc.next();
                    if(input.equals("zamknij")){
                        System.out.println("Zamknięcie systemu nastąpi za najwyżej 10 sekund");
                        return;
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
