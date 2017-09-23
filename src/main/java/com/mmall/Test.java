package com.mmall;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * @auther earlman
 * @create 9/21/17
 */
public class Test {
    public static void main(String arr[]) {
        Super c = new Sub();
        c.fun();
    }
}

class Sub extends Super {
    @Override
    public void fun() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(null, null);
        hashMap.put("oo", "ii");
        System.out.println(hashMap.size());
        HashSet<String> set = new HashSet<>();
        set.add(null);
        set.add(null);
        System.out.println(set.size());

        TreeSet<String> tree = new TreeSet<>();
        tree.add(null);
        new StringBuilder(0);
        System.out.println("I am Sub's method");
    }
}

class Super {
    public void fun() {
        System.out.println("I am Super's method");
    }
}
