
package project_os1;


import java.util.*;
import java.io.*;

/**
 * Process Scheduling Simulator
 * Supports:
 * - Preemptive Priority Scheduling with Round Robin among same priority
 * - FCFS
 *
 * Author: Alanoud Aloraydi, Norah Aldalal, Tala Alsheail, Aseel Almubaddel
 */
public class Project_Os1 {

    static class Process {
        String id;
        int arrival;
        int burst;
        int remaining;
        int priority;
        int startTime = -1;
        int completionTime = -1;

        public Process(String id, int arrival, int burst, int priority) {
            this.id = id;
            this.arrival = arrival;
            this.burst = burst;
            this.remaining = burst;
            this.priority = priority;
        }

        public Process cloneCopy() {
            Process p = new Process(this.id, this.arrival, this.burst, this.priority);
            p.remaining = this.remaining;
            p.startTime = this.startTime;
            p.completionTime = this.completionTime;
            return p;
        }
    }

    static class GanttEntry {
        int start, end;
        String pid;
        public GanttEntry(int s, int e, String id) {
            this.start = s; this.end = e; this.pid = id;
        }
        @Override
        public String toString() {
            return start + "-" + end + " " + pid;
        }
    }

    static List<Process> sortByArrival(List<Process> list) {
        List<Process> copy = new ArrayList<>(list.size());
        for (Process p: list) copy.add(p.cloneCopy());
        copy.sort(Comparator.comparingInt((Process p) -> p.arrival).thenComparing(p -> p.id));
        return copy;
    }

  
    static int fetchArrivals(List<Process> processes, int index, int currentTime, TreeMap<Integer, LinkedList<Process>> ready) {
        while (index < processes.size() && processes.get(index).arrival <= currentTime) {
            Process pr = processes.get(index);
            ready.computeIfAbsent(pr.priority, k -> new LinkedList<>()).add(pr);
            index++;
        }
        return index;
    }

    static void simulatePriorityRR(List<Process> origList, int quantum) {
        System.out.println("===== Priority Queue with Round Robin =====");
        List<Process> processes = sortByArrival(origList);

        int n = processes.size();
        int currentTime = 0;
        int index = 0;
        int completed = 0;
        long cpuBusy = 0;

        TreeMap<Integer, LinkedList<Process>> ready = new TreeMap<>();
        List<GanttEntry> gantt = new ArrayList<>();

        
        index = fetchArrivals(processes, index, currentTime, ready);
        if (ready.isEmpty() && index < processes.size()) {
            currentTime = processes.get(index).arrival;
            index = fetchArrivals(processes, index, currentTime, ready);
        }

        while (completed < n) {
            if (ready.isEmpty()) {
                if (index < processes.size()) {
                    currentTime = processes.get(index).arrival;
                    index = fetchArrivals(processes, index, currentTime, ready);
                } else break;
                continue;
            }

            int pri = ready.firstKey();
            LinkedList<Process> queue = ready.get(pri);
            Process p = queue.poll();
            if (queue.isEmpty()) ready.remove(pri);

            if (p.startTime == -1) p.startTime = currentTime;

            int nextHigherArrivalTime = Integer.MAX_VALUE;
            if (index < processes.size()) {
                for (int j = index; j < processes.size(); j++) {
                    Process future = processes.get(j);
                    if (future.priority < p.priority) {
                        nextHigherArrivalTime = future.arrival;
                        break;
                    }
                }
            }

            int timeToHigher = (nextHigherArrivalTime == Integer.MAX_VALUE) ? Integer.MAX_VALUE : (nextHigherArrivalTime - currentTime);
            int runTime = Math.min(quantum, p.remaining);
            if (timeToHigher <= 0) {
                 runTime = 0;
            } else if (timeToHigher < runTime) {
                runTime = timeToHigher;
            }

            if (runTime == 0) {
                 if (index < processes.size()) {
                    currentTime = processes.get(index).arrival;
                    index = fetchArrivals(processes, index, currentTime, ready);
                    ready.computeIfAbsent(p.priority, k -> new LinkedList<>()).addFirst(p);
                    continue;
                } else {
                    runTime = Math.min(quantum, p.remaining);
                }
            }

            int start = currentTime;
            currentTime += runTime;
            int end = currentTime;
            if (runTime > 0) gantt.add(new GanttEntry(start, end, p.id));
            p.remaining -= runTime;
            cpuBusy += runTime;

           
            index = fetchArrivals(processes, index, currentTime, ready);

            if (p.remaining == 0) {
                p.completionTime = currentTime;
                completed++;
            } else {
                ready.computeIfAbsent(p.priority, k -> new LinkedList<>()).add(p);
            }
        }

        printGanttAndMetrics(processes, gantt, cpuBusy);
    }

