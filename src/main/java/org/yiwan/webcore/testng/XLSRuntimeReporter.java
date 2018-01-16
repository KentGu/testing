package org.yiwan.webcore.testng;

import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.testng.ITestResult;
import org.testng.internal.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class XLSRuntimeReporter {
    private static final String TEST_SHEET = "Test Summary";
    private static final String STEP_SHEET = "Step Summary";
    private String reportName = "runtime-report.xls";
    private String reportPath = new File("").getAbsolutePath() + "\\target\\";

    public XLSRuntimeReporter(String reportName) {
        super();
        this.reportName = reportName;
    }

    /**
     * get status description from the testng test status id
     *
     * @param status
     * @return test case status string
     */
    private String getStatus(int status) {
        switch (status) {
            case 1:
                return "Passed";
            case 2:
                return "Failed";
            case 3:
                return "Skipped";
            case 4:
                return "SuccessPercentageFailure";
            case 16:
                return "Started";
            default:
                return "unkown";
        }
    }

    public void generateReport() throws IOException {
        Workbook workbook = openReport(reportPath + reportName);

        // cell style
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        // cell font style
        Font font = workbook.createFont();
        // font.setFontHeightInPoints((short)24); //font size
        // font.setFontName("楷体");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);// bold or not
        font.setColor(IndexedColors.WHITE.index);// font color
        style.setFont(font);

        if (workbook.getSheet(TEST_SHEET) == null) {
            Sheet testSheet = workbook.createSheet(TEST_SHEET);
            Row testRow = testSheet.createRow(0);
            testRow.createCell(0).setCellValue("TestCase");
            testRow.getCell(0).setCellStyle(style);
            testRow.createCell(1).setCellValue("Status");
            testRow.getCell(1).setCellStyle(style);
        }

        if (workbook.getSheet(STEP_SHEET) == null) {
            Sheet stepSheet = workbook.createSheet(STEP_SHEET);
            Row stepRow = stepSheet.createRow(0);
            stepRow.createCell(0).setCellValue("TestCase");
            stepRow.getCell(0).setCellStyle(style);
            stepRow.createCell(1).setCellValue("TestStep");
            stepRow.getCell(1).setCellStyle(style);
            stepRow.createCell(2).setCellValue("Status");
            stepRow.getCell(2).setCellStyle(style);
            stepRow.createCell(3).setCellValue("Description");
            stepRow.getCell(3).setCellStyle(style);
            stepRow.createCell(4).setCellValue("Comments");
            stepRow.getCell(4).setCellStyle(style);
        }
        saveReport(workbook, reportPath + reportName);
    }

    /**
     * update test report
     *
     * @param testResult
     */
    public void updateReport(ITestResult testResult) throws IOException {
        Workbook workbook = openReport(reportPath + reportName);
        Integer testRow = updateTestSheet(workbook, testResult);
        Integer stepRow = updateStepSheet(workbook, testResult);
        // set hyperlink between test sheet and step sheet
        if (testRow != null && stepRow != null) {
            HSSFHyperlink link = new HSSFHyperlink(HSSFHyperlink.LINK_DOCUMENT);
            link.setAddress("'" + STEP_SHEET + "'!A" + stepRow);
            Row row = workbook.getSheet(TEST_SHEET).getRow(testRow);
            row.getCell(1).setHyperlink(link);
        }
        saveReport(workbook, reportPath + reportName);
    }

    /**
     * update test summary sheet result
     *
     * @param workbook
     * @param testResult
     * @return row number
     */
    private Integer updateTestSheet(Workbook workbook, ITestResult testResult) {
        String testCase = testResult.getInstance().getClass().getSimpleName()
                .replace("Test", "").replaceFirst("^0*", "");
        Sheet sheet = workbook.getSheet(TEST_SHEET);
        Iterator<Row> it = sheet.iterator();

        // try to update exiting test result
        while (it.hasNext()) {
            Row row = it.next();
            String caseId = row.getCell(0).getStringCellValue();
            if (testCase.equals(caseId)) {
                // if the new status is not skipped or started, then update it
                if (testResult.getStatus() != 3 && testResult.getStatus() != 16) {
                    row.getCell(1).setCellValue(
                            getStatus(testResult.getStatus()));
                    setCellStyle(workbook, testResult, row.getCell(1));
                }
                return row.getRowNum();
            }
        }

        // add a new test result
        int rowNum = sheet.getPhysicalNumberOfRows();
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(testCase);
        row.createCell(1).setCellValue(getStatus(testResult.getStatus()));
        setCellStyle(workbook, testResult, row.getCell(1));
        return row.getRowNum();
    }

    /**
     * update test step sheet result
     *
     * @param workbook
     * @param testResult
     * @return row number
     */
    private Integer updateStepSheet(Workbook workbook, ITestResult testResult) {
        String testStep = testResult.getMethod().getMethodName();
        Sheet sheet = workbook.getSheet(STEP_SHEET);
        int rowNum = sheet.getPhysicalNumberOfRows();
        // step status is not equal to started
        if (testResult.getStatus() != 16) {
            Row row = sheet.createRow(rowNum);
            String testCase = testResult.getInstance().getClass()
                    .getSimpleName().replace("Test", "")
                    .replaceFirst("^0*", "");
            row.createCell(0).setCellValue(testCase);
            row.createCell(1).setCellValue(testStep);
            row.createCell(2).setCellValue(getStatus(testResult.getStatus()));
            setCellStyle(workbook, testResult, row.getCell(2));
            row.createCell(3).setCellValue(
                    testResult.getMethod().getDescription());
            // if having exception and not the org.testng.SkipException
            if (testResult.getThrowable() != null
                    && testResult.getStatus() != 3)
                row.createCell(4).setCellValue(
                        Utils.stackTrace(testResult.getThrowable(), false)[0]);
            return row.getRowNum();
        }
        return rowNum;
    }

    /**
     * set cell style by the test status
     *
     * @param workbook
     * @param testResult
     * @param cell
     */
    private void setCellStyle(Workbook workbook, ITestResult testResult,
                              Cell cell) {
        CellStyle style = workbook.createCellStyle();
        switch (testResult.getStatus()) {
            case 1:// passed
                style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                break;
            case 2:// failed
                style.setFillForegroundColor(IndexedColors.RED.getIndex());
                break;
            case 3:// skpped
                style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                break;
            default:
                style.setFillForegroundColor(IndexedColors.GOLD.getIndex());
                break;
        }
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }

    /**
     * if file exists then open it, if file doesn't exit then create it
     *
     * @param filePath
     * @return Workbook
     */
    private Workbook openReport(String filePath) throws IOException {
        Workbook workbook = null;
        File file = new File(filePath);
        if (!file.exists()) {
            workbook = new HSSFWorkbook();
        } else {
            FileInputStream ins = new FileInputStream(filePath);
            workbook = new HSSFWorkbook(ins);
        }
        return workbook;
    }

    /**
     * save file content into the disk
     *
     * @param workbook
     * @param filePath
     */
    private void saveReport(Workbook workbook, String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        workbook.write(fos);
    }
}
