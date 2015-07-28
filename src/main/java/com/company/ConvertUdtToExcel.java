package com.company;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

class Main {

    public static String filename, excelFilename;

    public static void main(String[] args) throws java.io.IOException {

        while(null == filename & null == excelFilename){
            parseArguments(args);
        }

        new ConvertUdtToExcel(filename, excelFilename);

        //obj.getTableTitle();

       // createExcelFile(excelFilename);



/*
        System.out.println("Nesting of file is:" + getStructNesting(filename));

        File myFile = new File(excelFilename);
        FileInputStream fis = new FileInputStream(myFile);

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);

        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);



        Map<String, Object[]> data = new HashMap<String, Object[]>();
        data.put("7", new Object[] {7d, "Sonya", "75K", "SALES", "Rupert"});
        data.put("8", new Object[] {8d, "Kris", "85K", "SALES", "Rupert"});
        data.put("9", new Object[] {9d, "Dave", "90K", "SALES", "Rupert"});

        // Set to Iterate and add rows into XLS file
        Set<String> newRows = data.keySet();

        // get the last row number to append new data
        int rownum = mySheet.getLastRowNum();
        System.out.println("Last row number is:" + rownum);

        for (String key : newRows) {

            // Creating a new Row in existing XLSX sheet
            Row row = mySheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                } else if (obj instanceof Boolean) {
                    cell.setCellValue((Boolean) obj);
                } else if (obj instanceof Date) {
                    cell.setCellValue((Date) obj);
                } else if (obj instanceof Double) {
                    cell.setCellValue((Double) obj);
                }
            }
        }


        FileOutputStream os = new FileOutputStream(myFile);
        myWorkBook.write(os);
        System.out.println("Writing on XLSX file Finished ...");*/
    }

    public static void parseArguments(String[] args){
        if (null != args & args.length == 2){
            File fileIn = new File(args[0]);
            File fileOut = new File(args[1]);
            if (!fileIn.isDirectory() & fileIn.exists()){
                filename = args[0];
            }
            if (!fileOut.isDirectory()){
                excelFilename = args[1];
            }
        }else System.out.println("Illegal argument lenght, please enter valid source filepath\nExample: C:\\udt.txt");
    }



}

public class ConvertUdtToExcel{

    public static final String ASSIGN_DEFAULT_VALUE = ":=";
    private static final String TYPE_SEPARATOR = " : ";
    private static final String TYPE_END_SEPARATOR = ";";
    private static final String COMMENT_SEPARATOR = "//";
    private static final String STRUCT_BEGIN = " STRUCT";
    private static final String STRUCT_END = "END_STRUCT";
    private static final String AUTHOR_SEPARATOR = "\'";
    private static final String TYPE_UDT_SEPARATOR = "\"";
    private static final String STRUCT_TYPE_END_SEPARATOR = " ";
    private static final String NEST_SHIFTER = "    ";

    public LinkedHashMap<String, String> tableTitle = new LinkedHashMap<String, String>();
    public LinkedHashMap<String, Double> dataTypes = new LinkedHashMap<String, Double>();

    public XSSFWorkbook myWorkBook;
    public XSSFSheet mySheet;

    public CellStyle structBegin, structEnd;


    public String filename , excelFilename;

    private double globalSum, structSum  = 0;


    private boolean isPreviousBool = false;


    public ConvertUdtToExcel(){
        createEmptyTableTitle();
    }

    public ConvertUdtToExcel(String filename, String excelFilename) {
        createEmptyTableTitle();
        fillDataTypesHashMap();
        this.filename = filename;
        this.excelFilename = excelFilename;
        getTableTitle(filename);
        createExcelFile(excelFilename);
        createCellStyles();
    }

    private void createEmptyTableTitle() {
        tableTitle.put("type", "");
        tableTitle.put("author", "");
        tableTitle.put("version", "");
        tableTitle.put("udt_name_cell", "Name of UDT: ");
        tableTitle.put("author_cell", "Author: ");
        tableTitle.put("version_cell", "Version: ");
        tableTitle.put("address_column", "Adress");
        tableTitle.put("name_column", "Name");
        tableTitle.put("type_column", "Type");
        tableTitle.put("comment_column", "Comment");
    }

    private void fillDataTypesHashMap(){
        dataTypes.put("BOOL", 0.1);
        dataTypes.put("INT", 2.0);
        dataTypes.put("DINT", 4.0);
        dataTypes.put("WORD", 2.0);
        dataTypes.put("REAL", 4.0);
        dataTypes.put("DWORD", 4.0);
        dataTypes.put("S5TIME", 2.0);
        dataTypes.put("TIME", 2.0);
        dataTypes.put("DATE", 4.0);
        dataTypes.put("TIME_OF_DAY", 4.0);



    }

    private void createCellStyles(){
        structBegin = myWorkBook.createCellStyle();
        structBegin.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        structEnd = myWorkBook.createCellStyle();
        structEnd.setFillBackgroundColor(IndexedColors.GREY_50_PERCENT.getIndex());

    }




    private void createExcelFile(String excelFilename) {
        createExcelEmptyFile(excelFilename);

    }

