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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.spacious_team.table_wrapper.autoconfigure.ReportPageFactoryTestFileCreator.*;

public class DefaultReportPageFactoryTest {

    DefaultReportPageFactory factory = new DefaultReportPageFactory();

    @BeforeAll
    static void beforeTests() {
        creteFiles();
    }

    @AfterAll
    static void afterTests() {
        deleteFiles();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.xls", "test.xlsx", "test.xml", "test.csv"})
    void create_firstSheetByPath_ok(String fileName) {
        Path path = getPath(fileName);
        assertNotNull(factory.create(path));
        assertNotNull(factory.create(path, 0));
        assertNotNull(factory.create(path, sheetName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.xls", "test.xlsx", "test.xml", "test.csv", "test.txt"})
    void create_firstSheetByInputStream_ok(String fileName) throws IOException {
        InputStream is = getInputStream(fileName);
        is.mark(Integer.MAX_VALUE);
        assertNotNull(factory.create(is));
        is.reset();
        assertNotNull(factory.create(is, 0));
        is.reset();
        assertNotNull(factory.create(is, sheetName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.txt", "test.bin"})
    void create_byPathWithUnknownFilenameExt_exception(String fileName) {
        Path path = getPath(fileName);
        assertThrows(ReportPageInstantiationException.class, () -> factory.create(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.bin"})
    void create_byInputStreamOfBinaryFile_exception(String fileName) {
        InputStream is = getInputStream(fileName);
        assertThrows(ReportPageInstantiationException.class, () -> factory.create(is));
    }

    // Test second sheet by index

    @ParameterizedTest
    @ValueSource(strings = {"test.xls", "test.xlsx", "test.xml", "test.txt", "test.bin"})
    void create_secondSheetByPath_exception(String fileName) {
        Path path = getPath(fileName);
        assertThrows(ReportPageInstantiationException.class, () -> factory.create(path, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.csv"})
    void create_secondSheetForCsvByPath_ok(String fileName) {
        Path path = getPath(fileName);
        assertNotNull(factory.create(path, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.csv", "test.txt"})
    void create_secondSheetForCsvByInputStream_ok(String fileName) {
        InputStream is = getInputStream(fileName);
        assertNotNull(factory.create(is, 1));
    }

    // Test sheet by name

    @ParameterizedTest
    @ValueSource(strings = {"test.xls", "test.xlsx", "test.xml", "test.txt", "test.bin"})
    void create_namedSheetByPath_exception(String fileName) {
        Path path = getPath(fileName);
        assertThrows(ReportPageInstantiationException.class, () -> factory.create(path, "SheetB"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.csv"})
    void create_namedSheetForCsvByPath_ok(String fileName) {
        Path path = getPath(fileName);
        assertNotNull(factory.create(path, "SheetB"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.csv", "test.txt"})
    void create_namedSheetForCsvByInputStream_ok(String fileName) {
        InputStream is = getInputStream(fileName);
        assertNotNull(factory.create(is, "SheetB"));
    }

    // test InputStream closing

    @Test
    void create_byteArrayInputStream_closed() throws IOException {
        InputStream is = spy(new ByteArrayInputStream(new byte[0]));
        assertNotNull(factory.create(is));
        verify(is, atLeast(1)).close();
    }

    @Test
    void create_instanceOfByteArrayInputStream_notClosed() throws IOException {
        InputStream is = spy(new ByteArrayInputStream(new byte[0]) {
            // own class impl extending ByteArrayInputStream
        });
        assertNotNull(factory.create(is));
        verify(is, never()).close();
    }

    @Test
    void create_notByteArrayInputStreamImpl_notClosed() throws IOException {
        InputStream is = spy(new InputStream() {
            @Override
            public int read() {
                return -1;
            }
        });
        assertNotNull(factory.create(is));
        verify(is, never()).close();
    }
}
