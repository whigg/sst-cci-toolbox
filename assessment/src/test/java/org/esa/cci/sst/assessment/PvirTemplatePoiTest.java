package org.esa.cci.sst.assessment;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PvirTemplatePoiTest {

    private File dataDir;
    private PoiWordDocument document;
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        document = new PoiWordDocument(CarTemplateTest.class.getResource("PVIR_appendix_example_v3.docx"));
        properties = new Properties();
        properties.load(CarTemplateTest.class.getResourceAsStream("pvir-template.properties"));

        dataDir = new File(System.getProperty("user.home"), "scratch/pvir/figures");
    }

    @After
    public void tearDown() throws Exception {
        final File targetFile = new File("pvir.docx");
        document.save(targetFile);

        if (targetFile.isFile()) {
            if (!targetFile.delete()) {
                fail("Unable to delete test file");
            }
        }
    }

    @Test
    public void testReplaceTextVariables() throws Exception {
        final String sensor = properties.getProperty("word.sensor");

        assertTrue(document.containsVariable("${word.sensor}"));
        document.replaceWithText("${word.sensor}", sensor);
        assertFalse(document.containsVariable("${word.sensor}"));

        final String insitu = properties.getProperty("word.insitu");

        assertTrue(document.containsVariable("${word.insitu}"));
        document.replaceWithText("${word.insitu}", insitu);
        assertFalse(document.containsVariable("${word.insitu}"));

        final String averaging = properties.getProperty("word.averaging");
        assertTrue(document.containsVariable("${word.averaging}"));
        document.replaceWithText("${word.averaging}", averaging);
        assertFalse(document.containsVariable("${word.averaging}"));

        final String depthOrSkin = properties.getProperty("word.depth_or_skin");
        assertTrue(document.containsVariable("${word.depth_or_skin}"));
        document.replaceWithText("${word.depth_or_skin}", depthOrSkin);
        assertFalse(document.containsVariable("${word.depth_or_skin}"));
    }

    @Test
    public void testReplaceVariableWithFigure() throws IOException, InvalidFormatException {
        if (!dataDir.isDirectory()) {
            System.out.println("data directory nor found, skipping `testReplaceVariableWithFigure`");
            return;
        }

        final String dependenceImage = properties.getProperty("figure.dependence-av1");
        File imageFile = new File(dataDir, dependenceImage);
        assertTrue(document.containsVariable("${figure.dependence-av1}"));
        document.replaceWithFigure("${figure.dependence-av1}", imageFile);
        assertFalse(document.containsVariable("${figure.dependence-av1}"));

        final String spatialImage = properties.getProperty("figure.spatial-av1");
        imageFile = new File(dataDir, spatialImage);
        assertTrue(document.containsVariable("${figure.spatial-av1}"));
        document.replaceWithFigure("${figure.spatial-av1}", imageFile);
        assertFalse(document.containsVariable("${figure.spatial-av1}"));

        final String histogramImage = properties.getProperty("figure.histogram-av1");
        imageFile = new File(dataDir, histogramImage);
        assertTrue(document.containsVariable("${figure.histogram-av1}"));
        document.replaceWithFigure("${figure.histogram-av1}", imageFile);
        assertFalse(document.containsVariable("${figure.histogram-av1}"));

        final String uncertImage = properties.getProperty("figure.uncert_val-av1");
        imageFile = new File(dataDir, uncertImage);
        assertTrue(document.containsVariable("${figure.uncert_val-av1}"));
        document.replaceWithFigure("${figure.uncert_val-av1}", imageFile);
        assertFalse(document.containsVariable("${figure.uncert_val-av1}"));
    }

    @Test
    public void testReplaceVariableWithFigure_andScaling() throws IOException, InvalidFormatException {
        if (!dataDir.isDirectory()) {
            System.out.println("data directory nor found, skipping `testReplaceVariableWithFigure`");
            return;
        }

        final String dependenceImage = properties.getProperty("figure.dependence-av1");
        final String scaleProperty = properties.getProperty("figure.dependence-av1.scale");
        File imageFile = new File(dataDir, dependenceImage);

        assertTrue(document.containsVariable("${figure.dependence-av1}"));
        document.replaceWithFigure("${figure.dependence-av1}", imageFile, Double.parseDouble(scaleProperty));
        assertFalse(document.containsVariable("${figure.dependence-av1}"));
    }
}
