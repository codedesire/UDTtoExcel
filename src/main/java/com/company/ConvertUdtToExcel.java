package com.company;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

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

    public XSSFCellStyle structBegin, structEnd;


    public String filename , excelFilename;

    private double globalSum  = 0;
    private ArrayList<Double> structSum = new  ArrayList<Double>();

    private int numberOfStruct = 0;

    private ArrayList<Short>  structBeginRows = new ArrayList<Short>();
    private ArrayList<Short>  structEndRows = new ArrayList<Short>();


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
        dataTypes.put("TIME", 4.0);
        dataTypes.put("DATE", 2.0);
        dataTypes.put("TIME_OF_DAY", 4.0);
        structSum.add(0.0);
    }

    private void createCellStyles(){
        structBegin = myWorkBook.createCellStyle();
        structBegin.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        structBegin.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        structEnd = myWorkBook.createCellStyle();
        structEnd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        structEnd.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
    }

    private void createExcelFile(String excelFilename) {
        createExcelEmptyFile(excelFilename);
    }

    private void createExcelEmptyFile(String excelFilename) {
        try {
            myWorkBook = new XSSFWorkbook();
            mySheet = myWorkBook.createSheet(tableTitle.get("type"));

            createCellStyles();

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
        fillRowsForeground(mySheet);
        lastRowSum(mySheet);
        System.out.println(structBeginRows);
        System.out.println(structEndRows);
        }

    private void lastRowSum(XSSFSheet mySheet) {
        Row lastRow = mySheet.getRow(mySheet.getLastRowNum());
        lastRow.getCell(0).setCellValue("=" + Double.toString(round(globalSum, 2)));
    }

    private void fillRowsForeground(XSSFSheet mySheet) {
        for(short rowNumber : structBeginRows){
            fillRowForeground(mySheet.getRow(rowNumber), structBegin);
        }
        for(short rowNumber : structEndRows){
            fillRowForeground(mySheet.getRow(rowNumber), structEnd);
        }
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

                        Cell cell = row.createCell(0);
                        cell.setCellValue(getCurrentAddress(line, rowIterator));
                        cell = row.createCell(1);

                        if (line.contains(STRUCT_BEGIN)) {
                            cell.setCellValue(getNestShifter(numberOfStruct) + line.split(TYPE_SEPARATOR)[0].replaceAll("\\s+", ""));
                            structBeginRows.add(rowIterator);
                            numberOfStruct++;
                        } else if (line.contains(STRUCT_END)){
                            numberOfStruct--;
                            cell = row.createCell(2);
                            cell.setCellValue(STRUCT_END);
                            structEndRows.add(rowIterator);
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

    private void fillRowForeground(Row row, XSSFCellStyle userCellStyle) {
        for (short iterator = 0; iterator <= 3; iterator++){
            Cell tempCell;
            if(null == row.getCell(iterator, Row.RETURN_NULL_AND_BLANK)) row.createCell(iterator);
            tempCell = row.getCell(iterator);
            tempCell.setCellStyle(userCellStyle);
        }
    }

    public String getCurrentAddress(String line, int rowIterator) {
        double currentDataSize, addressShift;
        double currentGlobalSum = globalSum;
        if (line.contains(STRUCT_BEGIN)) {
            //System.out.println(isStructNested(rowIterator));
            structSum.add(0.0);
            numberOfStruct++;
            return "+" + Double.toString(round(globalSum, 2));
        } else if (line.contains(STRUCT_END)) {
            String outAddressString = "=" + Double.toString(round(structSum.get(numberOfStruct), 2));
            if(numberOfStruct > 1) structSum.set(numberOfStruct-1, structSum.get(numberOfStruct)+structSum.get(numberOfStruct-1));
            structSum.set(numberOfStruct , 0.0);
            numberOfStruct--;
            return outAddressString;
        } else {
            String dataType = line.split(TYPE_SEPARATOR)[1].split(TYPE_END_SEPARATOR)[0].replaceAll("\\s+", "");
            if (line.contains(ASSIGN_DEFAULT_VALUE)) dataType = dataType.split(ASSIGN_DEFAULT_VALUE)[0].replaceAll("\\s+", "");
            currentDataSize = dataTypes.containsKey(dataType) ? dataTypes.get(dataType) : 0;
            if(line.contains("BOOL")){
                isPreviousBool = true;
                addressShift = (checkBoolSumOverflow(globalSum)) ? 0.3 : 0.1;
            }
            else if (isPreviousBool ) {
                /*if (checkBoolSumOverflow(globalSum)){
                    addressShift = currentDataSize;
                }else{
                    addressShift = currentDataSize + 1.0 + getBoolFractionToByte(globalSum);
                    //currentGlobalSum += 1.0 + getBoolFractionToByte(globalSum);
                }*/
                addressShift = currentDataSize;
                isPreviousBool = false;
            } else addressShift = currentDataSize;

            structSum.set(numberOfStruct, structSum.get(numberOfStruct)+addressShift);
            globalSum += addressShift;
            return "+" + Double.toString(round(currentGlobalSum, 2));
        }
    }

    public double getBoolFractionToByte(double num) {
        long iPart = (long) num;
        double fPart = num - iPart;
        return 1.0 - fPart;
    }

    private boolean isStructNested(int rowIterator) {
        return (structEndRows.size()>=1) && rowIterator < structEndRows.get(structEndRows.size()-1);

    }

    private String getNestShifter(int shiftNumbers) {
        if (shiftNumbers >=0) return new String(new char[shiftNumbers]).replace("\0", NEST_SHIFTER);
        return "";
    }

    public boolean checkBoolSumOverflow(double num){
        long iPart = (long) num;
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
