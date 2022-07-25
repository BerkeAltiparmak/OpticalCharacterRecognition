package org.example;

import java.util.ArrayList;
import java.util.Scanner;

public class Tester {
    public static void main(String[] args) {
        while(true) {
            //long start = System.currentTimeMillis();
            System.out.print("N gir: ");
            Scanner s = new Scanner(System.in);
            int N = s.nextInt();
            /*
            for (int i = 1; i <= N; i++) {
                int result = calculate(i);
                System.out.println(i + ": " + result);
            }
            */
            int result = calculate(N);
            System.out.println(N + ": " + result);
            //System.out.println("It took: " + (System.currentTimeMillis() - start) + " milliseconds.");
        }
    }
    public static int calculate(int N) {
        ArrayList<Integer> totalNums = new ArrayList<>();
        for (int i = 1; i < Math.sqrt(N); i ++) {
            for (int j = i + 1; j < Math.sqrt(N); j ++) {
                int total = i*i + j*j;
                if (total <= N) {
                    if (!totalNums.contains(total)) {
                        System.out.println("Added: " + total);
                        totalNums.add(total);
                    } else {
                        System.out.println("Removed: " + total);
                        totalNums.remove(Integer.valueOf(total));
                    }
                }
                else {
                    break;
                }
            }
        }
        return totalNums.size();
    }
}
