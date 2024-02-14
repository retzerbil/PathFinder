//Grupp 169, Alice Wallin alwa1412, Vainius Mikelinskas vami4627, Andreas Retzius anre8319

import java.util.*;

public class ListGraph<T> implements Graph<T> {

    private final Map<T, Set<Edge<T>>> nodeMap = new HashMap<>();

    private void validateNode(T nodeOne) {
        if (!hasNode(nodeOne)) throw new NoSuchElementException(nodeOne.toString() + " node doesn't exist");
    }

    public boolean hasNode(T nodeOne) {
        return nodeMap.containsKey(nodeOne);
    }

    public boolean hasEdge(T nodeOne, T nodeTwo) {
        validateNode(nodeOne);
        validateNode(nodeTwo);

        return nodeMap.get(nodeOne).contains(nodeTwo);
    }

    public void add(T nodeOne) {
        nodeMap.putIfAbsent(nodeOne, new HashSet<>());
    }

    @Override
    public Set<T> getNodes() {
        return nodeMap.keySet();
    }

    public void remove(T nodeOne) {
        if (!nodeMap.containsKey(nodeOne)) {
            throw new NoSuchElementException(nodeOne.toString() + "Node " + nodeOne + " does not exist");
        }
        for (Edge<T> edge : nodeMap.get(nodeOne)) {
            for (Edge<T> nodeToRemove : nodeMap.get(edge.getDestination())) {
                if (nodeToRemove.getDestination().equals(nodeOne)) {
                    nodeMap.get(edge.getDestination()).remove(nodeToRemove);
                    break;
                }
            }
        }

        nodeMap.remove(nodeOne);

    }

    public void connect(T nodeOne, T nodeTwo, String name, int weight) {

        if (!hasNode(nodeOne)) {
            throw new NoSuchElementException(nodeOne.toString() + "Node " + nodeOne + " does not exist");
        } else if (!hasNode(nodeTwo)) {
            throw new NoSuchElementException(nodeOne.toString() + "Node " + nodeTwo + " does not exist");
        } else if (weight < 0) {
            throw new IllegalArgumentException();
        } else if (getEdgeBetween(nodeOne, nodeTwo) != null) {
            throw new IllegalStateException("are already connected " + nodeOne + nodeTwo);
        } else {
            Set<Edge<T>> nodeOneEdges = nodeMap.get(nodeOne);
            Set<Edge<T>> nodeTwoEdges = nodeMap.get(nodeTwo);

            nodeOneEdges.add(new Edge<T>(nodeTwo, name, weight));
            nodeTwoEdges.add(new Edge<T>(nodeOne, name, weight));
        }
    }


    public void disconnect(T nodeOne, T nodeTwo) {
        if (hasNode(nodeOne) || hasNode(nodeTwo)) {

            Edge<T> edge1 = getEdgeBetween(nodeOne, nodeTwo);
            Edge<T> edge2 = getEdgeBetween(nodeTwo, nodeOne);

            if (edge1 != null && edge2 != null) {
                this.nodeMap.get(nodeOne).remove(edge1);
                this.nodeMap.get(nodeTwo).remove(edge2);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public void setConnectionWeight(T nodeOne, T nodeTwo, int newWeight) {
        if (!nodeMap.containsKey(nodeOne) || !nodeMap.containsKey(nodeTwo)) {
            throw new NoSuchElementException();
        } else if (newWeight < 0) {
            throw new IllegalArgumentException();
        } else {
            getEdgeBetween(nodeOne, nodeTwo).setWeight(newWeight);
            getEdgeBetween(nodeTwo, nodeOne).setWeight(newWeight);
        }
    }

    @Override
    public Collection<Edge<T>> getEdgesFrom(T node) {
        if (!nodeMap.containsKey(node)) {
            throw new NoSuchElementException();
        } else {
            Collection<Edge<T>> edgeCollection = nodeMap.get(node);
            return edgeCollection;
        }
    }


    public Edge<T> getEdgeBetween(T nodeOne, T nodeTwo) {
        validateNode(nodeOne);
        validateNode(nodeTwo);

        for (Edge<T> edge : nodeMap.get(nodeOne)) {
            if (edge.getDestination().equals(nodeTwo)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (T nodeOne : nodeMap.keySet()) {
            builder.append(nodeOne.toString() + ": ");
            for (Edge<T> nodeTwo : nodeMap.get(nodeOne)) {
                builder.append(nodeTwo.toString() + " ");
            }
            builder.append("\n");
        }

        return builder.toString();
    }


    public boolean pathExists(T nodeOne, T nodeTwo) {

        return hasNode(nodeOne) && hasNode(nodeTwo) && getPath(nodeOne, nodeTwo) != null;

    }

    @Override
    public List<Edge<T>> getPath(T from, T to) {

        Map<T, T> connection = new HashMap<>();
        depthFirstConnection(from, null, connection);
        if (!connection.containsKey(to)) {
            return null;
        }

        ArrayList<Edge<T>> path = new ArrayList<>();

        T current = to;
        while (!current.equals(from)) {
            T next = connection.get(current);
            Edge<T> edge = getEdgeBetween(next, current);
            path.add(edge);
            current = next;
        }

        Collections.reverse(path);

        return Collections.unmodifiableList(path);
    }

    private void depthFirstConnection(T to, T from, Map<T, T> connection) {
        connection.put(to, from);
        for (Edge<T> edge : nodeMap.get(to)) {
            if (!connection.containsKey(edge.getDestination())) {
                depthFirstConnection(edge.getDestination(), to, connection);
            }
        }

    }

    private void depthFirstVisitAll(T current, Set<T> visited) {
        visited.add(current);
        for (Edge<T> edge : nodeMap.get(current)) {
            if (!visited.contains(edge.getDestination())) {
                depthFirstVisitAll(edge.getDestination(), visited);
            }
        }
    }


}