    private void createExcelEmptyFile(String excelFilename) {
        try {
            myWorkBook = new XSSFWorkbook();
            mySheet = myWorkBook.createSheet(tableTitle.get("type"));

            writeTableHeader(mySheet, filename);

            FileOutputStream fileOut = new FileOutputStream(excelFilename);
            myWorkBook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public  void getTableTitle(String filename) {
        int lineNumber = 0;
        try {
            File structFile = new File(filename);
            Scanner scanner = new Scanner(structFile);
            while (lineNumber < 100) {
                String line = scanner.nextLine();
                if (line.contains("TYPE")) tableTitle.put("type", line.split(TYPE_UDT_SEPARATOR)[1]);
                if (line.contains("AUTHOR")) tableTitle.put("author", line.split(AUTHOR_SEPARATOR)[1]);
                if (line.contains("VERSION")) {
                    tableTitle.put("version", line.split(TYPE_SEPARATOR)[1]);
                    break;
                }
                lineNumber++;

            }
            scanner.close();
        }catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
    }

    public  void writeTableHeader(XSSFSheet mySheet, String filename){
        ArrayList<String> listTitle = new ArrayList<String>(tableTitle.values());
        for(short rowIterator = 0; rowIterator < 4; rowIterator++){
            Row row = mySheet.createRow(rowIterator);
            Cell cell = row.createCell(0);
            if(rowIterator < 3){
                cell.setCellValue(listTitle.get(rowIterator+3) + listTitle.get(rowIterator));
                mySheet.addMergedRegion(new CellRangeAddress(
                        rowIterator, //first row (0-based)
                        rowIterator, //last row  (0-based)
                        0, //first column (0-based)
                        3  //last column  (0-based)
                ));
            }
            else{
                for (short cellIterator = 0; cellIterator <4; cellIterator++){
                    cell = row.createCell(cellIterator);
                    cell.setCellValue(listTitle.get(cellIterator+6));
                }
            }


        }


        fillTableWithData(mySheet);

        autoSizeAllColumnWidth(mySheet);



        }

    private void autoSizeAllColumnWidth(XSSFSheet mySheet) {
        Row row = mySheet.getRow(3);
        for(int colNum = 0; colNum<row.getLastCellNum();colNum++)
            mySheet.autoSizeColumn(colNum);
    }

    private void fillTableWithData(XSSFSheet mySheet) {
        File structFile = new File(filename);
        short rowIterator = 4;
        int lineNumber = 0;
        int numberOfStruct = 0;

        boolean jumpToData = false;
        try {
            Scanner scanner = new Scanner(structFile);
            String line = scanner.nextLine();
            while(scanner.hasNextLine() & lineNumber < 100) {
                if (line.contains("STRUCT")) {
                    jumpToData = true;
                    break;
                }
                lineNumber++;
                line = scanner.nextLine();
            }
            if (!jumpToData){
                System.out.println("Sorry, we can't find STRUCT begin");
            } else{
                while (scanner.hasNextLine()){
                    line = scanner.nextLine();
                    if(line.contains(TYPE_END_SEPARATOR) | line.contains(STRUCT_BEGIN)){

                        Row row = mySheet.createRow(rowIterator);
                        //System.out.println(numberOfStruct);
                        Cell cell = row.createCell(0);
                        cell.setCellValue(getCurrentAddress(line));

                        cell = row.createCell(1);

                        if (line.contains(STRUCT_BEGIN)) {
                            cell.setCellValue(getNestShifter(numberOfStruct) + line.split(TYPE_SEPARATOR)[0].replaceAll("\\s+", ""));
                            cell.setCellStyle(structBegin);
                            numberOfStruct++;
                        } else if (line.contains(STRUCT_END)){
                            numberOfStruct--;
                            cell = row.createCell(2);
                            cell.setCellValue(STRUCT_END);
                            cell.setCellStyle(structEnd);
                            rowIterator++;
                            continue;
                        }else{
                            cell.setCellValue(getNestShifter(numberOfStruct) + line.split(TYPE_SEPARATOR)[0].replaceAll("\\s+", ""));
                        }
                        cell = row.createCell(2);
                        cell.setCellValue(line.split(TYPE_SEPARATOR)[1].split(line.contains(STRUCT_BEGIN) ? COMMENT_SEPARATOR :TYPE_END_SEPARATOR)[0].replaceAll("\\s+", ""));
                        if (line.contains(COMMENT_SEPARATOR)) {
                            cell = row.createCell(3);
                            cell.setCellValue(line.split(COMMENT_SEPARATOR)[1]);
                        }
                        rowIterator++;
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentAddress(String line) {
        double currentDataSize, addressShift;
        double currentGlobalSum = globalSum;
        //double currentStructSum = structSum;
        if (line.contains(STRUCT_BEGIN)) {
            structSum = 0.0;
            return "+" + Double.toString(round(globalSum, 2));
        } else if (line.contains(STRUCT_END)) {
            return "=" + Double.toString(round(structSum, 2));
        } else {
            String dataType = line.split(TYPE_SEPARATOR)[1].split(TYPE_END_SEPARATOR)[0].replaceAll("\\s+", "");
            if (line.contains(ASSIGN_DEFAULT_VALUE)) dataType = dataType.split(ASSIGN_DEFAULT_VALUE)[0].replaceAll("\\s+", "");
            currentDataSize = dataTypes.containsKey(dataType) ? dataTypes.get(dataType) : 0;
            if(line.contains("BOOL")){
                isPreviousBool = true;
                addressShift = (checkBoolSumOverflow(globalSum)) ? 0.3 : 0.1;
            }
            else if (isPreviousBool & checkBoolSumOverflow(globalSum)) {
                addressShift = 2.0 + currentDataSize;
                isPreviousBool = false;
            } else addressShift = currentDataSize;

            structSum +=  addressShift;
            globalSum += addressShift;
            return "+" + Double.toString(round(currentGlobalSum, 2));
        }
    }

    private String getNestShifter(int shiftNumbers) {
        if (shiftNumbers >=0) return new String(new char[shiftNumbers]).replace("\0", NEST_SHIFTER);
        return "";
    }

    public boolean checkBoolSumOverflow(double num){
        long iPart = (long) num;;
        double fPart = num - iPart;
        return (fPart >= 0.7);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }



}
