import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class LSR {
    private final ArrayList<String> nodesList = new ArrayList<>();
    private int[][] graph;
    private Dijkstra dijkstra;
    private int source;

    private Stack<String> undoStack = new Stack<>();
    private Stack<String> redoStack = new Stack<>();

    LSR(File lsaPacket) throws IOException {
        lsaReader(lsaPacket);
    }

    /**
     * This method creates an empty graph.
     * @param numOfNodes the total number of nodes
     */
    private void createEmptyGraph(int numOfNodes) {
        graph = new int[numOfNodes][numOfNodes];
        for (int[] row: graph)
            // Fill -999 to the graph
            Arrays.fill(row, -999);
        for (int i = 0; i < numOfNodes; i++)
            graph[i][i]=0;
    }

    /**
     * This method creates a list of nodes from an LSA packet.
     * @param lsaPacket the lsa packet string
     */
    private void createNodesList(String lsaPacket) {
        nodesList.clear();
        String newNode;
        // Scan the String
        Scanner scanner = new Scanner(lsaPacket);

        //Read the String Line By Line
        while (scanner.hasNextLine()){
            newNode = scanner.nextLine().split("\\s+")[0];
            if (!newNode.endsWith(":"))
                throw new IllegalArgumentException ("LSA packet format error");
            newNode = newNode.split(":")[0];
            nodesList.add(newNode);
        }
        //System.out.println(nodesList);
    }

    /**
     * This method reads the graph from an .lsa file.
     * @param lsaPacket the lsa packet file
     */
    private void lsaReader(File lsaPacket) throws IOException {
        String content = Files.readString(lsaPacket.getAbsoluteFile().toPath());
        lsaReader(content);
    }

    /**
     * This method reads the graph from an LSA packet.
     * @param lsaPacket the lsa packet string
     */
    private void lsaReader(String lsaPacket){
        try{
            createNodesList(lsaPacket);
            int totalNodes = nodesList.size();

            createEmptyGraph(totalNodes);

            String strLine;
            // Scan the String
            Scanner scanner = new Scanner(lsaPacket);

            //Read the String Line By Line
            while (scanner.hasNextLine()){
                strLine = scanner.nextLine();
                //System.out.println(strLine.charAt(0));
                inputToUpdate(strLine);
            }
        }
        catch(Exception e){
            throw new IllegalArgumentException ("LSA packet format error");
        }
        //String[] nodes = nodesList.toArray(new String[0]); // Convert Arraylist to String[]
        resetDijkstra();
    }

    /**
     * This method updates existing node or adds new node (and its adjacent links) from an input line of string.
     * @param newNodeInput the node to be updated or added
     * @return {@code true} if add new node; {@code false} if only update existing node.
     */
    public boolean inputToUpdate(String newNodeInput) { //return false if only update existing node, true if add new node
        String[] input = newNodeInput.split("\\s+");
        if (!input[0].endsWith(":")) {
            throw new IllegalArgumentException("Format error");
        }
        String newNode = input[0].split(":")[0];

        if (nodesList.contains(newNode)){
            int updateNodeIndex = nodesList.indexOf(newNode);
            updateLinks(input, updateNodeIndex, graph);
            return false;
        }
        int newNodeIndex = nodesList.size();
        nodesList.add(newNode);

        int[][] newGraph = new int[nodesList.size()][nodesList.size()];
        for (int i = 0; i < this.graph.length; i++) {
            System.arraycopy(this.graph[i], 0, newGraph[i], 0, this.graph.length);
        }

        updateLinks(input, newNodeIndex, newGraph);

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
        resetDijkstra();

        return true;
    }

    /**
     * This method updates the adjacent links of an existing node in the graph from an input line of string.
     * @param input the links to be updated
     * @param updateNodeIndex the node to be updated when updating link
     * @param graph the network topology graph
     */
    private void updateLinks(String[] input, int updateNodeIndex, int[][] graph) {
        for (int x = 1; x < input.length; x++) {
            if (!input[x].contains(":")){
                throw new IllegalArgumentException("Input format error");
            }
            String adjNode = input[x].split(":")[0];
            if (adjNode.equals(getNodeName(updateNodeIndex)))
                break;
            int distance = Integer.parseInt(input[x].split(":")[1]);
            int adjNodeIndex = nodesList.indexOf(adjNode);

            graph[updateNodeIndex][adjNodeIndex] = distance;
            graph[adjNodeIndex][updateNodeIndex] = distance;

        }
    }

    /**
     * This method deletes a user selected node and all its adjacent links.
     * @param delNodeIndex the index of node that to be deleted
     */
    public void delNodes(int delNodeIndex) {
        //delete the row
        graph = removeTheElement(graph, delNodeIndex);
        //delete the column
        for (int i=0; i < graph.length; i++){
            graph[i] = removeTheElement(graph[i], delNodeIndex);
        }
        nodesList.remove(delNodeIndex);
        resetDijkstra();
    }

    /**
     * This method removes an element at specific index from an array.
     * @param arr array to be modified
     * @param index specific index of the element to be removed
     * @return modified array
     * Reference: https://www.geeksforgeeks.org/remove-an-element-at-specific-index-from-an-array-in-java/
     */
    private int[] removeTheElement(int[] arr, int index) {
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

    /**
     * This method removes a row at specific index from an 2D array.
     * @param arr array to be modified
     * @param index specific index of the element to be removed
     * @return modified array
     * Reference: https://www.geeksforgeeks.org/remove-an-element-at-specific-index-from-an-array-in-java/
     */
    private int[][] removeTheElement(int[][] arr, int index) {
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

    /**
     * This method breaks the link between two selected nodes src and dest.
     * @param src the source node index of a link that to be deleted
     * @param dest the destination node index of a link that to be deleted
     */
    public void delLink(int src, int dest){
        pushToUndo(); //push current nodes and links to undo stack first
        if (src == dest) return;
        graph[src][dest] = -999;
        graph[dest][src] = -999;
        resetDijkstra();
    }

    /**
     * This method sets the source node which is selected by user for computing the shortest path.
     * @param source the index of source node
     */
    public void setSource(int source){
        this.source = source;
        resetDijkstra();
    }

    /**
     * This method creates a new Dijkstra object with the current network topology.
     */
    private void resetDijkstra(){
        dijkstra = new Dijkstra(getNodeList(), this.graph, this.source);
    }

    /**
     * This method returns the list of nodes in string array.
     * @return the list of node in string array
     */
    public String[] getNodeList(){
        return nodesList.toArray(new String[0]);
    }

    /**
     * This method returns the name of node.
     * @param index the index of node
     * @return the name of node
     */
    public String getNodeName(int index){ return getNodeList()[index]; }

    /**
     * This method returns the computed output in compute all mode.
     * @return the computed output in compute all mode
     */
    public String computeAll(){
        resetDijkstra();
        getDijkstra().runAll();
        return getDijkstra().toString();
    }

    /**
     * This method returns a line of output in single step mode.
     * @return a line of output in single step mode
     */
    public String singleStep(){
        return getDijkstra().loop();
    }

    /**
     * This method saves a new .lsa file.
     * @param fileNameToSave the file name of new .lsa file to be saved
     */
    public void saveToLsa(File fileNameToSave){
        try {
            FileWriter myWriter = new FileWriter(fileNameToSave);
            myWriter.write(this.toString());
            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * This method pushes image of current network topology to undoStack.
     */
    public void pushToUndo() {
        //push current nodes and links to undo stack
        undoStack.push(this.toString());
        //clear the redo stack
        redoStack.clear();
    }

    /**
     * This method pushes image of network topology to undoStack.
     */
    public void pushToUndo(String image) {
        //push current nodes and links to undo stack
        undoStack.push(image);
        //clear the redo stack
        redoStack.clear();
    }

    /**
     * This method undo a topology update.
     */
    public void undo() {
        if (undoStack.isEmpty()) return;
        //push current nodes anf links to redo stack
        redoStack.push(this.toString());
        this.lsaReader(undoStack.pop());
        resetDijkstra();
    }

    /**
     * This method redo a topology update.
     */
    public void redo() {
        if (redoStack.isEmpty()) return;
        //push current nodes and links to undo stack
        undoStack.push(this.toString());
        this.lsaReader(redoStack.pop());
        resetDijkstra();
    }

    /**
     * This method returns a dijkstra object.
     * @return dijkstra object
     */
    private Dijkstra getDijkstra(){ return dijkstra; }

    /**
     * This method returns the cost of the link between 2 nodes.
     * @param src the source node index of a link
     * @param dest the destination node index of a link
     * @return the cost of the link
     */
    private int getLinkCost(int src, int dest){ return graph[src][dest]; }

    /**
     * This method returns the source node index.
     * @return the source node index
     */
    public int getSource() { return source; }

    /**
     * This method returns undo stack.
     * @return the undo stack
     */
    public Stack<String> getUndoStack() { return undoStack; }

    /**
     * This method returns the current nodes and links of the network topology in string.
     * @return the current nodes and links of the network topology
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getNodeList().length; i++) {
            sb.append(getNodeName(i)).append(":");
            for (int j = 0; j < graph[i].length; j++){
                if (getLinkCost(i,j) > 0){
                    sb.append(" ").append(getNodeName(j)).append(":").append(getLinkCost(i, j));
                }
            }
            if (i < getNodeList().length-1)
                sb.append("\n");
        }
        return sb.toString();
    }
}
