package com.company;

public class Main {

    public static void main(String[] args) {
	// write your code hereDial dial = new Dial();
        //调用拨号方法
        Dial dial = new Dial();
        boolean result = dial.dial("18969940656@GDPF.XY", "716598");
    }
    public void test() {

        MD5 getMD5 = new MD5();

        String TAG = "DialTag";


        byte a = -12;

        int b = a & 0x0FF;

        String result = getMD5.GetMD5Code("1538114646511111111111111111111".substring(0, 26));
    }
}
