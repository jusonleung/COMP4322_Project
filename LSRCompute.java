import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Stack;

class LSRCompute {
    ArrayList<String> visited = new ArrayList<>();
    int cost_to[];
    int predecessor[];
    final int source;
    final String nodes[];
    final int graph[][];
    boolean finished = false;
    final int numberOfNodes;


    LSRCompute(String nodes[], int graph[][], int source){
        this.nodes = nodes;
        this.numberOfNodes = nodes.length;
        this.graph = graph;
        this.source = source;
        this.cost_to = new int[nodes.length];
        this.predecessor = new int[nodes.length];
    }

    private int getNumberOfNodes(){
        return numberOfNodes;
    }

    private int getSourceIndex(){
        return source;
    }

    private String getSourceName(){
        return getNodes(getSourceIndex());
    }

    private String getNodes(int index){
        return nodes[index];
    }

    private int[] getAdjacent(int index){
        return graph[index];
    }

    private int getCost_to(int index){
        return cost_to[index];
    }

    private int getPredecessor(int index){
        return predecessor[index];
    }

    public boolean isFinished(){
        return finished;
    }

    private int findMinIndexFromCost_to(){
        int min_index = 0;
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (getCost_to(i)<=0 || visited.contains(getNodes(i)))
                continue;
            if (getCost_to(i) > getCost_to(min_index))
                min_index = i;
        }
        return min_index;
    }

    public void init(){
        visited.add(getSourceName());
        cost_to = getAdjacent(getSourceIndex());
    }

    public void loop(){
        if (isFinished() == true || visited.size() == getNumberOfNodes()){
            finished = true;
            return;
        }
        int visit = findMinIndexFromCost_to();//index of current visit node
        visited.add(getNodes(visit));
        int[] adjacent = getAdjacent(visit);//adjacent paths of current visit node
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (adjacent[i] < 0) continue;
            if (getCost_to(i) < 0 || getCost_to(visit)+adjacent[i] < getCost_to(i)){
                predecessor[i] = visit;
                cost_to[i] = getCost_to(visit)+adjacent[i];
            }
        }
        return;
    }

    public int[] findPathTo(int dest){
        if (getCost_to(dest) < 0)
            return null;
        ArrayList<Integer> path = new ArrayList<>();
        path.add(getSourceIndex());
        Stack<Integer> st = new Stack();
        st.push(dest);
        int backVisit = dest;
        while (getPredecessor(backVisit) != getSourceIndex()){
            st.push(getPredecessor(backVisit));
            backVisit = getPredecessor(backVisit);
        }
        while (!st.empty()){
            path.add(st.pop());
        }
        return path.stream().mapToInt(Integer::intValue).toArray();
    }

    public void runAll(){
        init();
        while (isFinished() == false)
            loop();
    }

    public void printAllPath(){
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (i == getSourceIndex())
                continue;
            System.out.print("From "+getSourceName()+" to "+getNodes(i)+": ");
            int[] path = findPathTo(i);
            for (int j=0; j < path.length; j++){
                System.out.print(getNodes(j));
                if (j != path.length-1)
                    System.out.print(">");
            }
            System.out.println(" Cost: "+getCost_to(i));
        }
    }


    public static void main(String[] args){
        int totalNodes = 0;
        int x = 0;
        ArrayList<String> nodesList = new ArrayList<>();

        try{
            // Get line number from the file
            LineNumberReader lnr = new LineNumberReader(new FileReader(new File("routes.lsa")));
            lnr.skip(Long.MAX_VALUE);
            totalNodes = lnr.getLineNumber();
            lnr.close();
        }
        catch(Exception e){
            System.err.println("Errors: " + e.getMessage());
        }
        //System.out.println("No. of nodes: " + totalNodes);

        // Fill -999 to the arraylist
        int[][] graph = new int[totalNodes][totalNodes];
        for (int[] row: graph)
            Arrays.fill(row, -999);

        try{
            // Open the file
            FileInputStream fStream = new FileInputStream(args[0]);
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
                //System.out.println(Arrays.toString(infoOfEachLine));

                // Add corresponding cost metrics to graph[][]
                for(int j = 0; j < infoOfEachLine.length; j++) {
                    String s = String.valueOf(infoOfEachLine[j].charAt(0));
                    // Convert character (ABC...) to number (012...)
                    int n = 0;
                    for (int i = 0; i < s.length(); ++i) {
                        char ch = s.charAt(i);
                        n = (int) ch - (int) 'A';
                    }
                    graph[x][n] = Integer.parseInt(String.valueOf(infoOfEachLine[j].charAt(2)));
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

        String[] nodes = nodesList.toArray(new String[0]); // Convert Arraylist to String[]
        LSRCompute dijkstra = new LSRCompute(nodes, graph, 0);
        dijkstra.runAll();
        dijkstra.printAllPath();
    }
}
