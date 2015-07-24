package com.company;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by engineer on 22.07.2015.
 * ExcelTest com.company contains ..
 */
public class MainTest {

    private static String FILE_PATH = new File("").getAbsolutePath()+File.separator+"src"+File.separator+
            "test"+File.separator;

    public static final HashMap<String, String []> FILE_NAMES;
    static{
        FILE_NAMES = new HashMap<String, String[]>();
        FILE_NAMES.put("nest.txt", new String[]{"4", "nest", "", "1.1.1"});
        FILE_NAMES.put("u_Motor.txt", new String[]{"3", "u_Motor", "P@", "0.1"});
        FILE_NAMES.put("uAI_IN.txt", new String[]{"1", "uAI_IN", "", "0.1"});
        FILE_NAMES.put("uAI_OUT.txt", new String[]{"1", "uAI_OUT", "", "0.1"});
        FILE_NAMES.put("u_Gate.txt", new String[]{"3", "u_Gate", "P@", "0.1"});
        FILE_NAMES.put("uCDA.txt", new String[]{"2", "uCDA", "", "0.1"});
    }

    public static final HashMap<String, Integer> FILE_TITLE_ORDER;
    static{
        FILE_TITLE_ORDER = new HashMap<String, Integer>();
        FILE_TITLE_ORDER.put("type", 1);
        FILE_TITLE_ORDER.put("author", 2);
        FILE_TITLE_ORDER.put("version", 3);
    }


    @Test
    public void testGetStructNesting() throws Exception {
        ConvertUdtToExcel obj = new ConvertUdtToExcel();
        for(String key : FILE_NAMES.keySet() )
        assertEquals("File:"+key, obj.getStructNesting(FILE_PATH + key), Integer.parseInt(FILE_NAMES.get(key)[0]));
    }

    @Test
    public void testGetTableTitle() throws Exception {
        for (String key : FILE_NAMES.keySet()) {
            ConvertUdtToExcel test = new ConvertUdtToExcel();
            test.getTableTitle(FILE_PATH + key);
            for (String keyIterator : test.tableTitle.keySet())
                assertEquals("File:" + key + " parameter: " + keyIterator, test.tableTitle.get(keyIterator), FILE_NAMES.get(key)[FILE_TITLE_ORDER.get(keyIterator)]);
        }
    }
}