import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Dijkstra {
    ArrayList<String> visited = new ArrayList<>();
    int cost_to[];
    int predecessor[];
    final int source;
    final String nodesList[];
    final int graph[][];
    boolean finished = false;
    final int numberOfNodes;


    Dijkstra(String nodesList[], int graph[][], int source){
        this.nodesList = nodesList;
        this.numberOfNodes = nodesList.length;
        this.graph = graph;
        this.source = source;
        this.cost_to = new int[nodesList.length];
        this.predecessor = new int[nodesList.length];
    }

    private int getNumberOfNodes(){
        return numberOfNodes;
    }

    private int getSourceIndex(){
        return source;
    }

    private String getSourceName(){
        return getNodeName(getSourceIndex());
    }

    private String getNodeName(int index){
        return nodesList[index];
    }

    private int[] getAdjacent(int index){
        return graph[index].clone();
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
        int min_index = -1;
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (getCost_to(i)<=0 || visited.contains(getNodeName(i)))
                continue;
            if (min_index < 0 || getCost_to(i) < getCost_to(min_index))
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
        //System.out.println(Arrays.toString(cost_to));
        System.out.println("Found "+getNodeName(visit)+": Path:"+pathToString(findPathTo(visit))+" Cost: "+getCost_to(visit));
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
        return;
    }

    public void runAll(){
        init();
        while (isFinished() == false)
            loop();
    }
    public String pathToString(int[] path){
        String result = "";
        for (int i=0; i < path.length; i++){
            result += getNodeName(path[i]);
            if (i != path.length-1)
                result +=">";
        }
        return result;
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



    public void printAllPath(){
        for(int i = 0; i < getNumberOfNodes(); i++){
            if (i == getSourceIndex())
                continue;
            System.out.print("From "+getSourceName()+" to "+getNodeName(i)+": ");
            int[] path = findPathTo(i);
            System.out.println(pathToString(path)+" Cost: "+getCost_to(i));
        }
    }
}
