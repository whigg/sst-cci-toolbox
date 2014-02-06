package org.esa.cci.sst.reader;

import org.esa.cci.sst.data.DataFile;
import org.esa.cci.sst.data.Sensor;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Insitu_CCI_2_AccessorTest {

    @Test
    public void testExtractWMOID() {
        DataFile dataFile = createDataFile("/usr/local/data/test/insitu_0_WMOID_71569_20030117_20030131.nc");
        assertEquals("71569", Insitu_CCI_2_Accessor.extractWMOID(dataFile));

        dataFile = createDataFile("C:\\Toms\\Data\\SST\\insitu_5_WMOID_7900016_20030110_20030130.nc");
        assertEquals("7900016", Insitu_CCI_2_Accessor.extractWMOID(dataFile));

        dataFile = createDataFile("C:\\Toms\\Data\\SST\\contains_no_WMO_ID.nc");
        assertNull(Insitu_CCI_2_Accessor.extractWMOID(dataFile));
    }

    private DataFile createDataFile(String pathname) {
        //noinspection deprecation
        return new DataFile(new File(pathname), new Sensor());
    }
}
