package com.company;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

class Main {

    public static String filename, excelFilename;

    public static void main(String[] args) throws java.io.IOException {

        while(null == filename & null == excelFilename){
            parseArguments(args);
        }

        ConvertUdtToExcel obj = new ConvertUdtToExcel(filename, excelFilename);

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

    private static final String TYPE_SEPARATOR = " : ";
    private static final String COMMENT_SEPARATOR = "\\\\";
    private static final String STRUCT_BEGIN = " STRUCT";
    private static final String STRUCT_END = "END_STRUCT";
    private static final String AUTHOR_SEPARATOR = "\'";
    private static final String TYPE_UDT_SEPARATOR = "\"";

    public LinkedHashMap<String, String> tableTitle = new LinkedHashMap<String, String>();

    public XSSFWorkbook myWorkBook;
    public XSSFSheet mySheet;



    public ConvertUdtToExcel(){
        createEmptyTableTitle();
    }

    public ConvertUdtToExcel(String filename, String excelFilename) {
        createEmptyTableTitle();
        this.filename = filename;
        this.excelFilename = excelFilename;
        getTableTitle(filename);
        createExcelFile(excelFilename);
    }

    private void createEmptyTableTitle() {
        tableTitle.put("type", "");
        tableTitle.put("author", "");
        tableTitle.put("version", "");
        tableTitle.put("udt_name_cell", "Name of datatype");
        tableTitle.put("author_cell", "�����: ");
        tableTitle.put("version_cell", "������: ");
        /*tableTitle.put("address_column", "�����");
        tableTitle.put("name_column", "���");
        tableTitle.put("type_column", "��� ������");
        tableTitle.put("comment_column", "�����������");*/
    }

    public String filename , excelFilename;

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

    public  int getStructNesting(String filename) throws FileNotFoundException{
        int numberOfStruct = 0;
        boolean  structEnd = false;
        File structFile = new File(filename);
        Scanner scanner = new Scanner(structFile);
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            if (line.contains(STRUCT_BEGIN) & !structEnd) {
                numberOfStruct++;
            } else if (line.contains(STRUCT_BEGIN)) structEnd = false;
            if (line.contains(STRUCT_END)) structEnd = true;
        }
        scanner.close();
        return numberOfStruct;
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
        for(short rowIterator = 0; rowIterator < 3; rowIterator++){
            Row row = mySheet.createRow(rowIterator);
            Cell cell = row.createCell(0);
            cell.setCellValue("");
            //listTitle.get(rowIterator+3) + listTitle.get(rowIterator)

            mySheet.addMergedRegion(new CellRangeAddress(
                    rowIterator, //first row (0-based)
                    rowIterator, //last row  (0-based)
                    0, //first column (0-based)
                    3  //last column  (0-based)
            ));
        }

        // Write the output to a file



       /* // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = mySheet.iterator();

        // Traversing over each row of XLSX file
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // For each row, iterate through each columns
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {

                Cell cell = cellIterator.next();

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING:
                        System.out.print(cell.getStringCellValue() + "\t");
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        System.out.print(cell.getNumericCellValue() + "\t");
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        System.out.print(cell.getBooleanCellValue() + "\t");
                        break;
                    default :

                }
            }
            System.out.println("");*/
        }

}