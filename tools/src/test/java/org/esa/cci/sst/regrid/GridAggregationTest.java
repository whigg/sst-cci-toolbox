package org.esa.cci.sst.regrid;

import org.esa.cci.sst.util.ArrayGrid;
import org.esa.cci.sst.util.GridDef;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * @author Bettina Scholze
 *         Date: 27.07.12 09:02
 */
public class GridAggregationTest {

    private MeanCalculator meanCalculator = new MeanCalculator();

    @Test
    public void testAggregateData_increasing() throws Exception {
        GridDef sourceGridDef = GridDef.createGlobal(15.0).setTime(1);
        int[] sourceShape = {1, sourceGridDef.getHeight(), sourceGridDef.getWidth()};
        short[] data = createData_increasingPerRow(sourceGridDef);
        assertEquals(288, data.length);
//        printShortArray(sourceGridDef, data);
        Array sourceArray = Array.factory(DataType.SHORT, sourceShape, data);
        ArrayGrid sourceArrayGrid = new ArrayGrid(sourceGridDef, sourceArray, Short.MIN_VALUE, 1.0, 0).setVariable("A");

        GridDef targetGridDef = GridDef.createGlobal(90.0).setTime(1);

        Map ignored = null;
        GridAggregation gridAggregation = new GridAggregation(ignored, ignored, meanCalculator);
        double[] sourceDoubles = gridAggregation.fetchDataAsScaledDoubles(sourceArrayGrid); //tested
//        printDoubleArray(sourceGridDef, sourceDoubles);

        //execution
        double[] targetDoubles = gridAggregation.aggregateData(sourceDoubles, sourceGridDef, targetGridDef);

        assertEquals(288, sourceDoubles.length);
        assertEquals(8, targetDoubles.length);
        double[] expected = {3.5, 3.5, 3.5, 3.5, 9.5, 9.5, 9.5, 9.5};
        for (int i = 0; i < expected.length; i++) {
            assertEquals("i: " + i, expected[i], targetDoubles[i]);
        }
    }

    @Test
    public void testAggregateData_alternating() throws Exception {
        GridDef sourceGridDef = GridDef.createGlobal(15.0).setTime(1);
        int[] sourceShape = {1, sourceGridDef.getHeight(), sourceGridDef.getWidth()};
        short[] data = createData_alternatingPerRow(sourceGridDef);
        assertEquals(288, data.length);
//        printShortArray(sourceGridDef, data);
        Array sourceArray = Array.factory(DataType.SHORT, sourceShape, data);
        ArrayGrid sourceArrayGrid = new ArrayGrid(sourceGridDef, sourceArray, Short.MIN_VALUE, 1.0, 0).setVariable("A");

        GridDef targetGridDef = GridDef.createGlobal(90.0).setTime(1);

        Map ignored = null;
        GridAggregation gridAggregation = new GridAggregation(ignored, ignored, meanCalculator);
        double[] sourceDoubles = gridAggregation.fetchDataAsScaledDoubles(sourceArrayGrid); //tested

//        printDoubleArray(sourceGridDef, sourceDoubles);
        //execution
        double[] targetDoubles = gridAggregation.aggregateData(sourceDoubles, sourceGridDef, targetGridDef);

        assertEquals(288, sourceDoubles.length);
        assertEquals(8, targetDoubles.length);
        double[] expected = {2.5, 2.5, 2.5, 2.5, 2.5, 2.5, 2.5, 2.5};
        for (int i = 0; i < expected.length; i++) {
            assertEquals("i: " + i, expected[i], targetDoubles[i]);
        }
    }

