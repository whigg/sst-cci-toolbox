/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.cci.sst.rules;

/**
 * Replaces the second, third, and fourth dimension with atsr.nwp.nz, atsr.nwp.ny, and atsr.nwp.nx, respectively.
 *
 * @author Thomas Storm
 */
@SuppressWarnings({"ClassTooDeepInInheritanceTree", "UnusedDeclaration"})
final class AtsrNwpXyzDimensions extends AbstractDimensionReplacement {

    @Override
    protected void replaceDimensions(DimensionStringBuilder builder) throws RuleException {
        builder.replace(1, "atsr.nwp.nz");
        builder.replace(2, "atsr.nwp.ny");
        builder.replace(3, "atsr.nwp.nx");
    }
}
