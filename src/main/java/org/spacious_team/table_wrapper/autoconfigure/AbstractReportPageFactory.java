/*
 * Table Wrapper Spring Boot Starter
 * Copyright (C) 2024  Spacious Team <spacious-team@ya.ru>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.spacious_team.table_wrapper.autoconfigure;


import nl.fountain.xelem.excel.Worksheet;
import nl.fountain.xelem.lex.ExcelReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.spacious_team.table_wrapper.api.ReportPage;
import org.spacious_team.table_wrapper.api.TableCellAddress;
import org.spacious_team.table_wrapper.csv.CsvReportPage;
import org.spacious_team.table_wrapper.excel.ExcelSheet;
import org.spacious_team.table_wrapper.xml.XmlReportPage;
import org.springframework.util.Assert;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;
import static java.util.Objects.nonNull;
import static org.spacious_team.table_wrapper.autoconfigure.AbstractReportPageFactory.KnownFileExtension.XLS;
import static org.spacious_team.table_wrapper.autoconfigure.AbstractReportPageFactory.KnownFileExtension.XLSX;

/**
 * Implements methods for quickly instantiating ReportPage implementations:
 *  {@link ExcelSheet}, {@link XmlReportPage} and {@link CsvReportPage}
 * using well-known constructors.
 */
public abstract class AbstractReportPageFactory implements ReportPageFactory {

    @Override
    public ReportPage create(Path path, Integer sheetNumber) {
        return doCreate(path, sheetNumber);
    }

    @Override
    public ReportPage create(Path path, String sheetName) {
        return doCreate(path, sheetName);
    }

    protected ReportPage doCreate(Path path, Object sheetId) {
        try {
            InputStream is;
            KnownFileExtension extension = KnownFileExtension.valueOf(path);
            switch (extension) {
                case XLS:
                case XLSX:
                    Assert.isTrue(nonNull(sheetId), "Excel file's sheet number or name expected");
                    is = openForRead(path);
                    Sheet sheet = getExcelSheet(is, sheetId, extension);
                    return new ExcelSheet(sheet);
                case XML:
                    Assert.isTrue(nonNull(sheetId), "Xml file's sheet number or name expected");
                    is = openForRead(path);
                    Worksheet worksheet = getXmlSheet(is, sheetId);
                    return new XmlReportPage(worksheet);
                case CSV:
                    return new CsvReportPage(path);
            }
        } catch (Exception e) {
            throw new ReportPageInstantiationException("Can't open path: " + path, e);
        }
        throw new ReportPageInstantiationException("Can't open file (unknown file extension): " + path);
    }

    /**
     * @implSpec Does not close InputStream
     */
    private static InputStream openForRead(Path path) throws IOException {
        return Files.newInputStream(path, READ);
    }

    /**
     * @implNote Closes InputStream
     */
    private static Sheet getExcelSheet(InputStream is, Object sheetId, KnownFileExtension extension) throws IOException {
        Workbook workbook = getExcelWorkbook(is, extension);
        if (sheetId instanceof Integer) {
            Sheet sheet = workbook.getSheetAt((Integer) sheetId);
            Assert.notNull(sheet, () -> "Excel sheet not found: " + sheetId);
            return sheet;
        } else if (sheetId instanceof CharSequence) {
            String sheetName = String.valueOf(sheetId);
            Sheet sheet = workbook.getSheet(sheetName);
            Assert.notNull(sheet, () -> "Excel sheet not found: " + sheetId);
            return sheet;
        }
        String sheetIdType = sheetId.getClass().getSimpleName();
        throw new ReportPageInstantiationException("Unexpected Excel Sheet identifier type:" + sheetIdType);
    }

    /**
     * @implNote Closes InputStream
     */
    private static Workbook getExcelWorkbook(InputStream is, KnownFileExtension extension) throws IOException {
        if (extension == XLS) {
            return new HSSFWorkbook(is);  // constructor closes InputStream
        } else {
            return new XSSFWorkbook(is);  // constructor closes InputStream
        }
    }