    static void simulateFCFS(List<Process> origList) {
        System.out.println("===== First-Come First-Served (FCFS) =====");
        List<Process> processes = sortByArrival(origList);
        int n = processes.size();
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int index = 0;
        long cpuBusy = 0;
        List<GanttEntry> gantt = new ArrayList<>();

        while (index < processes.size() || !readyQueue.isEmpty()) {
            if (readyQueue.isEmpty()) {
                currentTime = Math.max(currentTime, processes.get(index).arrival);
                 while (index < processes.size() && processes.get(index).arrival <= currentTime) {
                    readyQueue.add(processes.get(index));
                    index++;
                }
            }

            Process p = readyQueue.poll();
            if (p.startTime == -1) p.startTime = currentTime;

            int start = currentTime;
            currentTime += p.burst;
            int end = currentTime;
            gantt.add(new GanttEntry(start, end, p.id));

            cpuBusy += p.burst;
            p.remaining = 0;
            p.completionTime = currentTime;

            while (index < processes.size() && processes.get(index).arrival <= currentTime) {
                readyQueue.add(processes.get(index));
                index++;
            }
        }

        printGanttAndMetrics(processes, gantt, cpuBusy);
    }

   
    static void printGanttAndMetrics(List<Process> processes, List<GanttEntry> gantt, long cpuBusy) {
        List<GanttEntry> mergedGantt = new ArrayList<>();
        if (!gantt.isEmpty()) {
            mergedGantt.add(gantt.get(0));
            for (int i = 1; i < gantt.size(); i++) {
                GanttEntry last = mergedGantt.get(mergedGantt.size() - 1);
                GanttEntry current = gantt.get(i);
                if (current.pid.equals(last.pid) && current.start == last.end)
                    last.end = current.end;
                else
                    mergedGantt.add(current);
            }
        }

        System.out.println("Time Process");
        int prevEnd = -1;
        for (GanttEntry g : mergedGantt) {
            // Show any CPU idle gap between the previous slice and this one.
            if (prevEnd != -1 && g.start > prevEnd)
                System.out.println(prevEnd + "-" + g.start + " IDLE");
            System.out.println(g);
            prevEnd = g.end;
        }

        double totalTurnaround = 0;
        double totalWaiting = 0;
        int n = processes.size();
        int firstArrival = Integer.MAX_VALUE;
        int lastCompletion = Integer.MIN_VALUE;

        for (Process p : processes) {
            if (p.completionTime == -1) continue;
            int tat = p.completionTime - p.arrival;
            int wt = tat - p.burst;
            totalTurnaround += tat;
            totalWaiting += wt;
            firstArrival = Math.min(firstArrival, p.arrival);
            lastCompletion = Math.max(lastCompletion, p.completionTime);
        }

        double avgTAT = totalTurnaround / n;
        double avgWT = totalWaiting / n;

        double totalTime = lastCompletion - firstArrival;
        double idleTime = totalTime - cpuBusy;
        if (idleTime < 0) idleTime = 0;
        double cpuUtil = (cpuBusy / (double)(cpuBusy + idleTime)) * 100.0;

        System.out.println("\nPerformance Metrics");
        System.out.printf("Average Turnaround Time: %.2f%n", avgTAT);
        System.out.printf("Average Waiting Time: %.2f%n", avgWT);
        System.out.printf("CPU Utilization: %.2f%%%n", cpuUtil);
    }

    static List<Process> deepCopy(List<Process> list) {
        List<Process> cp = new ArrayList<>();
        for (Process p : list) cp.add(new Process(p.id, p.arrival, p.burst, p.priority));
        return cp;
    }

    public static void main(String[] args) throws Exception {
    Scanner sc = new Scanner(System.in);
    System.out.print("Enter number of processes: ");
    int n = Integer.parseInt(sc.nextLine().trim());
    List<Process> processes = new ArrayList<>();

    System.out.println("Enter details for each process:");

    for (int i = 0; i < n; i++) {
        String id = "P" + (i + 1);

        System.out.println("\nProcess " + id + ":");
        System.out.print("Arrival time: ");
        int arrival = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Burst time: ");
        int burst = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Priority: ");
        int priority = Integer.parseInt(sc.nextLine().trim());

        processes.add(new Process(id, arrival, burst, priority)); 
    }

    System.out.print("\nEnter time quantum (for Priority+RR): ");
    int quantum = Integer.parseInt(sc.nextLine().trim());
    sc.close();

    List<Process> copyForPriority = deepCopy(processes);
    List<Process> copyForFCFS = deepCopy(processes);

    System.out.println();
    simulatePriorityRR(copyForPriority, quantum);
    System.out.println();
    simulateFCFS(copyForFCFS);
}

}