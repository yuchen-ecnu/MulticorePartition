package embs;

import java.util.ArrayList;
import java.util.List;

public class Driver {
    public static void main(String[] args) {
//        // 输入参数 (为符合程序定义，编号均修改为从零开始)
//        int[][] dependencyList = {{3, 4}, {3, 4}, {4}, {}, {}};
//        int[] runningTime = {1, 1, 2, 1, 1};
//        int[] releaseTime = {0, 3, 0, 0, 0};
//        PriorTable priorTable = new PriorTable(releaseTime, runningTime, dependencyList);
//        TaskExecutor taskExecutor = new TaskExecutor(priorTable, 2);
//        while (taskExecutor.clk()) ;
//        taskExecutor.printLog();

        // 输入参数 (为符合程序定义，编号均修改为从零开始)
        int[][] dependencyList = {{3}, {3, 4}, {4}, {5, 6}, {7, 8}, {9}, {9}, {10}, {10}, {}, {}};
        int[] runningTime = {1, 2, 1, 4, 1, 3, 1, 1, 2, 2, 2};
        int[] releaseTime = {0, 0, 0, 4, 0, 0, 0, 6, 0, 0, 0};
        PriorTable priorTable = new PriorTable(releaseTime, runningTime, dependencyList);
        TaskExecutor taskExecutor = new TaskExecutor(priorTable, 3);
        while (taskExecutor.clk()) ;
        taskExecutor.printLog();
    }

    // 任务执行器
    static class TaskExecutor {
        // 任务优先级表
        public PriorTable priorTable;
        // 当前时间
        public int currentTime = 0;
        // 处理器 (Processors)
        private Task[] processors;
        // 日志器 (Logger)
        private List<Info>[] logger;

        // 创建任务执行器
        public TaskExecutor(PriorTable priorTable, int processors_cnt) {
            this.priorTable = priorTable;
            this.processors = new Task[processors_cnt];
            this.logger = new List[processors_cnt];
            for (int i = 0; i < processors_cnt; i++) {
                logger[i] = new ArrayList<>();
            }
        }

        // 时钟，调用一次时间增加1个单位
        public boolean clk() {
            if (isFinished()) return false;
            // 将处理器中的所有任务执行一个单位时间
            for (int i = 0; i < processors.length; i++) {
                Task p = processors[i];
                if (p == null || p.clk()) continue;
                logger[i].get(logger[i].size() - 1).endTime = currentTime;
                System.out.printf("Time %d: Task %d in P%d Finished.\n", currentTime, processors[i].id, i);
                priorTable.finishTask(processors[i], currentTime);
                processors[i] = null;
            }
            // 寻找空处理器并添加新的任务;
            for (int i = 0; i < processors.length; i++) {
                if (processors[i] != null) continue;
                Task nextTask = priorTable.getNext();
                // 若当前不存在可以执行的任务，则停止部署任务
                if (nextTask == null) break;
                processors[i] = nextTask;
                System.out.printf("Time %d: Task %d Deployed in P%d.\n", currentTime, processors[i].id, i);
                Info info = new Info(nextTask.id, currentTime, i);
                logger[i].add(info);
            }
            currentTime += 1;
            return !isFinished();
        }

        // 检查作业是否结束
        public boolean isFinished() {
            // 检查是否有未执行的任务
            if (priorTable.hasNext()) return false;
            // 检查是否有正在执行的任务
            for (Task task : processors) {
                if (task != null) return false;
            }
            return true;
        }

        // 打印调度结果图和日志
        public void printLog() {
            System.out.println();
            // 图形输出
            for (int i = 0; i < logger.length; i++) {
                int currentTime = 0;
                System.out.printf("P%d: |", i);
                while (!logger[i].isEmpty()) {
                    Info info = logger[i].remove(0);
                    for (; currentTime < info.startTime; currentTime++) {
                        System.out.print("_____|");
                    }
                    if (info.id >= 10) {
                        System.out.printf("__%d%d_", info.id / 10, info.id % 10);
                    } else {
                        System.out.printf("__%d__", info.id);
                    }
                    currentTime++;
                    for (; currentTime < info.endTime; currentTime++) {
                        System.out.print("______");
                    }
                    System.out.print("|");
                }
                System.out.println();
                // 打印 Label
                System.out.print("    ");
                for (int j = 0; j <= currentTime; j++) {
                    System.out.printf("%d     ", j);
                }
                System.out.println();
            }
        }
    }
}
