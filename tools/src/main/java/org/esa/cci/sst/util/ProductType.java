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

package org.esa.cci.sst.util;

import org.esa.cci.sst.common.ProcessingLevel;
import org.esa.cci.sst.common.cellgrid.GridDef;
import org.esa.cci.sst.common.file.*;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

/**
 * Represents the product types handled by the {@link org.esa.cci.sst.regavg.RegionalAverageTool}.
 *
 * @author Norman Fomferra, Bettina Scholze
 */
public enum ProductType {
    ARC_L3U(ArcL3UFileType.INSTANCE, ProcessingLevel.L3U),
    ARC_L2P(ArcL2PFileType.INSTANCE, ProcessingLevel.L2P),
    CCI_L3U(CciL3FileType.INSTANCE, ProcessingLevel.L3U),
    CCI_L3C(CciL3FileType.INSTANCE, ProcessingLevel.L3C),
    CCI_L4(CciL4FileType.INSTANCE, ProcessingLevel.L4);

    private final FileType fileType;
    private final ProcessingLevel processingLevel;

    ProductType(FileType fileType, ProcessingLevel processingLevel) {
        this.fileType = fileType;
        this.processingLevel = processingLevel;
    }

    public FileType getFileType() {
        return fileType;
    }

    public Date parseDate(File file) throws ParseException {
        return getFileType().parseDate(file);
    }

    public String getDefaultFilenameRegex() {
        return getFileType().getFilenameRegex();
    }

    public GridDef getGridDef() {
        return getFileType().getGridDef();
    }

    public ProcessingLevel getProcessingLevel() {
        return processingLevel;
    }
}