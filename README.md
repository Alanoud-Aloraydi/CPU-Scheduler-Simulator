# ⚙️ CPU Scheduler Simulator

A Java console program that simulates how an operating system schedules processes on
the CPU. You enter a set of processes (arrival time, burst time, priority), and it runs
two scheduling algorithms, printing a Gantt-chart timeline and performance metrics for
each so you can compare them.

> **University:** King Saud University — Information Technology
> **Course:** CSC 227 — Operating Systems

## Algorithms
1. **Preemptive Priority + Round Robin** — the CPU always runs the highest-priority
   ready process. Processes that share the same priority take turns using a time
   quantum (Round Robin). A higher-priority arrival **preempts** the running process.
2. **First-Come, First-Served (FCFS)** — processes run to completion in arrival order.

## Metrics reported
- **Gantt chart** of the execution timeline (including **idle** CPU periods).
- **Average Turnaround Time** — average of `completion − arrival`.
- **Average Waiting Time** — average of `turnaround − burst`.
- **CPU Utilization** — busy time ÷ total time.

## How to run
```bash
cd src
javac project_os1/Project_Os1.java
java project_os1.Project_Os1
```
Then follow the prompts, e.g.:
```
Enter number of processes: 3
Process P1: arrival 0, burst 5, priority 2
Process P2: arrival 1, burst 3, priority 1
Process P3: arrival 2, burst 4, priority 3
Enter time quantum (for Priority+RR): 2
```

## Example output
```
===== Priority Queue with Round Robin =====
Time Process
0-1 P1
1-4 P2
4-8 P1
8-12 P3
Average Turnaround Time: 7.00
Average Waiting Time: 3.00
CPU Utilization: 100.00%

===== First-Come First-Served (FCFS) =====
Time Process
0-5 P1
5-8 P2
8-12 P3
Average Turnaround Time: 7.33
Average Waiting Time: 3.33
CPU Utilization: 100.00%
```
(Here P2 arrives at t=1 with a higher priority and preempts P1 under Priority+RR,
but simply waits its turn under FCFS — showing how the algorithm changes the schedule.)

## How it works
- Processes are copied and sorted by arrival time before each simulation, so the two
  algorithms run independently on the same input.
- The Priority+RR scheduler keeps ready processes in a `TreeMap<priority, queue>` so the
  highest-priority group is always at the front, and it computes when the next
  higher-priority process arrives to preempt at the right moment.
- Adjacent Gantt slices for the same process are merged for a clean timeline, and gaps
  between slices are shown as `IDLE`.

## Notes on this version
Cleaned-up version of the original course submission. The scheduling logic is unchanged
(verified correct on several cases, including preemption and idle-CPU gaps); the Gantt
output was enhanced to display idle CPU periods explicitly.
