package embs;

import java.util.*;

public class PriorTable {
    private List<Task> priorityTable = new ArrayList<>();
    private int[] taskFinishTime;

    // 创建任务优先级表
    public PriorTable(int[] releaseTimeTable, int[] runningTime, int[][] dependencyList) {
        // 初始化任务List；
        Task[] tasks = new Task[releaseTimeTable.length];
        for (int i = 0; i < releaseTimeTable.length; i++) {
            tasks[i] = new Task(i, releaseTimeTable[i], runningTime[i]);
        }
        // 设置任务的依赖序列
        for (int i = 0; i < releaseTimeTable.length; i++) {
            for (int successorId : dependencyList[i]) {
                tasks[i].addSuccessor(tasks[successorId]);
            }
        }
        // 计算优先级值
        calculatePriorityValues(tasks);

        // 存储到priorityTable中,并根据算法要求生成 “任务优先级表”
        Collections.addAll(priorityTable, tasks);
        priorityTable.sort((o1, o2) -> {
            // 1 若Priority不相等则，按照Priority从大到小排序
            if (o1.priority != o2.priority) {
                return o2.priority - o1.priority;
            }
            DependencyRelation dp = o1.getDependencyRelation(o2);
            // 2 若存在依赖关系，则以依赖关系排序(前驱 优于 后继)
            if (dp != DependencyRelation.NONE) {
                return dp.getValue();
            }
            // 3 若释放时间不相等，则按照释放时间从小到大排序
            if (o1.releasedTime != o2.releasedTime) {
                return o1.releasedTime - o2.releasedTime;
            }
            // 4 若运行时间不相等，则按照运行时间从大到小排序
            if (o1.runningTime != o2.runningTime) {
                return o2.runningTime - o1.runningTime;
            }
            // 5 如上述条件都相等，则按照ID从小到大进行排序
            return o1.id - o2.id;
        });
        // 初始化任务完成时间为-1
        this.taskFinishTime = new int[releaseTimeTable.length];
        for (int i = 0; i < releaseTimeTable.length; i++) {
            taskFinishTime[i] = -1;
        }
    }

    // 计算优先级值 Pri(Ji)= pi+O(Ji)+MaxJk∈Suc(Ji)Pri(Jk)
    private void calculatePriorityValues(Task[] tasks) {
        for (Task task : tasks) {
            task.priority = task.runningTime + task.successors.size() + getMaxSuccessorPri(task);
        }
    }

    // 递归计算后继部分的优先级值 MaxJk∈Suc(Ji)Pri(Jk)
    private int getMaxSuccessorPri(Task task) {
        int maxSuccessorPri = 0;
        for (Task t : task.successors) {
            maxSuccessorPri = Math.max(maxSuccessorPri, t.runningTime + t.successors.size() + getMaxSuccessorPri(t));
        }
        return maxSuccessorPri;
    }

    // 获取下一个可以执行的 Task
    public Task getNext(int currentTime) {
        for (int i = 0; i < priorityTable.size(); i++) {
            Task candidateTask = priorityTable.get(i);
            // 当前时间未达到释放要求
            if (candidateTask.releasedTime > currentTime) continue;
            // 检查Task的前驱是否均已完成
            boolean isValid = true;
            Deque<Task> queue = new ArrayDeque<>();
            queue.addAll(candidateTask.predecessor);
            while (!queue.isEmpty()) {
                if (taskFinishTime[queue.poll().id] != -1) continue;
                isValid = false;
                break;
            }
            // 若合法则返回该任务
            if (isValid) return priorityTable.remove(i);
        }
        System.out.printf("Time %d: No more appropriate task at this time.\n", currentTime);
        return null;
    }

    // 检查任务队列中是否还存在有未执行的任务
    public boolean hasNext() {
        for (int time : taskFinishTime) {
            if (time == -1) return true;
        }
        return false;
    }

    public void finishTask(Task task, int currentTime) {
        taskFinishTime[task.id] = currentTime;
    }
}
