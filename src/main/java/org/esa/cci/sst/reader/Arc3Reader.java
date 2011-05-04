/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.cci.sst.reader;

import org.esa.cci.sst.data.DataFile;
import org.esa.cci.sst.data.Observation;
import org.esa.cci.sst.data.VariableDescriptor;
import ucar.nc2.NetcdfFile;

import java.io.IOException;

/**
 * Allows to read from files output by ARC3.
 *
 * @author Thomas Storm
 */
public class Arc3Reader implements ObservationReader {

    private final MmdReader delegate;

    public Arc3Reader(final DataFile dataFile,
                      final NetcdfFile arc3file,
                      final String sensor) {
        delegate = new MmdReader(dataFile, arc3file, sensor);
    }

    @Override
    public Observation readObservation(final int recordNo) throws IOException {
        delegate.validateRecordNumber(recordNo);
        final Observation observation = new Observation();
        delegate.setupObservation(recordNo, observation);
        return observation;
    }

    @Override
    public int getNumRecords() {
        return delegate.getNumRecords();
    }

    @Override
    public VariableDescriptor[] getVariableDescriptors() throws IOException {
        return delegate.getVariableDescriptors();
    }
}
