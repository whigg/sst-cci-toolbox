/*
 * SST_cci Tools
 *
 * Copyright (C) 2011-2013 by Brockmann Consult GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.esa.cci.sst.tool;

/**
 * Simple abstraction of a parameter.
 *
 * @author Norman Fomferra
 */
public class Parameter {

    private final String name;
    private final String argName;
    private final String defaultValue;
    private final String description;
    private final boolean optional;

    public Parameter(String name, String type, String defaultValue, String description) {
        this(name, type, defaultValue, description, false);
    }

    public Parameter(String name, String type, String defaultValue, String description, boolean optional) {
        this.name = name;
        this.argName = type;
        this.defaultValue = defaultValue;
        this.description = description;
        this.optional = optional;
    }

    public String getName() {
        return name;
    }

    public String getArgName() {
        return argName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOptional() {
        return optional;
    }
}