    private static Worksheet getXmlSheet(InputStream is, Object sheetId) throws Exception {
        nl.fountain.xelem.excel.Workbook workbook = getXmlWorkbook(is);
        if (sheetId instanceof Integer) {
            Worksheet worksheet = workbook.getWorksheetAt((Integer) sheetId);
            Assert.notNull(worksheet, () -> "Xml sheet not found: " + sheetId);
            return worksheet;
        } else if (sheetId instanceof CharSequence) {
            String sheetName = String.valueOf(sheetId);
            Worksheet worksheet = workbook.getWorksheet(sheetName);
            Assert.notNull(worksheet, () -> "Xml sheet not found: " + sheetId);
            return worksheet;
        }
        String sheetIdType = sheetId.getClass().getSimpleName();
        throw new ReportPageInstantiationException("Unexpected Excel Sheet identifier type:" + sheetIdType);
    }

    private static nl.fountain.xelem.excel.Workbook getXmlWorkbook(InputStream is) throws Exception {
        ExcelReader reader = new ExcelReader();
        is = skipNewLines(is); // required by ExcelReader
        InputSource source = new InputSource(is);
        return reader.getWorkbook(source);

    }

    private static InputStream skipNewLines(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        int symbol;
        do {
            bis.mark(8);
            symbol = bis.read();
        } while (symbol == '\n' || symbol == '\r');
        bis.reset();
        return bis;
    }

    @Override
    public ReportPage create(InputStream is, Integer sheetNumber) {
        return doCreate(is, sheetNumber);
    }

    @Override
    public ReportPage create(InputStream is, String sheetName) {
        return doCreate(is, sheetName);
    }

    public ReportPage doCreate(InputStream is, Object sheetId) {
        try {
            ByteArrayInputStream bais = convertToByteArrayInputStream(is);
            // try Excel file
            for (KnownFileExtension extension : new KnownFileExtension[]{XLS, XLSX}) {
                try {
                    Sheet sheet = getExcelSheet(bais, sheetId, extension);
                    return new ExcelSheet(sheet);
                } catch (Exception ignore) {
                    bais.reset();
                }
            }
            // try Xml file
            try {
                Worksheet sheet = getXmlSheet(bais, sheetId);
                return new XmlReportPage(sheet);
            } catch (Exception ignore) {
                bais.reset();
            }
            // try Csv file
            int size = bais.available();
            CsvReportPage reportPage = new CsvReportPage(bais);
            if (size > 0 && isEmptyCsvReportPage(reportPage)) {
                throw new ReportPageInstantiationException("Unexpected binary data");
            }
            return reportPage;
        } catch (Exception e) {
            throw new ReportPageInstantiationException("Unexpected data format", e);
        }
    }

    /**
     * @implSpec Closes InputStream
     */
    public static ByteArrayInputStream convertToByteArrayInputStream(InputStream inputStream) throws IOException {
        if (inputStream instanceof ByteArrayInputStream) {
            return (ByteArrayInputStream) inputStream;
        } else {
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();
            return new ByteArrayInputStream(bytes);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static boolean isEmptyCsvReportPage(CsvReportPage reportPage) {
        // has only one Cell with NULL value
        return reportPage.getLastRowNum() == 0 &&
                reportPage.getCell(TableCellAddress.of(0, 0)) != null &&
                reportPage.getRow(0).getCell(0).getValue() == null &&
                reportPage.getCell(TableCellAddress.of(0, 1)) == null &&
                reportPage.getCell(TableCellAddress.of(1, 0)) == null;
    }

    enum KnownFileExtension {
        XLS, XLSX, XML, CSV;

        static KnownFileExtension valueOf(Path path) {
            String fileName = path.getFileName().toString();
            String[] fileNameParts = fileName.split("\\.");
            String extension = fileNameParts[fileNameParts.length - 1];
            return valueOf(extension.toUpperCase());
        }
    }
}
