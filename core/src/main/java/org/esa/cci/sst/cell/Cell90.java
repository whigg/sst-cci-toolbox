/*
 * Copyright (c) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.cci.sst.cell;

import org.esa.cci.sst.aggregate.AggregationContext;

public final class Cell90 extends DefaultCellAggregationCell {

    public Cell90(AggregationContext context, int cellX, int cellY) {
        super(context, cellX, cellY);
    }

    @Override
    public double getCoverageUncertainty() {
        final double uncertainty5 = super.getCoverageUncertainty();
        final double uncertainty90 = getAggregationContext().getCoverageUncertaintyProvider().calculate(this, 90.0);
        return Math.sqrt(uncertainty5 * uncertainty5 + uncertainty90 * uncertainty90);
    }
}
