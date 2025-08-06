package service;

import DAO.LocationDao;
import DAO.PathDao;
import model.location;
import model.path;

import java.nio.file.Path;
import java.util.*;

public class PathService {
    // 内部类用于优先队列
    private static class PathNode implements Comparable<PathNode> {
        int id;
        double distance;

        public PathNode(int id, double distance) {
            this.id = id;
            this.distance = distance;
        }

        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public static boolean addPath(int start, int end) {
        location l1 = new LocationDao().findById(start);
        location l2 = new LocationDao().findById(end);
        path p1 = new path();
        if (l1 != null && l2 != null && !l1.equals(l2)) {
            p1.setStartid(start);
            p1.setEndid(end);
            double d1 = Math.abs(l1.getLatitude() - l2.getLatitude());
            double d2 = Math.abs(l1.getLongitude() - l2.getLongitude());
            double dis = Math.sqrt(d1 * d1 + d2 * d2)*10;
            p1.setDistance(dis);
            if (new PathDao().insert(p1) != 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean deletePath(int id) {
        if (new PathDao().delete(id) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static List<path> getallPath() {
        List<path> allpath = new PathDao().findAll();
        return allpath;
    }

    public static List<path> getallPath(int id) {
        List<path> allpath = new PathDao().findByLocation(id);
        return allpath;
    }

    /**
     * 使用Dijkstra算法查找最短路径
     * @param startId 起始地点ID
     * @param endId 目标地点ID
     * @return 包含路径ID列表的List，如果找不到路径返回空列表
     */
    public static List<Integer> findShortestPath(int startId, int endId) {
        List<path> allPaths = new PathDao().findAll();
        Map<Integer, List<path>> graph = buildGraph(allPaths);
        //各个点到起始点的距离
        Map<Integer, Double> distances = new HashMap<>();
        //储存路径信息
        Map<Integer, Integer> predecessors = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>();

        // 初始化距离
        for (Integer nodeId : graph.keySet()) {
            distances.put(nodeId, Double.MAX_VALUE);
        }
        distances.put(startId, 0.0);
        queue.add(new PathNode(startId, 0.0));

        // Dijkstra算法主循环
        while (!queue.isEmpty()) {
            //从距离起始点距离最小的开始
            PathNode currentNode = queue.poll();
            int currentId = currentNode.id;

            if (currentId == endId) {
                break;
            }

            for (path edge : graph.getOrDefault(currentId, new ArrayList<>())) {
                int neighborId = (edge.getStartid() == currentId) ? edge.getEndid() : edge.getStartid();
                double newDist = distances.get(currentId) + edge.getDistance();

                if (newDist < distances.get(neighborId)) {
                    distances.put(neighborId, newDist);
                    predecessors.put(neighborId, currentId);
                    queue.add(new PathNode(neighborId, newDist));
                }
            }
        }

        // 构建路径
        List<Integer> path = new ArrayList<>();
        if (distances.get(endId) == Double.MAX_VALUE) {
            return path; // 返回空列表表示无路径
        }

        int current = endId;
        while (current != startId) {
            path.add(current);
            current = predecessors.get(current);
        }
        path.add(startId);
        Collections.reverse(path);

        return path;
    }

    /**
     * 获取最短路径的总距离
     * @param pathIds 路径ID列表
     * @return 总距离，如果路径无效返回0
     */
    public static double getPathDistance(List<Integer> pathIds) {
        if (pathIds == null || pathIds.size() < 2) {
            return 0;
        }

        List<path> allPaths = new PathDao().findAll();
        double totalDistance = 0;

        for (int i = 0; i < pathIds.size() - 1; i++) {
            int startId = pathIds.get(i);
            int endId = pathIds.get(i + 1);
            boolean found = false;

            for (path p : allPaths) {
                if ((p.getStartid() == startId && p.getEndid() == endId) ||
                        (p.getStartid() == endId && p.getEndid() == startId)) {
                    totalDistance += p.getDistance();
                    found = true;
                    break;
                }
            }

            if (!found) {
                return 0; // 路径不连续
            }
        }

        return totalDistance;
    }

    // 构建图的邻接表
    private static Map<Integer, List<path>> buildGraph(List<path> paths) {
        Map<Integer, List<path>> graph = new HashMap<>();

        for (path p : paths) {
            graph.computeIfAbsent(p.getStartid(), k -> new ArrayList<>()).add(p);
            graph.computeIfAbsent(p.getEndid(), k -> new ArrayList<>()).add(p);
        }
        return graph;
    }
}