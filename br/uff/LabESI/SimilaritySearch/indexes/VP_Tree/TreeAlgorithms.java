package br.uff.LabESI.SimilaritySearch.indexes.VP_Tree;

import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.commons.Commons;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@NoArgsConstructor
public class TreeAlgorithms<T> {

    private int threadPoolSize = 1;
    private ExecutorService threadPOOL;
    private final AtomicLong openedElements = new AtomicLong();

    public TreeAlgorithms(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public List<Node<T>> getAllLeafNodes(List<Node<T>> rootNodes) {
        return getAllLeafNodes(rootNodes.toArray(new Node[rootNodes.size()]));
    }

    public List<Node<T>> getAllParentsOfLeafNodes(List<Node<T>> rootNodes) {
        return getAllParentsOfLeafNodes(rootNodes.toArray(new Node[rootNodes.size()]));
    }

    public Map<Node<T>, Double> getMapOfLeafNodes(List<Node<T>> rootNodes, T target, DistanceFunction<T> df) {
        return getMapOfLeafNodes(target, df, rootNodes.toArray(new Node[rootNodes.size()]));
    }

    public List<Node<T>> getAllLeafNodes(Node<T>... rootNodes) {
        ConcurrentLinkedQueue<Node<T>> result = new ConcurrentLinkedQueue<>();
        threadPOOL = Executors.newFixedThreadPool(threadPoolSize);
        openedElements.incrementAndGet();
        for (Node<T> rootNode : rootNodes) {
            getAllLeafNodes(rootNode, result);
        }
        waitJobsFinish();
        threadPOOL.shutdown();
        return new ArrayList<>(result);
    }

    public List<Node<T>> getAllParentsOfLeafNodes(Node<T>... rootNodes) {
        ConcurrentLinkedQueue<Node<T>> result = new ConcurrentLinkedQueue<>();
        threadPOOL = Executors.newFixedThreadPool(threadPoolSize);
        openedElements.incrementAndGet();
        for (Node<T> rootNode : rootNodes) {
            getAllParentsOfLeafNodes(rootNode, result);
        }
        waitJobsFinish();
        threadPOOL.shutdown();
        return new ArrayList<>(result);
    }

    public Map<Node<T>, Double> getMapOfLeafNodes(T target, DistanceFunction<T> df, Node<T>... rootNodes) {
        ConcurrentHashMap<Node<T>, Double> result = new ConcurrentHashMap<>();
        threadPOOL = Executors.newFixedThreadPool(threadPoolSize);
        openedElements.incrementAndGet();
        for (Node<T> rootNode : rootNodes) {
            getMapOfLeafNodes(rootNode, result, target, df);
        }
        waitJobsFinish();
        threadPOOL.shutdown();
        return new HashMap<>(result);
    }

    public void checkAndMergeTree(List<Node<T>> rootNodes) {
        checkAndMergeTree(rootNodes.toArray(new Node[rootNodes.size()]));
    }

    public boolean checkAndMergeTree(Node<T>... rootNodes) {
        boolean teveMerge = false;
        threadPOOL = Executors.newFixedThreadPool(threadPoolSize);
        for (Node<T> rootNode : rootNodes) {
            if (rootNode.isLeafNode()) continue;

            int alturaDaArvore = alturaDaArvore(rootNode);
            int alturaMinimaDaArvore = alturaMinimaDaArvore(rootNode);

            if (alturaDaArvore != alturaMinimaDaArvore) {
                teveMerge = true;
                System.out.println("Fazendo merge da arvore: alturaDaArvore: " + alturaDaArvore + ", alturaMinimaDaArvore: " + alturaMinimaDaArvore);
                findLevelAndMerge(alturaMinimaDaArvore, rootNode);
            }

        }
        waitJobsFinish();
        threadPOOL.shutdown();
        return teveMerge;
    }

    private void findLevelAndMerge(int level, Node<T> rootNode) {
        openedElements.incrementAndGet();
        findLevelAndMerge(0, level, rootNode);
    }

    private void findLevelAndMerge(int level, int targetLevel, Node<T> node) {
        if (!node.isLeafNode()) {
            if (level < targetLevel) {
                openedElements.addAndGet(2);
                threadPOOL.execute(() -> findLevelAndMerge(level + 1, targetLevel, node.getRightNode()));
                threadPOOL.execute(() -> findLevelAndMerge(level + 1, targetLevel, node.getLeftNode()));
            } else {
                mergeToNode(node.getLeftNode(), node.getFile());
                node.setLeftNode(null);
                mergeToNode(node.getRightNode(), node.getFile());
                node.setRightNode(null);
            }
        }
        openedElements.decrementAndGet();
    }

    private void mergeToNode(Node<T> targetNode, File targetFile) {
        if (!targetNode.isLeafNode()) {
            mergeToNode(targetNode.getLeftNode(), targetFile);
            targetNode.setLeftNode(null);
            mergeToNode(targetNode.getRightNode(), targetFile);
            targetNode.setRightNode(null);
            return;
        }
        Commons.mergeFileIntoAnother(targetNode.getFile(), targetFile);
        targetNode.getFile().delete();
    }

    private void getAllLeafNodes(Node<T> node, ConcurrentLinkedQueue result) {
        if (node.isLeafNode()) {
            result.add(node);
        } else {
            openedElements.addAndGet(2);
            threadPOOL.execute(() -> getAllLeafNodes(node.getLeftNode(), result));
            threadPOOL.execute(() -> getAllLeafNodes(node.getRightNode(), result));
        }
        openedElements.decrementAndGet();
    }

    private void getAllParentsOfLeafNodes(Node<T> node, ConcurrentLinkedQueue result) {
        if (node.getLeftNode().isLeafNode() || node.getRightNode().isLeafNode()) {
            result.add(node);
        } else {
            openedElements.addAndGet(2);
            threadPOOL.execute(() -> getAllParentsOfLeafNodes(node.getLeftNode(), result));
            threadPOOL.execute(() -> getAllParentsOfLeafNodes(node.getRightNode(), result));
        }
        openedElements.decrementAndGet();
    }

    private void getMapOfLeafNodes(Node<T> node, ConcurrentHashMap result, T target, DistanceFunction<T> df) {
        if (node.getLeftNode().isLeafNode() || node.getRightNode().isLeafNode()) {
            result.put(node, df.getDistance(node.getVantagePoint(), target));
        } else {
            openedElements.addAndGet(2);
            threadPOOL.execute(() -> getMapOfLeafNodes(node.getLeftNode(), result, target, df));
            threadPOOL.execute(() -> getMapOfLeafNodes(node.getRightNode(), result, target, df));
        }
        openedElements.decrementAndGet();
    }

    public static <T> int alturaMinimaDaArvore(Node<T> node) {
        if (node.isLeafNode()) return 0;
        int left = alturaMinimaDaArvore(node.getLeftNode());
        int right = alturaMinimaDaArvore(node.getRightNode());
        return Math.min(left, right) + 1;
    }

    public static <T> int alturaDaArvore(Node<T> node) {
        if (node.isLeafNode()) return 0;
        int left = alturaDaArvore(node.getLeftNode());
        int right = alturaDaArvore(node.getRightNode());
        return Math.max(left, right) + 1;
    }

    public static <T> List<Node<T>> getAllNodeOfLevel(Node<T> rootNode, int level) {
        List<Node<T>> result = new LinkedList<>();
        getAllNodeOfLevel(result, rootNode, 0, level);
        return result;
    }

    private static <T> void getAllNodeOfLevel(List<Node<T>> result, Node<T> node, int level, int targetLevel) {
        if (node.isLeafNode())
            return;
        if (level < targetLevel) {
            getAllNodeOfLevel(result, node.getLeftNode(), level + 1, targetLevel);
            getAllNodeOfLevel(result, node.getRightNode(), level + 1, targetLevel);
            return;
        }
        result.add(node);
    }

    private void waitJobsFinish() {
        while (openedElements.get() > 0) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> Iterator<Node<T>> buscaEmLargura(Node<T> rootNode) {
        return new Iterator<Node<T>>() {

            Queue<Node<T>> nodeQueue = new LinkedList<>(Arrays.asList(rootNode));

            @Override
            public boolean hasNext() {
                return !nodeQueue.isEmpty();
            }

            @Override
            public Node<T> next() {
                Node<T> next = nodeQueue.poll();
                if (!next.isLeafNode()) {
                    nodeQueue.add(next.getLeftNode());
                    nodeQueue.add(next.getRightNode());
                }
                return next;
            }
        };
    }
}
