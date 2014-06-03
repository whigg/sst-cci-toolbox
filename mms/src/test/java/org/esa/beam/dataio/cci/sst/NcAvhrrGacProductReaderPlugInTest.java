package org.esa.beam.dataio.cci.sst;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ralf Quast
 */
public class NcAvhrrGacProductReaderPlugInTest {

    @Test
    public void testGetDecodeQualification() throws Exception {
        final NcAvhrrGacProductReaderPlugIn plugIn = new NcAvhrrGacProductReaderPlugIn();
        final File file = getResourceAsFile("19940101000100-AVHRR-L1b-AVHRR11_G-v02.0-fv01.0.nc");

        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(file));
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(file.getPath()));

        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification("20100601.egr"));
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification("aai_20101224.nc"));
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification("ice_conc_nh_201006301200.hdf"));
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification("ice_conc_sh_qual_201006301200.hdf"));
    }

    @Test
    public void testMatches() throws Exception {
        assertTrue(NcAvhrrGacProductReaderPlugIn.matches("19940101000100-AVHRR-L1b-AVHRR11_G-v02.0-fv01.0.nc"));
    }

    private static File getResourceAsFile(String name) throws URISyntaxException {
        return new File(NcAvhrrGacProductReaderPlugInTest.class.getResource(name).toURI());
    }
}
