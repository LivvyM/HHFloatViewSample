package demo.kingnet.com.plugins;

/**
 * Created by liulb1 on 2019/1/10.
 */

public class NumberUtils {

    public static int strToNumber(String str) {
        try{
            return Integer.parseInt(str);
        }catch (Exception e){
            return 0;
        }
    }

}