    @Test
    public void testAggregateGrids() throws Exception {
        HashMap<String, ArrayGrid> sourceGrids = new HashMap<String, ArrayGrid>();
        HashMap<String, ArrayGrid> targetGrids = new HashMap<String, ArrayGrid>();

        GridDef sourceGridDef = GridDef.createGlobal(15.0).setTime(1);
        GridDef targetGridDef = GridDef.createGlobal(90.0).setTime(1);

        int[] sourceShape = {1, sourceGridDef.getHeight(), sourceGridDef.getWidth()};
        int[] targetShape = {1, targetGridDef.getHeight(), targetGridDef.getWidth()};

        short[] data_1 = createData_increasingPerRow(sourceGridDef);
        short[] data_2 = createData_alternatingPerRow(sourceGridDef);

        Array sourceArray_1 = Array.factory(DataType.SHORT, sourceShape, data_1);
        Array sourceArray_2 = Array.factory(DataType.SHORT, sourceShape, data_2);
        Array targetArray_1 = Array.factory(DataType.SHORT, targetShape);
        Array targetArray_2 = Array.factory(DataType.SHORT, targetShape);

        ArrayGrid sourceArrayGrid_1 = new ArrayGrid(sourceGridDef, sourceArray_1, Short.MIN_VALUE, 2.0, 0).setVariable("A");
        ArrayGrid sourceArrayGrid_2 = new ArrayGrid(sourceGridDef, sourceArray_2, Short.MIN_VALUE, 0.1, 0).setVariable("B");
        ArrayGrid targetArrayGrid_1 = new ArrayGrid(targetGridDef, targetArray_1, Short.MIN_VALUE, 2.0, 0).setVariable("A");
        ArrayGrid targetArrayGrid_2 = new ArrayGrid(targetGridDef, targetArray_2, Short.MIN_VALUE, 0.1, 0).setVariable("B");

        sourceGrids.put("A", sourceArrayGrid_1);
        sourceGrids.put("B", sourceArrayGrid_2);
        targetGrids.put("A", targetArrayGrid_1);
        targetGrids.put("B", targetArrayGrid_2);
//        printShortArray(sourceGridDef, (short[]) sourceGrids.get("A").getArray().copyTo1DJavaArray());
//        printShortArray(sourceGridDef, (short[]) sourceGrids.get("B").getArray().copyTo1DJavaArray());

        GridAggregation gridAggregation = new GridAggregation(sourceGrids, targetGrids, meanCalculator);

        //execution
        gridAggregation.aggregateGrids();

        //verification
        assertArrayEquals(new int[]{1, 12, 24}, sourceShape);
        assertArrayEquals(new int[]{1, 2, 4}, targetShape);

//        targetGrids -> have valid data now
        final Object objA = targetArray_1.copyToNDJavaArray();
        assertTrue(objA instanceof short[][][]);
        short[][][] dataA = (short[][][]) objA;
        short[][] latRowsA = dataA[0];
        short[] longCellsA = dataA[0][0];
        assertEquals(1, dataA.length);
        assertEquals(2, latRowsA.length);
        assertEquals(4, longCellsA.length);
        assertEquals("[4, 4, 4, 4]", Arrays.toString(latRowsA[0]));
        assertEquals("[10, 10, 10, 10]", Arrays.toString(latRowsA[1]));

        final Object objB = targetArray_2.copyToNDJavaArray();
        assertTrue(objB instanceof short[][][]);
        short[][][] dataB = (short[][][]) objB;
        short[][] latRowsB = dataB[0];
        short[] longCellsB = dataB[0][0];
        assertEquals(1, dataA.length);
        assertEquals(2, latRowsB.length);
        assertEquals(4, longCellsB.length);
        assertEquals("[3, 3, 3, 3]", Arrays.toString(latRowsB[0]));
        assertEquals("[3, 3, 3, 3]", Arrays.toString(latRowsB[1]));

    }

    private static short[] createData_increasingPerRow(GridDef sourceGridDef) {
        assertEquals(1, sourceGridDef.getTime());
        int size = sourceGridDef.getHeight() * sourceGridDef.getWidth();
        short[] data = new short[size];

        short value = 0;
        for (int i = 0; i < size; i++) {
            int innerDimPosition = (i) % sourceGridDef.getWidth();
            if (innerDimPosition == 0) {
                value++;
            }
            data[i] = value;
        }
        return data;
    }

