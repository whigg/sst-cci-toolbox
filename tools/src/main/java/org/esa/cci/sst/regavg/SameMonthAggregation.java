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

package org.esa.cci.sst.regavg;

/**
 * A "same month" (daily, monthly) / regional aggregation
 * that aggregates daily, monthly / 5º,90º cells ({@link AggregationCell5}, {@link AggregationCell90}).
 *
 * @author Norman Fomferra
 */
public interface SameMonthAggregation<A extends AggregationCell> extends RegionalAggregation {
    void accumulate(A cell, double seaCoverage);
}