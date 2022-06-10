import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;

public class Dijkstra {
    private final ArrayList<String> visited = new ArrayList<>();
    private int[] cost_to;
    private final int[] predecessor;
    private final int source;
    private final String[] nodesList;
    private final int[][] graph;
    private boolean finished = false;
    private boolean initialized = false;
    private final int numberOfNodes;
    private final ArrayList<String> nodesWithLink = new ArrayList<>();


    Dijkstra(String[] nodesList, int[][] graph, int source){
        this.nodesList = nodesList;
        this.numberOfNodes = nodesList.length;
        this.graph = graph;
        this.source = source;
        this.cost_to = new int[nodesList.length];
        this.predecessor = new int[nodesList.length];
        for (int i = 0; i < numberOfNodes; i++){
            for (int j : graph[i]) {
                if (j > 0) {
                    nodesWithLink.add(nodesList[i]);
                    break;
                }
            }
        }
    }

    /**
     * This method returns the total number of nodes in the graph.
     * @return the total number of nodes in the graph
     */
    private int getNumberOfNodes(){
        return numberOfNodes;
    }

    /**
     * This method returns the index of source node.
     * @return the index of source node
     */
    private int getSourceIndex(){ return source; }

    /**
     * This method returns the name of source node.
     * @return the name of source node
     */
    private String getSourceName(){
        return getNodeName(getSourceIndex());
    }

    /**
     * This method returns the name of requested node.
     * @param index the index to requested node in node list
     * @return the name of requested node
     */
    private String getNodeName(int index){
        return nodesList[index];
    }

    /**
     * This method returns the cost of adjacent path of the requested node.
     * @param index the index of requested node in graph[]
     * @return the copy of int[] of graph[index]
     */
    private int[] getAdjacent(int index){
        return graph[index].clone();
    }

    /**
     * This method returns the cost metric from source node to requested node.
     * @param index the index of requested node in cost_to[]
     * @return the cost metric
     */
    private int getCost_to(int index){
        return cost_to[index];
    }

    /**
     * This method returns the requested node's predecessor.
     * @param index the index of node in predecessor array
     * @return the predecessor node's index
     */
    private int getPredecessor(int index){
        return predecessor[index];
    }

    /**
     * This method returns the Dijkstra algo is finished or not.
     * @return {@code true} or {@code false} from finished field which is used to indicate the Dijkstra algo is finished or not.
     */
    private boolean isFinished(){ return finished; }

    /**
     * This method returns the Dijkstra algo is initialized or not.
     * @return {@code true} or {@code false} from initialized field which is used to indicate the Dijkstra algo is initialized or not.
     */
    private boolean isInitialized(){ return initialized; }

    /**
     * This method returns the index of node which has min cost in cost_to[].
     * @return the index of node which has min cost in cost_to[]
     */
    private int findMinIndexFromCost_to(){
        int min_index = -1;
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (getCost_to(i)<=0 || visited.contains(getNodeName(i)))
                continue;
            if (min_index < 0 || getCost_to(i) < getCost_to(min_index))
                min_index = i;
        }
        return min_index;
    }

    /**
     * This method initializes the Dijkstra algorithm.
     */
    private void init(){
        visited.add(getSourceName());
        cost_to = getAdjacent(getSourceIndex());
        Arrays.fill(this.predecessor, this.source);
        initialized = true;
    }

    /**
     * This method finds the shortest path and returns the output of a step.
     * @return the output of a step when finding the shortest path
     */
    public String loop(){
        if (isFinished() || visited.containsAll(nodesWithLink)){
            System.out.println("Dijkstra algorithm finished");
            finished = true;
            return this.toString();
        }

        if (!isInitialized()) init();

        int visit = findMinIndexFromCost_to();//index of current visit node
        String output = "Found "+getNodeName(visit)+": Path:"+pathToString(Objects.requireNonNull(findPathTo(visit)))+" Cost: "+getCost_to(visit);
        //System.out.println(Arrays.toString(getAdjacent(0)));
        visited.add(getNodeName(visit));
        int[] adjacent = getAdjacent(visit);//adjacent paths of current visit node
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (adjacent[i] < 0) continue;
            if (getCost_to(i) < 0 || getCost_to(visit)+adjacent[i] < getCost_to(i)){
                predecessor[i] = visit;
                cost_to[i] = getCost_to(visit)+adjacent[i];
                //System.out.println(getNodeName(i)+"'s new predecessor: "+getNodeName(visit));
            }
        }
        return output;
    }

    /**
     * This method runs loop() if isFinished is false, i.e. the Dijkstra algorithm is not finished.
     */
    public void runAll(){
        //int n = 0;
        while (!isFinished())
            //System.out.println(n++);
            loop();
    }

    /**
     * This method returns the path in string.
     * @param path the path array (containing index of nodes) to be converted to string
     * @return the path in string
     */
    private String pathToString(int[] path){
        StringBuilder result = new StringBuilder();
        for (int i=0; i < path.length; i++){
            result.append(getNodeName(path[i]));
            if (i != path.length-1)
                result.append(">");
        }
        return result.toString();
    }

    /**
     * This method finds the path to destination node.
     * @param dest the node requested to find the path
     * @return the int[] of path which contains the index of nodes in the path
     */
    private int[] findPathTo(int dest){
        Stack<Integer> st = new Stack();
        st.push(dest);
        int backVisit = dest;
        while (getPredecessor(backVisit) != getSourceIndex()){
            st.push(getPredecessor(backVisit));
            backVisit = getPredecessor(backVisit);
        }
        ArrayList<Integer> path = new ArrayList<>();
        path.add(getSourceIndex());
        while (!st.empty()){
            path.add(st.pop());
        }
        return path.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * This method returns the final result after finishing the Dijkstra algorithm in string.
     * @return the final result after finishing the Dijkstra algorithm
     */
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Source: ").append(getSourceName()).append("\n");
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (i == getSourceIndex())
                continue;
            stringBuilder.append("From ").append(getSourceName()).append(" to ").append(getNodeName(i)).append(": ");
            if (getCost_to(i) < 0) {
                stringBuilder.append("No link");
            } else {
                int[] path = findPathTo(i);
                stringBuilder.append(pathToString(path)).append(" Cost: ").append(getCost_to(i));
            }
            if (i < getNumberOfNodes()-1)
                stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

}
