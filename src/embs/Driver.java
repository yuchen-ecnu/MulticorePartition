package embs;

import java.util.ArrayList;
import java.util.List;

public class Driver {
    public static void main(String[] args) {
//         输入参数 (为符合程序定义，编号均修改为从零开始)
//        int[][] dependencyList = {{3, 4}, {3, 4}, {4}, {}, {}};
//        int[] runningTime = {1, 1, 2, 1, 1};
//        int[] releaseTime = {0, 0, 0, 0, 0};
//        PriorTable priorTable = new PriorTable(releaseTime, runningTime, dependencyList);
//        priorTable.printPriorityTable();
//        TaskExecutor taskExecutor = new TaskExecutor(priorTable, 2);
//        while (taskExecutor.clk(false)) ;
//        taskExecutor.printLog();
        // 输入参数 (为符合程序定义，编号均修改为从零开始)
//        int[][] dependencyList = {{3}, {3, 4}, {4}, {5, 6}, {7, 8}, {9}, {9}, {10}, {10}, {}, {}};
//        int[] runningTime = {1, 2, 1, 4, 1, 3, 1, 1, 2, 2, 2};
//        int[] releaseTime = {0, 0, 0, 4, 0, 0, 0, 6, 0, 0, 0};
//        PriorTable priorTable = new PriorTable(releaseTime, runningTime, dependencyList);
//        priorTable.printPriorityTable();
//        TaskExecutor taskExecutor = new TaskExecutor(priorTable, 3);
//        while (taskExecutor.clk(false)) ;
//        taskExecutor.printLog();
//         输入参数 (为符合程序定义，编号均修改为从零开始)
        int[][] dependencyList = {{}, {2, 5}, {3}, {}, {5, 7}, {}, {5, 7}, {}};
        int[] runningTime = {3, 1, 2, 2, 2, 4, 4, 1};
        int[] releaseTime = {0, 0, 0, 0, 4, 0, 0, 0};
        PriorTable priorTable = new PriorTable(releaseTime, runningTime, dependencyList, PriorTable.PriorityType.IN_DEGREE);
        priorTable.printPriorityTable();
        TaskExecutor taskExecutor = new TaskExecutor(priorTable, 2);
        while (taskExecutor.clk(true)) ;
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
        // 处理器 使用时长记录器（Recorder）
        private Integer[] times;
        // 日志器 (Logger)
        private List<Info>[] logger;

        // 创建任务执行器
        public TaskExecutor(PriorTable priorTable, int processors_cnt) {
            this.priorTable = priorTable;
            this.processors = new Task[processors_cnt];
            this.logger = new List[processors_cnt];
            this.times = new Integer[processors_cnt];
            for (int i = 0; i < processors_cnt; i++) {
                this.logger[i] = new ArrayList<>();
                this.times[i] = 0;
            }
        }

        // 时钟，调用一次时间增加1个单位
        public boolean clk(boolean preemptible) {
            if (isFinished()) return false;
            // 将处理器中的所有任务执行一个单位时间
            for (int i = 0; i < processors.length; i++) {
                Task p = processors[i];
                if (p == null) continue; //处理器i空闲
                // 处理器执行一个单位时间
                boolean executeSuccess = p.clk();
                times[i]++;
                // 执行成功，且任务未结束，处理下一个处理器
                if (executeSuccess) continue;
                // 任务刚好完成，从处理器i中移除该任务
                logger[i].get(logger[i].size() - 1).endTime = currentTime;
                System.out.printf("Time %d: Task %d in P%d Finished.\n", currentTime, processors[i].id+1, i);
                priorTable.finishTask(processors[i], currentTime);
                processors[i] = null;
            }
            // 寻找空处理器(或抢占处理器)并添加对应新的任务;
            while (priorTable.hasNext()) {
                boolean assignedTaskSuccess = false;
                int acceptedProcessorId = -1;
                Task nextTask = priorTable.getNext(currentTime);
                if (nextTask == null) break;
                // 1. 优先寻找空闲处理器
                for (int i = 0; i < processors.length; i++) {
                    if (processors[i] == null) {
                        processors[i] = priorTable.deployTask(nextTask);
                        assignedTaskSuccess = true;
                        acceptedProcessorId = i;
                        break;
                    }
                }
                if (assignedTaskSuccess) {
                    // 任务被部署在空闲的处理器上，打印并记录日志
                    System.out.printf("Time %d: Task %d Deployed in a free processor P%d.\n", currentTime, processors[acceptedProcessorId].id+1, acceptedProcessorId);
                    logger[acceptedProcessorId].add(new Info(nextTask.id, currentTime, acceptedProcessorId));
                    continue;
                }
                // 若不可抢占，说明该任务当前无法处理，Pending
                if (!preemptible) break;
                // 2. 尝试抢占处理器
                Task preemptedTask = null; // 记录被抢占的处理器，用于打印日志
                for (int i = 0; i < processors.length; i++) {
                    // 如果处理器上的任务优先级低于当前处理任务的优先级，执行抢占
                    if (processors[i].priority < nextTask.priority) {
                        priorTable.stashTask(processors[i]);
                        assignedTaskSuccess = true;
                        acceptedProcessorId = i;
                        preemptedTask = processors[i];
                        processors[i] = priorTable.deployTask(nextTask);
                        break;
                    }
                }
                // 未找到空闲处理器，也未找到可抢占的处理器，Pending
                if (!assignedTaskSuccess) break;

                // 任务被部署在抢占的处理器上，打印并记录日志
                System.out.printf("Time %d: Task %d Preempts Task %d's Processor P%d and Deployed in Processor P%d.\n",
                        currentTime, nextTask.id+1, preemptedTask.id+1, acceptedProcessorId, acceptedProcessorId);
                logger[acceptedProcessorId].add(new Info(nextTask.id, currentTime, acceptedProcessorId));
            }

            System.out.println();
            // 打印处理器状态
            for (int i = 0; i < processors.length; i++) {
                if (processors[i] == null) System.out.printf("Time %d: Processor %d Free.\n", currentTime, i);
                else {
                    System.out.printf("Time %d: Processor %d Running Task %d.\n", currentTime, i, processors[i].id+1);
                }
            }
            System.out.println("---------------------------------------");
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
            // 处理器使用率输出
            System.out.println("Processor usage:");
            for (int i = 0; i < processors.length; i++) {
                System.out.printf("P%d: running time = %d, free time = %d, total time = %d, usage = %f\n", i, times[i], currentTime - times[i] - 1, currentTime - 1, times[i] * 1.0 / (currentTime - 1));
            }
            // 图形输出
            for (int i = 0; i < logger.length; i++) {
                System.out.println();
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
                        System.out.printf("__%d__", info.id+1);
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
