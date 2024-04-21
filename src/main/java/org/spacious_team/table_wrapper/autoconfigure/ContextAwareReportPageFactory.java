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
import java.util.List;

public interface ContextAwareReportPageFactory extends ReportPageFactory {

    /**
     * Returns the ReportPage implementation classes that can be instantiated by this factory.
     */
    List<Class<? extends ReportPage>> getRegisteredReportPageTypes();

    /**
     * Register prototype spring bean definition for ReportPage implementation.
     * This class instance can subsequently be created using the method {@link #create(Object...)}
     */
    void registerBeanDefinition(Class<? extends ReportPage> clazz);

    /**
     * Chooses ReportPage implementation by its constructor argument types and creates it.
     * Use this method if ReportPage should be created with nonstandard configuration.
     *
     * @throws ReportPageInstantiationException if ReportPage implementations' constructor not found
     *                                          or ReportPage instance creation fails
     * @see ReportPageFactory#create(Path)
     * @see ReportPageFactory#create(InputStream)
     */
    ReportPage create(Object... args);
}
