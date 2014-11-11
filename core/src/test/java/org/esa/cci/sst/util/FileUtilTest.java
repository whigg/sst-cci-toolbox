package org.esa.cci.sst.util;


import org.esa.cci.sst.tool.ToolException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileUtilTest {

    @Test
    public void testGetFile_pathEmpty() {
        File file = FileUtil.getFile("", "toolHome");
        assertNull(file);

        file = FileUtil.getFile(null, "toolHome");
        assertNull(file);
    }

    @Test
    public void testGetFile_absolutePath() {
        final File absoluteFile = new File(".");
        final String absolutePath = absoluteFile.getAbsolutePath();

        final File file = FileUtil.getFile(absolutePath, "toolHome");
        assertNotNull(file);
        assertEquals(absolutePath, file.getAbsolutePath());
    }

    @Test
    public void testGetFile_relativePath() {
        final File toolHomeFile = new File(".");
        final String toolHome = toolHomeFile.getAbsolutePath();

        final File file = FileUtil.getFile("schneck", toolHome);
        assertNotNull(file);
        assertEquals(toolHome + File.separator + "schneck", file.getAbsolutePath());
    }

    @Test
    public void testGetExistingFile_notExisting() {
        final File toolHomeFile = new File(".");
        final String toolHome = toolHomeFile.getAbsolutePath();

        try {
            FileUtil.getExistingFile("schneck", toolHome);
            fail("ToolException expected");
        } catch (ToolException expected) {
        }
    }

    @Test
    public void testGetExistingFile_existing() throws IOException {
        final File toolHomeFile = new File(".");
        final String toolHome = toolHomeFile.getAbsolutePath();

        final File existingFile = new File(toolHome, "delete_me");
        if (!existingFile.createNewFile()) {
            fail("failed to create test file");
        }
        try {
            final File file = FileUtil.getExistingFile(existingFile.getAbsolutePath(), toolHome);
            assertNotNull(file);
            assertEquals(existingFile.getAbsolutePath(), file.getAbsolutePath());

        } finally {
            if (!existingFile.delete()) {
                fail("failed to delete test file");
            }
        }
    }

    @Test
    public void testGetExistingDirectory_notExisting() {
        final File toolHomeFile = new File(".");
        final String toolHome = toolHomeFile.getAbsolutePath();

        try {
            FileUtil.getExistingDirectory("not_there", toolHome);
            fail("ToolException expected");
        } catch (ToolException expected) {
        }
    }

    @Test
    public void testGetExistingDirectory_existing() throws IOException {
        final File toolHomeFile = new File(".");
        final String toolHome = toolHomeFile.getAbsolutePath();

        final File existingFile = new File(toolHome, "delete_me");
        if (!existingFile.mkdir()) {
            fail("failed to create test directory");
        }
        try {
            final File file = FileUtil.getExistingDirectory(existingFile.getAbsolutePath(), toolHome);
            assertNotNull(file);
            assertEquals(existingFile.getAbsolutePath(), file.getAbsolutePath());

        } finally {
            if (!existingFile.delete()) {
                fail("failed to delete test directory");
            }
        }
    }
}
