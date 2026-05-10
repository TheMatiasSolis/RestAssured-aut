package automation.generic;

import java.util.HashMap;
import java.util.Map;

public class DataHeader {

    private static final Map<String, String> myHashMap = new HashMap<>();

    public void setParametros(Map<String, String> walletData) {
        myHashMap.clear();

        if (walletData != null) {
            myHashMap.putAll(walletData);
        }
    }


    public static Map<String, String> getParametros()
    {
        return myHashMap;
    }

}
