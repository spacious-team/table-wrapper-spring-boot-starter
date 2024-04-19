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

import org.spacious_team.table_wrapper.api.ReportPage;

import java.io.InputStream;
import java.nio.file.Path;

public interface ReportPageFactory {

    /**
     * Chooses and creates ReportPage implementation by file name extension: "xls", "xlsx", "xml" or "csv".
     * Uses the first sheet for files with extensions "xls", "xlsx", "xml" to create a ReportPage.
     *
     * @throws ReportPageInstantiationException if the file name extension is not expected
     *                                          or ReportPage instance creation fails
     */
    default ReportPage create(Path path) {
        return create(path, 0);
    }

    /**
     * Chooses and creates ReportPage implementation by file name extension: "xls", "xlsx", "xml" or "csv".
     *
     * @param sheetNumber index of the 0-based sheet number for "xls", "xlsx", "xml" files, it doesn't used for csv files
     * @throws ReportPageInstantiationException if the file name extension is not expected
     *                                          or ReportPage instance creation fails
     */
    ReportPage create(Path path, Integer sheetNumber);


    /**
     * Chooses and creates ReportPage implementation by file name extension: "xls", "xlsx", "xml" or "csv".
     *
     * @param sheetName sheet name for "xls", "xlsx", "xml" files, it doesn't used for csv files
     * @throws ReportPageInstantiationException if the file name extension is not expected
     *                                          or ReportPage instance creation fails
     */
    ReportPage create(Path path, String sheetName);

    /**
     * Creates ReportPage implementation from input stream.
     * Uses the first sheet for xls / xlsx / xml files to create a ReportPage.
     *
     * @throws ReportPageInstantiationException if the file name extension is not expected
     *                                          or ReportPage instance creation fails
     */
    default ReportPage create(InputStream is) {
        return create(is, 0);
    }

    /**
     * Creates ReportPage implementation from input stream.
     *
     * @param sheetNumber index of the 0-based sheet number for "xls", "xlsx", "xml" files, it doesn't used for csv files
     * @throws ReportPageInstantiationException if the file name extension is not expected
     *                                          or ReportPage instance creation fails
     */
    ReportPage create(InputStream is, Integer sheetNumber);


    /**
     * Creates ReportPage implementation from input stream.
     *
     * @param sheetName sheet name for "xls", "xlsx", "xml" files, it doesn't used for csv files
     * @throws ReportPageInstantiationException if the file name extension is not expected
     *                                          or ReportPage instance creation fails
     */
    ReportPage create(InputStream is, String sheetName);

    /**
     * Chooses ReportPage implementation by its constructor argument types and creates it.
     * Use this method if ReportPage should be created with nonstandard configuration.
     *
     * @throws ReportPageInstantiationException if ReportPage implementation constructor not found
     *                                          or ReportPage instance creation fails
     */
    ReportPage create(Object... args);
}
