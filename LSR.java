import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class LSR {
    ArrayList<String> nodesList = new ArrayList<>();
    int[][] graph;

    public void lsaReader(String fileName){
        int totalNodes = 0;
        int x = 0;

        try{
            // Get line number from the file
            LineNumberReader lnr = new LineNumberReader(new FileReader(fileName));
            lnr.skip(Long.MAX_VALUE);
            totalNodes = lnr.getLineNumber();
            lnr.close();
        }
        catch(Exception e){
            System.err.println("Errors: " + e.getMessage());
        }
        //System.out.println("No. of nodes: " + totalNodes);

        // Fill -999 to the arraylist
        graph = new int[totalNodes][totalNodes];
        for (int[] row: graph)
            Arrays.fill(row, -999);
        for (int i = 0; i < totalNodes; i++)
            graph[i][i]=0;
        try{
            // Open the file
            FileInputStream fStream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fStream));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null){

                //System.out.println(strLine.charAt(0));
                nodesList.add(String.valueOf(strLine.charAt(0))); // Add the first character in a line to arraylist
                System.out.println(nodesList);

                strLine = strLine.substring(strLine.indexOf(":") + 2); // Remove node identifier of each line
                //System.out.println(strLine);

                // split by spaces and store in infoOfEachLine
                String[] infoOfEachLine = strLine.split("\\s+");
                System.out.println(Arrays.toString(infoOfEachLine));

                // Add corresponding cost metrics to graph[][]
                for(int j = 0; j < infoOfEachLine.length; j++) {
                    String s = String.valueOf(infoOfEachLine[j].charAt(0));
                    // Convert character (ABC...) to number (012...)
                    int n = 0;
                    for (int i = 0; i < s.length(); ++i) {
                        char ch = s.charAt(i);
                        n = (int) ch - (int) 'A';
                    }
                    String substring = infoOfEachLine[j].substring(infoOfEachLine[j].indexOf(":") + 1); // Remove node identifier before the cost
                    //System.out.println(substring);
                    graph[x][n] = Integer.parseInt(substring);
                }
                x++;

                // For testing only
                System.out.println(Arrays.deepToString(graph));
                System.out.println();
            }
            //Close the input stream
            br.close();
        }
        catch(Exception e){
            System.err.println("Errors: " + e.getMessage());
        }
        //String[] nodes = nodesList.toArray(new String[0]); // Convert Arraylist to String[]
    }

    public void addNodes(String newNodeInput) {
        int newNodeIndex = nodesList.size();
        String[] input = newNodeInput.split(" ");
        String NewNode = input[0].substring(0,input[0].length()-1);
        nodesList.add(NewNode);

        int[][] newGraph = new int[nodesList.size()][nodesList.size()];
        for (int i = 0; i < this.graph.length; i++) {
            for (int j = 0; j < this.graph.length; j++) {
                newGraph[i][j] = this.graph[i][j];
            }
        }


        for (int x = 1; x < input.length; x++) {
            String adjNode = input[x].split(":")[0];
            int distance = Integer.parseInt(input[x].split(":")[1]);
            int adjNodeIndex = nodesList.indexOf(adjNode);

            newGraph[newNodeIndex][adjNodeIndex] = distance;
            newGraph[adjNodeIndex][newNodeIndex] = distance;

        }

        for (int i = 0; i < newGraph.length-1; i++) {
            if (newGraph[i][newNodeIndex] == 0) {
                newGraph[i][newNodeIndex] = -999;
            }
        }
        for (int j = 0; j < newGraph[newNodeIndex].length-1; j++) {
            if (newGraph[newNodeIndex][j] == 0) {
                newGraph[newNodeIndex][j] = -999;
            }
        }
        this.graph = newGraph;
    }

    public void delNodes(String delNodeInput) {
        int delNodeIndex = nodesList.indexOf(delNodeInput);
        graph = removeTheElement(graph, delNodeIndex);
        for (int i=0; i < graph.length; i++){
            graph[i] = removeTheElement(graph[i], delNodeIndex);
        }
        nodesList.remove(delNodeIndex);
    }

    public int[] removeTheElement(int[] arr, int index)
    {
        // If the array is empty
        // or the index is not in array range
        // return the original array
        if (arr == null || index < 0 || index >= arr.length) {
            return arr;
        }

        // Create another array of size one less
        int[] anotherArray = new int[arr.length - 1];

        // Copy the elements from starting till index
        // from original array to the other array
        System.arraycopy(arr, 0, anotherArray, 0, index);

        // Copy the elements from index + 1 till end
        // from original array to the other array
        System.arraycopy(arr, index + 1, anotherArray, index,arr.length - index - 1);

        // return the resultant array
        return anotherArray;
    }

    public int[][] removeTheElement(int[][] arr, int index)
    {
        // If the array is empty
        // or the index is not in array range
        // return the original array
        if (arr == null || index < 0 || index >= arr.length) {
            return arr;
        }

        // Create another array of size one less
        int[][] anotherArray = new int[arr.length - 1][];

        // Copy the elements from starting till index
        // from original array to the other array
        System.arraycopy(arr, 0, anotherArray, 0, index);

        // Copy the elements from index + 1 till end
        // from original array to the other array
        System.arraycopy(arr, index + 1, anotherArray, index,arr.length - index - 1);

        // return the resultant array
        return anotherArray;
    }

    public void delLink(String delLinkInput){
        String[] delLinkNodes = delLinkInput.split(">");
        int delLinkIndex1 = nodesList.indexOf(delLinkNodes[0]);
        int delLinkIndex2 = nodesList.indexOf(delLinkNodes[1]);

        graph[delLinkIndex1][delLinkIndex2] = -999;
        graph[delLinkIndex2][delLinkIndex1] = -999;
    }

    public String[] getNodeList(){
        return nodesList.toArray(new String[0]);
    }

    @Override
    public String toString() {
        String lineSeparator = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        for (int[] row : this.graph) {
            sb.append(Arrays.toString(row)).append(lineSeparator);
        }
        return "graph=\n" + sb + "nodes=" + nodesList.toString() + '}';
    }

    public static void main(String[] args){
        LSR lsr = new LSR();
        lsr.lsaReader("routes.lsa");
        Dijkstra dijkstra = new Dijkstra(lsr.getNodeList(), lsr.graph, 0);

        dijkstra.runAll();
        dijkstra.printAllPath();

        lsr.addNodes("H: C:6 F:9 E:2");
        System.out.println(lsr);
        lsr.delNodes("D");
        System.out.println(lsr);
        lsr.delLink("C>E");
        System.out.println(lsr);
    }
}
