package embs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Task {
    public int id;
    public int priority;
    public int releasedTime;
    public int runningTime;
    public int runTime = 0; // 已经执行的时间
    public List<Task> predecessor = new ArrayList<>();
    public List<Task> successors = new ArrayList<>();

    public Task(int id, int releasedTime, int runningTime) {
        this.id = id;
        this.releasedTime = releasedTime;
        this.runningTime = runningTime;
    }

    // 添加任务后继, 并设置前驱
    public void addSuccessor(Task next) {
        this.successors.add(next);
        next.predecessor.add(this);
    }

    // 检查任务是否执行完成
    public boolean isFinished() {
        return runTime >= runningTime;
    }

    // 时钟驱动函数
    public boolean clk() {
        if (isFinished()) return false;
        runTime++;
        return !isFinished();
    }

    public DependencyRelation getDependencyRelation(Task task) {
        Deque<Task> taskDeque = new ArrayDeque<>();
        // 检查是否为前驱
        taskDeque.addAll(predecessor);
        while (!taskDeque.isEmpty()) {
            Task t = taskDeque.poll();
            if (t.id == task.id) {
                return DependencyRelation.PREDECESSOR;
            }
            taskDeque.addAll(t.predecessor);
        }
        // 检查是否为后继
        taskDeque.addAll(successors);
        while (!taskDeque.isEmpty()) {
            Task t = taskDeque.poll();
            if (t.id == task.id) {
                return DependencyRelation.SUCCESSOR;
            }
            taskDeque.addAll(t.successors);
        }
        // 未发现两个任务之间存在依赖
        return DependencyRelation.NONE;
    }
}
