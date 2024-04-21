/*
 * Table Wrapper Spring Boot Starter
 * Copyright (C) 2023  Spacious Team <spacious-team@ya.ru>
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


import org.spacious_team.table_wrapper.api.TableFactoryRegistry;
import org.spacious_team.table_wrapper.csv.CsvReportPage;
import org.spacious_team.table_wrapper.csv.CsvTableFactory;
import org.spacious_team.table_wrapper.excel.ExcelSheet;
import org.spacious_team.table_wrapper.excel.ExcelTableFactory;
import org.spacious_team.table_wrapper.xml.XmlReportPage;
import org.spacious_team.table_wrapper.xml.XmlTableFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@AutoConfiguration
@SuppressWarnings("unused")
@ConditionalOnClass(TableFactoryRegistry.class)
public class TableWrapperAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ExcelTableFactory.class)
    public static class TableWrapperExcelConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ExcelTableFactory excelTableFactory() {
            ExcelTableFactory factory = new ExcelTableFactory();
            TableFactoryRegistry.add(factory);
            return factory;
        }
    }


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(XmlTableFactory.class)
    public static class TableWrapperXmlConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public XmlTableFactory xmlTableFactory() {
            XmlTableFactory factory = new XmlTableFactory();
            TableFactoryRegistry.add(factory);
            return factory;
        }
    }


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CsvTableFactory.class)
    public static class TableWrapperCsvConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CsvTableFactory csvTableFactory() {
            CsvTableFactory factory = new CsvTableFactory();
            TableFactoryRegistry.add(factory);
            return factory;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(DefaultContextAwareReportPageFactory.class)
    public static class TableWrapperConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ContextAwareReportPageFactory defaultReportPageFactory(ApplicationContext context) {
            DefaultContextAwareReportPageFactory factory = new DefaultContextAwareReportPageFactory(context);
            Stream.of(ExcelSheet.class, XmlReportPage.class, CsvReportPage.class)
                    .forEach(factory::registerBeanDefinition);
            return factory;
        }
    }
}
