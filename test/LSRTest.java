import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class LSRTest {

    File testPacket = new File("routes_test.lsa");
    /*
    A: B:5 D:5 C:3
    B: A:5 F:2 C:4 E:3
    F: B:2 E:5
    D: A:5 C:1 E:3
    C: A:3 B:4 D:1 E:6
    E: B:3 F:5 D:3 C:6
    */

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void test_LsaReader() throws IOException {
        File file = new File("testing2.lsa");
        LSR lsr = new LSR(file);
        String expected =
                """
                A: B:1 C:10
                B: A:1 C:3 D:12
                C: A:10 B:3 D:12
                D: B:12 C:12""";
        assertEquals(lsr.toString(), expected);
    }

    @Test
    public void inputToUpdate() throws IOException {
        LSR lsr = new LSR(testPacket);
        assertTrue(lsr.inputToUpdate("G:  A:3 F:4"));
        String expected =
                """
                A: B:5 D:5 C:3 G:3
                B: A:5 F:2 C:4 E:3
                F: B:2 E:5 G:4
                D: A:5 C:1 E:3
                C: A:3 B:4 D:1 E:6
                E: B:3 F:5 D:3 C:6
                G: A:3 F:4""";
        assertEquals(lsr.toString(), expected);
    }

    @Test
    public void test_LsaReader_error1() throws IOException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("LSA packet format error");
        File file = new File("testing3.lsa");
        LSR lsr = new LSR(file);
    }

    @Test
    public void test_LsaReader_error2() throws IOException {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("LSA packet format error");
        File file = new File("testing4.lsa");
        LSR lsr = new LSR(file);
    }

    @Test
    public void test_inputToUpdate_error() throws IOException {
        LSR lsr = new LSR(testPacket);
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Format error");
        lsr.inputToUpdate("G;  A:3 F:4");
    }

    @Test
    public void delNodes() throws IOException {
        LSR lsr = new LSR(testPacket);
        lsr.delNodes(3); //delete node D
        String expected =
                """
                A: B:5 C:3
                B: A:5 F:2 C:4 E:3
                F: B:2 E:5
                C: A:3 B:4 E:6
                E: B:3 F:5 C:6""";
        assertEquals(lsr.toString(), expected);
    }

    @Test
    public void delLink() throws IOException {
        LSR lsr = new LSR(testPacket);
        lsr.delLink(1,2); //delete link between B and F
        String expected =
                """
                A: B:5 D:5 C:3
                B: A:5 C:4 E:3
                F: E:5
                D: A:5 C:1 E:3
                C: A:3 B:4 D:1 E:6
                E: B:3 F:5 D:3 C:6""";
        assertEquals(lsr.toString(), expected);
    }

    @Test
    public void setSource() throws IOException {
        LSR lsr = new LSR(testPacket);
        assertEquals(lsr.getSource(),0);
        lsr.setSource(4);
        assertEquals(lsr.getSource(),4);
    }

    @Test
    public void getNodeList() throws IOException {
        LSR lsr = new LSR(testPacket);
        assertArrayEquals(lsr.getNodeList(), new String[]{"A","B","F","D","C","E"});
    }

    @Test
    public void computeAll() throws IOException {
        LSR lsr = new LSR(testPacket);
        lsr.setSource(2);
        String expected =
                """
                Source: F
                From F to A: F>B>A Cost: 7
                From F to B: F>B Cost: 2
                From F to D: F>B>C>D Cost: 7
                From F to C: F>B>C Cost: 6
                From F to E: F>E Cost: 5""";
        assertEquals(lsr.computeAll(), expected);
    }

    @Test
    public void singleStep() throws IOException {
        LSR lsr = new LSR(testPacket);
        lsr.setSource(3);
        assertEquals(lsr.singleStep(), "Found C: Path:D>C Cost: 1");
        assertEquals(lsr.singleStep(), "Found E: Path:D>E Cost: 3");
    }

    @Test
    public void saveToLsa() throws IOException {
        LSR lsr = new LSR(testPacket);
        File fileToSave = new File("routes_testResult.lsa");
        lsr.saveToLsa(fileToSave);
    }

    @Test
    public void pushToUndo() throws IOException {
        LSR lsr = new LSR(testPacket);
        String image = lsr.toString();
        lsr.pushToUndo();
        assertEquals(lsr.getUndoStack().peek(), image);
    }

    @Test
    public void undo() throws IOException {
        LSR lsr = new LSR(testPacket);
        String expected = lsr.toString();
        lsr.pushToUndo();
        lsr.delNodes(2);
        lsr.undo();
        assertEquals(lsr.toString(), expected);
    }

    @Test
    public void redo() throws IOException {
        LSR lsr = new LSR(testPacket);
        lsr.pushToUndo();
        lsr.delNodes(2);
        lsr.undo();
        lsr.redo();
        String expected =
                """
                A: B:5 D:5 C:3
                B: A:5 C:4 E:3
                D: A:5 C:1 E:3
                C: A:3 B:4 D:1 E:6
                E: B:3 D:3 C:6""";
        assertEquals(lsr.toString(), expected);
    }

    @Test
    public void testPushToUndo() throws IOException {
        LSR lsr = new LSR(testPacket);
        String image = lsr.toString();
        lsr.pushToUndo(image);
        assertEquals(lsr.getUndoStack().peek(), lsr.toString());
    }
}