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

import lombok.SneakyThrows;
import nl.fountain.xelem.XSerializer;
import nl.fountain.xelem.XelemException;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Workbook;
import nl.fountain.xelem.excel.Worksheet;
import nl.fountain.xelem.excel.ss.XLWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.FileSystemUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class AbstractReportPageFactoryTestFileCreator {
    static final Path root = Path.of("target", "test-classes", "test-data");
    static final String sheetName = "SheetA";

    @SneakyThrows
    static void creteFiles() {
        createRoot();
        createBinFile("test.bin");
        createCsvFile("test.csv");
        createCsvFile("test.txt");
        createXmlFile("test.xml");
        createExcelFile("test.xls", new HSSFWorkbook());
        createExcelFile("test.xlsx", new XSSFWorkbook());
    }

    @SneakyThrows
    static void createRoot() {
        Files.createDirectories(root);
    }

    @SneakyThrows
    static void deleteFiles() {
        FileSystemUtils.deleteRecursively(root);
    }

    static Path getPath(String fileName) {
        return root.resolve(fileName);
    }

    @SneakyThrows
    static InputStream getInputStream(String fileName) {
        Path path = getPath(fileName);
        try (InputStream is = Files.newInputStream(path)) {
            return new ByteArrayInputStream(is.readAllBytes());  // read all bytes here for release file's InputStream
        }
    }

    static void createBinFile(@SuppressWarnings("SameParameterValue") String fileName) throws IOException {
        Path path = getPath(fileName);
        byte[] bytes = new byte[]{0, 4, 5, 6};
        Files.write(path, bytes);
    }

    static void createCsvFile(String fileName) throws IOException {
        List<String> lines = List.of("Table 1", "a;b;c", "1;2;3", "a4;b5;c6");
        Path path = getPath(fileName);
        Files.write(path, lines);
    }

    static void createXmlFile(@SuppressWarnings("SameParameterValue") String fileName) throws XelemException {
        Workbook workbook = new XLWorkbook();
        Path path = getPath(fileName);
        workbook.setFileName(path.toString());

        Worksheet worksheet = workbook.addSheet(sheetName);
        Row row = worksheet.addRow();
        row.addCell();
        row.addCell().setData("Table 1");
        row = worksheet.addRow();
        row.addCell().setData("a");
        row.addCell().setData("b");
        row.addCell().setData("c");
        row = worksheet.addRow();
        row.addCell().setData(1);
        row.addCell().setData(2);
        row.addCell().setData(3);
        row = worksheet.addRow();
        row.addCell().setData("a4");
        row.addCell().setData("b5");
        row.addCell().setData("c6");

        new XSerializer().serialize(workbook);
    }

    static void createExcelFile(String fileName, org.apache.poi.ss.usermodel.Workbook workbook) throws IOException {
        Path path = getPath(fileName);
        FileOutputStream fos = new FileOutputStream(path.toFile());

        Sheet sheet = workbook.createSheet(sheetName);
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Table 1");
        row = sheet.createRow(1);
        row.createCell(0).setCellValue("a");
        row.createCell(1).setCellValue("b");
        row.createCell(2).setCellValue("c");
        row = sheet.createRow(2);
        row.createCell(0).setCellValue(1);
        row.createCell(1).setCellValue(2);
        row.createCell(2).setCellValue(3);
        row = sheet.createRow(3);
        row.createCell(0).setCellValue("a4");
        row.createCell(1).setCellValue("b5");
        row.createCell(2).setCellValue("c6");


        workbook.write(fos);
    }
}
