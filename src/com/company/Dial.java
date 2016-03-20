package com.company;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;


/**
 * Created by left on 16/3/18.
 */
public class Dial {

    private String getPIN(String userName) {

        int i, j;//循环变量
        long timedivbyfive;//时间除以五
        long timenow;//当前时间，从time()获得
        byte[] PIN = new byte[29];

        byte[] RADIUS = "singlenet01".getBytes();//凑位字符
        byte[] timechar = new byte[4];//时间 div 5
        byte[] beforeMD5 = new byte[26];//时间 div 5+用户名+凑位
        byte[] afterMD5 = new byte[16];//MD5输出
        byte[] MD501 = new byte[3];
        byte[] timeHash = new byte[4]; //时间div5经过第一次转换后的值
        byte[] temp = new byte[32]; //第一次转换时所用的临时数组
        byte[] PIN27 = new byte[6]; //PIN的2到7位，由系统时间转换

        timenow = (new Date().getTime()) / 1000;//得到1970年的时间戳获取秒数
        timedivbyfive = timenow / 5;

        for (i = 0; i < 4; i++) {
            timechar[i] = (byte) (timedivbyfive >> (8 * (3 - i)) & 0xFF);
        }
        for (i = 0; i < 4; i++) {
            beforeMD5[i] = timechar[i];
        }
        for (i = 4; i < 16 && userName.charAt(i - 4) != '@'; i++) {
            beforeMD5[i] = userName.getBytes()[i - 4];
        }

        for (j = 0; j < RADIUS.length; j++) {
            beforeMD5[i++] = RADIUS[j];
        }

        String t = MD5.GetMD5Code(new String(beforeMD5));
        for (i = 0; i < 16; i++)
            afterMD5[i] = t.getBytes()[i];

        MD501[0] = t.getBytes()[0];
        MD501[1] = t.getBytes()[1];


        for (i = 0; i < 32; i++) {
            temp[i] = (byte) (timechar[(31 - i) / 8] & 1);
            timechar[(31 - i) / 8] = (byte) (timechar[(31 - i) / 8] >> 1);
        }

        for (i = 0; i < 4; i++) {
            timeHash[i] = (byte) (temp[i] * 128 + temp[4 + i] * 64 + temp[8 + i]
                    * 32 + temp[12 + i] * 16 + temp[16 + i] * 8 + temp[20 + i]
                    * 4 + temp[24 + i] * 2 + temp[28 + i]);
        }

        temp[1] = (byte) ((timeHash[0] & 3) << 4);
        temp[0] = (byte) ((timeHash[0] >> 2) & 0x3F);
        temp[2] = (byte) ((timeHash[1] & 0xF) << 2);
        temp[1] = (byte) ((timeHash[1] >> 4 & 0xF) + temp[1]);
        temp[3] = (byte) (timeHash[2] & 0x3F);
        temp[2] = (byte) (((timeHash[2] >> 6) & 0x3) + temp[2]);
        temp[5] = (byte) ((timeHash[3] & 3) << 4);
        temp[4] = (byte) ((timeHash[3] >> 2) & 0x3F);

        for (i = 0; i < 6; i++) {
            PIN27[i] = (byte) (temp[i] + 0x020);
            if (PIN27[i] >= 0x40) {
                PIN27[i]++;
            }
        }

        PIN[0] = '\r';
        PIN[1] = '\n';

        for (i = 0; i < 6; i++) {
            PIN[i + 2] = PIN27[i];
        }

        PIN[8] = MD501[0];
        PIN[9] = MD501[1];

        for (i = 0; i < userName.length(); i++) {
            PIN[10 + i] = userName.getBytes()[i];
        }

        String result = "";
        try {
            result = new String(PIN, "ascii");//第二个参数指定编码方式;
        } catch (UnsupportedEncodingException e) {

        }
        return result;
    }

    public boolean dial(String userName, String password) {

        String TAG = "DialTag";

        String res = getPIN(userName);

     //   Log.e(TAG, res);

        String real = UrlEncode(res);
   //     Log.e(TAG, real);

        String address = "http://192.168.1.1/userRpm/PPPoECfgRpm.htm?wan=0&wantype=2&acc=" + real
                + "&psw=" + password + "&confirm=" + password + "&specialDial=100&SecType=1&sta_ip" +
                "=0.0.0.0&sta_mask=0.0.0.0&linktype=4&waittime2=0&Connect=%C1%AC+%BD%D3";

   //     Log.e(TAG, address);

        try {
            //封装访问服务器的地址
            URL url = new URL(address);
            try {
                //打开对服务器的连接
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //设置请求头
                conn.setRequestProperty("Referer", "http://192.168.1.1/userRpm/PPPoECfgRpm.htm");
                conn.setRequestProperty("Cookie", "Authorization=Basic%20YWRtaW46MTIzNDU2; ChgPwdSubTag=");
                conn.setRequestProperty("Connection", "keep-alive");

                //连接服务器
                conn.connect();

                return conn.getResponseCode() == 200;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //将data字节型数据转换为0~255 (0xFF 即BYTE)。
    public int getUnsignedByte(byte data) {
        return data & 0x0FF;
    }

    public int ToHex(int x) {
        return x > 9 ? x + 55 : x + 48;
    }

    public boolean isalnum(int data) {
        if ((data >= 48 && data <= 57) || (data >= 65 && data <= 90) || (data >= 97 && data <= 122))
            return true;
        else
            return false;
    }


    String UrlEncode(String str) {

        Vector<Byte> strTemp = new Vector<>();
        byte[] strbyte = str.getBytes();

        for (int i = 0; i < str.length(); i++) {
            if (isalnum(getUnsignedByte(strbyte[i])) || (strbyte[i] == '-') || (strbyte[i] == '_')
                    || (strbyte[i] == '.') || (strbyte[i] == '~'))
                strTemp.add(strbyte[i]);
            else if (strbyte[i] == ' ')
                strTemp.add(("+".getBytes()[0]));
            else {
                strTemp.add(("%".getBytes()[0]));
                strTemp.add((byte) ToHex(getUnsignedByte(strbyte[i]) >> 4));
                strTemp.add((byte) ToHex(getUnsignedByte(strbyte[i]) % 16));
            }
        }

        String result = "";
        try {
            Byte[] befbyte = new Byte[strTemp.size()];
            strTemp.toArray(befbyte);
            byte[] afbyte = new byte[befbyte.length];
            for (int i = 0; i < befbyte.length; i++) {
                afbyte[i] = befbyte[i].byteValue();
            }
            result = new String(afbyte, "ascii");//第二个参数指定编码方式;
        } catch (UnsupportedEncodingException e) {

        }
        return result;
    }


}


