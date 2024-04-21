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

import lombok.RequiredArgsConstructor;
import org.spacious_team.table_wrapper.api.ReportPage;
import org.spacious_team.table_wrapper.csv.CsvReportPage;
import org.spacious_team.table_wrapper.excel.ExcelSheet;
import org.spacious_team.table_wrapper.xml.XmlReportPage;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@RequiredArgsConstructor
public class DefaultReportPageFactory extends AbstractReportPageFactory {

    private final List<Class<? extends ReportPage>> knownReportPageTypes = new CopyOnWriteArrayList<>();
    private final ApplicationContext context;

    @PostConstruct
    public void init() {
        Stream.of(ExcelSheet.class, XmlReportPage.class, CsvReportPage.class)
                .forEach(this::registerBeanDefinition);
    }

    @Override
    public void registerBeanDefinition(Class<? extends ReportPage> clazz) {
        knownReportPageTypes.add(clazz);
        String beanName = getBeanName(clazz);
        BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(clazz)
                .setScope(SCOPE_PROTOTYPE)
                .getBeanDefinition();
        getBeanDefinitionRegistry()
                .registerBeanDefinition(beanName, beanDefinition);
    }

    private static String getBeanName(Class<?> clazz) {
        char[] chars = clazz.getSimpleName().toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private BeanDefinitionRegistry getBeanDefinitionRegistry() {
        AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
        return (BeanDefinitionRegistry) factory;
    }

    @Override
    public ReportPage create(Object... args) {
        for (Class<? extends ReportPage> clazz : knownReportPageTypes) {
            try {
                return context.getBean(clazz, args);
            } catch (Exception ignore) {
            }
        }
        throw new ReportPageInstantiationException("Can't create ReportPage with arguments: " + List.of(args));
    }
}
