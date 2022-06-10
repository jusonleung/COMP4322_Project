import org.junit.Test;

import static org.junit.Assert.*;

public class DijkstraTest {

    String[] nodeList = {"A","B","F","D","C","E"};
    int[][] graph = {
            {0,5,-999,5,3,-999},
            {5,0,2,-999,4,3},
            {-999,2,0,-999,-999,5},
            {5,-999,-999,0,1,3},
            {3,4,-999,1,0,6},
            {-999,3,5,3,6,0}
    };

    @Test
    public void loop() {
        int src = 1;
        Dijkstra dijkstra = new Dijkstra(nodeList, graph, src);
        assertEquals(dijkstra.loop(), "Found F: Path:B>F Cost: 2");
    }

    @Test
    public void runAll() {
        int src = 2;
        Dijkstra dijkstra = new Dijkstra(nodeList, graph, src);
        dijkstra.runAll();
        assertEquals(dijkstra.toString(),
                """
                Source: F
                From F to A: F>B>A Cost: 7
                From F to B: F>B Cost: 2
                From F to D: F>B>C>D Cost: 7
                From F to C: F>B>C Cost: 6
                From F to E: F>E Cost: 5""");
    }
}