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
import nl.fountain.xelem.excel.ss.XLWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spacious_team.table_wrapper.api.ReportPage;
import org.spacious_team.table_wrapper.api.ReportPageRow;
import org.spacious_team.table_wrapper.api.TableCellAddress;
import org.spacious_team.table_wrapper.csv.CsvReportPage;
import org.spacious_team.table_wrapper.excel.ExcelSheet;
import org.spacious_team.table_wrapper.xml.XmlReportPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.spacious_team.table_wrapper.autoconfigure.ReportPageFactoryTestFileCreator.*;

@SpringBootTest(classes = TableWrapperAutoConfiguration.class)
public class DefaultContextAwareReportPageFactoryTest {

    @Autowired
    DefaultContextAwareReportPageFactory factory;

    @BeforeAll
    static void beforeTests() {
        createRoot();
    }

    @AfterAll
    static void afterTests() {
        deleteFiles();
    }

    @Test
    void create_excelSheet_ok() throws IOException {
        for (Workbook workbook : List.of(new HSSFWorkbook(), new XSSFWorkbook())) {
            try (workbook) {
                Sheet sheet1 = workbook.createSheet();
                Sheet sheet2 = workbook.createSheet();

                ReportPage reportPage1 = factory.create(sheet1);
                ReportPage reportPage2 = factory.create(sheet2);

                assertNotSame(reportPage1, reportPage2);
                assertSame(ExcelSheet.class, reportPage1.getClass());
                assertSame(ExcelSheet.class, reportPage2.getClass());
            }
        }
    }

    @Test
    void create_xmlReportPage_ok() {
        XLWorkbook workbook = new XLWorkbook();
        Worksheet worksheet1 = workbook.addSheet();
        Worksheet worksheet2 = workbook.addSheet();

        ReportPage reportPage1 = factory.create(worksheet1);
        ReportPage reportPage2 = factory.create(worksheet2);

        assertNotSame(reportPage1, reportPage2);
        assertSame(XmlReportPage.class, reportPage1.getClass());
        assertSame(XmlReportPage.class, reportPage2.getClass());
    }

    @Test
    void create_csvReportPageFromArray_ok() {
        Object table1 = new String[][]{{"a"}, {"1"}};
        Object table2 = new String[][]{{"a"}, {"1"}};

        ReportPage reportPage1 = factory.create(table1);
        ReportPage reportPage2 = factory.create(table2);

        assertNotSame(reportPage1, reportPage2);
        assertSame(CsvReportPage.class, reportPage1.getClass());
        assertSame(CsvReportPage.class, reportPage2.getClass());
    }

    @Test
    void create_csvReportPageFromPath_ok() throws IOException {
        createCsvFile("file1.any");
        createCsvFile("file2.any");
        Object path1 = getPath("file1.any");
        Object path2 = getPath("file2.any");

        ReportPage reportPage1 = factory.create(path1);
        ReportPage reportPage2 = factory.create(path2);

        assertNotSame(reportPage1, reportPage2);
        assertSame(CsvReportPage.class, reportPage1.getClass());
        assertSame(CsvReportPage.class, reportPage2.getClass());
    }

    @Test
    void create_csvReportPageFromInputStream_ok() throws IOException {
        createCsvFile("file1");
        createCsvFile("file2");
        Object is1 = getInputStream("file1");
        Object is2 = getInputStream("file2");

        ReportPage reportPage1 = factory.create(is1);
        ReportPage reportPage2 = factory.create(is2);

        assertNotSame(reportPage1, reportPage2);
        assertSame(CsvReportPage.class, reportPage1.getClass());
        assertSame(CsvReportPage.class, reportPage2.getClass());
    }

    @Test
    void create_csvReportPageFromInputStreamAndSettings_ok() throws IOException {
        createCsvFile("file1");
        createCsvFile("file2");
        Object is1 = getInputStream("file1");
        Object is2 = getInputStream("file2");

        ReportPage reportPage1 = factory.create(is1, UTF_8, CsvReportPage.getDefaultCsvParserSettings());
        ReportPage reportPage2 = factory.create(is2, UTF_8, CsvReportPage.getDefaultCsvParserSettings());

        assertNotSame(reportPage1, reportPage2);
        assertSame(CsvReportPage.class, reportPage1.getClass());
        assertSame(CsvReportPage.class, reportPage2.getClass());
    }

    @Test
    void create_unknownConstructorArgTypes_exception() {
        // impl with constructor of (String, String) types not found
        assertThrows(ReportPageInstantiationException.class, () -> factory.create("arg1", "arg2", "arg3"));
    }

    @Test
    void registerBeanDefinition() {
        int registeredTypes = factory.getRegisteredReportPageTypes().size();

        factory.registerBeanDefinition(TestReportPage.class);
        ReportPage reportPage = factory.create("arg1", "arg2");

        assertSame(TestReportPage.class, reportPage.getClass());
        assertEquals(registeredTypes + 1, factory.getRegisteredReportPageTypes().size());
        assertSame(TestReportPage.class, factory.getRegisteredReportPageTypes().get(registeredTypes));
    }


    private static class TestReportPage implements ReportPage {

        @SuppressWarnings("unused")
        public TestReportPage(String arg1, String arg2) {
        }

        @Override
        public TableCellAddress find(Object value, int startRow, int endRow, int startColumn, int endColumn) {
            return TableCellAddress.NOT_FOUND;
        }

        @Override
        public TableCellAddress find(int startRow, int endRow,
                                     int startColumn, int endColumn,
                                     Predicate<Object> cellValuePredicate) {
            return TableCellAddress.NOT_FOUND;
        }

        @Override
        @SuppressWarnings("ReturnOfNull")
        public ReportPageRow getRow(int i) {
            return null;
        }

        @Override
        public int getLastRowNum() {
            return -1;
        }
    }
}