    private static byte[] createData_increasingPerRow_byte(GridDef sourceGridDef) {
        assertEquals(1, sourceGridDef.getTime());
        int size = sourceGridDef.getHeight() * sourceGridDef.getWidth();
        byte[] bytes = new byte[size];

        short value = 0;
        for (int i = 0; i < size; i++) {
            int innerDimPosition = i % sourceGridDef.getWidth();
            if (innerDimPosition == 0) {
                value++;
            }
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    private static int[] createData_increasingPerRow_int(GridDef sourceGridDef) {
        assertEquals(1, sourceGridDef.getTime());
        int size = sourceGridDef.getHeight() * sourceGridDef.getWidth();
        int[] ints = new int[size];

        short value = 0;
        for (int i = 0; i < size; i++) {
            int innerDimPosition = i % sourceGridDef.getWidth();
            if (innerDimPosition == 0) {
                value++;
            }
            ints[i] = value;
        }
        return ints;
    }

    private static short[] createData_alternatingPerRow(GridDef sourceGridDef) {
        assertEquals(1, sourceGridDef.getTime());
        int size = sourceGridDef.getHeight() * sourceGridDef.getWidth();
        short[] shorts = new short[size];

        short value = 0;
        for (int i = 0; i < size; i++) {
            int innerDimPosition = i % sourceGridDef.getWidth();
            int outerDimPosition = i / sourceGridDef.getWidth();
            if (innerDimPosition == 0) {
                int remain = outerDimPosition % 2;
                if (remain == 0) {
                    value = 2;
                } else { //1
                    value = 3;
                }
            }

            shorts[i] = value;
        }
        return shorts;
    }

    @Test
    public void testFetchDataAsScaledDouble_short() throws Exception {
        GridDef sourceGridDef = GridDef.createGlobal(90.0).setTime(1);
        int[] sourceShape = {1, sourceGridDef.getHeight(), sourceGridDef.getWidth()};
        short[] data = new short[]{1, 1, 1, 1, 2, 2, 2, 2};
        Array sourceArray = Array.factory(DataType.SHORT, sourceShape, data);
        ArrayGrid sourceArrayGrid = new ArrayGrid(sourceGridDef, sourceArray, 0, 4.0, 0).setVariable("A");
        Map ignored = null;
        GridAggregation gridAggregation = new GridAggregation(ignored, ignored, null);

        //execution
        double[] dataAsScaledDouble = gridAggregation.fetchDataAsScaledDoubles(sourceArrayGrid);

        double[] expected = {4.0, 4.0, 4.0, 4.0, 8.0, 8.0, 8.0, 8.0};
        for (int i = 0; i < dataAsScaledDouble.length; i++) {
            assertTrue(expected[i] == dataAsScaledDouble[i]);
        }
    }

    @Test
    public void testFetchDataAsScaledDouble_int() throws Exception {
        GridDef sourceGridDef = GridDef.createGlobal(90.0).setTime(1);
        int[] sourceShape = {1, sourceGridDef.getHeight(), sourceGridDef.getWidth()};
        int[] data = new int[]{1, 1, 1, 1, 2, 2, 2, 2};
        Array sourceArray = Array.factory(DataType.INT, sourceShape, data);
        ArrayGrid sourceArrayGrid = new ArrayGrid(sourceGridDef, sourceArray, 0, 2.0, 0).setVariable("A");
        Map ignored = null;
        GridAggregation gridAggregation = new GridAggregation(ignored, ignored, null);

        //execution
        double[] dataAsScaledDouble = gridAggregation.fetchDataAsScaledDoubles(sourceArrayGrid);

        double[] expected = {2.0, 2.0, 2.0, 2.0, 4.0, 4.0, 4.0, 4.0};
        for (int i = 0; i < dataAsScaledDouble.length; i++) {
            assertTrue(expected[i] == dataAsScaledDouble[i]);
        }
    }

    @Test
    public void testFetchDataAsScaledDouble_byte() throws Exception {
        GridDef sourceGridDef = GridDef.createGlobal(90.0).setTime(1);
        int[] sourceShape = {1, sourceGridDef.getHeight(), sourceGridDef.getWidth()};
        byte[] data = new byte[]{1, 1, 1, 1, 2, 2, 2, 2};
        Array sourceArray = Array.factory(DataType.BYTE, sourceShape, data);
        ArrayGrid sourceArrayGrid = new ArrayGrid(sourceGridDef, sourceArray, 0, 0.1, 0).setVariable("A");
        Map ignored = null;
        GridAggregation gridAggregation = new GridAggregation(ignored, ignored, null);

        //execution
        double[] dataAsScaledDouble = gridAggregation.fetchDataAsScaledDoubles(sourceArrayGrid);

        double[] expected = {0.1, 0.1, 0.1, 0.1, 0.2, 0.2, 0.2, 0.2};
        for (int i = 0; i < dataAsScaledDouble.length; i++) {
            assertTrue(expected[i] == dataAsScaledDouble[i]);
        }
    }

    @Test
    public void testFetchDataAsScaledDouble_notSupportedType() throws Exception {
        GridDef sourceGridDef = GridDef.createGlobal(90.0).setTime(1);
        int[] sourceShape = {1, sourceGridDef.getHeight(), sourceGridDef.getWidth()};
        char[] data = new char[]{'e'};
        Array sourceArray = Array.factory(DataType.CHAR, sourceShape, data);
        ArrayGrid sourceArrayGrid = new ArrayGrid(sourceGridDef, sourceArray, 0, 1, 0).setVariable("A");
        Map ignored = null;
        GridAggregation gridAggregation = new GridAggregation(ignored, ignored, null);

        //execution
        try {
            gridAggregation.fetchDataAsScaledDoubles(sourceArrayGrid);
            fail("Exception expected.");
        } catch (Exception expected) {
            assertEquals("byte[], short[] or int[] expected, but found char[].", expected.getMessage());
        }

    }

    private static void printShortArray(GridDef sourceGridDef, short[] sourceDoubles) {
        System.out.println("\n");
        for (int h = 0; h < sourceGridDef.getHeight(); h++) {
            int offset = h * sourceGridDef.getWidth();
            for (int i = 0; i < sourceGridDef.getWidth(); i++) {
                System.out.print(sourceDoubles[offset + i] + " ");
            }
            System.out.println();

        }
    }

    private static void printDoubleArray(GridDef sourceGridDef, double[] sourceDoubles) {
        System.out.println("\n");
        for (int h = 0; h < sourceGridDef.getHeight(); h++) {
            int offset = h * sourceGridDef.getWidth();
            for (int i = 0; i < sourceGridDef.getWidth(); i++) {
                System.out.print(sourceDoubles[offset + i] + " ");
            }
            System.out.println();

        }
    }
}