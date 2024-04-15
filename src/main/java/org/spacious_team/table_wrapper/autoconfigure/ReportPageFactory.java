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
import org.apache.poi.ss.usermodel.Sheet;
import org.spacious_team.table_wrapper.api.ReportPage;
import org.spacious_team.table_wrapper.csv.CsvReportPage;
import org.spacious_team.table_wrapper.excel.ExcelSheet;
import org.spacious_team.table_wrapper.xml.XmlReportPage;

public class ReportPageFactory {

    public ReportPage create(Object page) {
        // TODO parse all ReportPage implementations constructors and choose appropriate impl by args type
        if (page instanceof Sheet) {
            return new ExcelSheet((Sheet) page);
        } else if (page instanceof Worksheet) {
            return new XmlReportPage((Worksheet) page);
        } else if (page instanceof String[][]) {
            return new CsvReportPage((String[][]) page);
        }
        throw new IllegalArgumentException("Unexpected type: " + page.getClass());
    }
}
