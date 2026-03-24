package com.example.demo.task.entity;

public class Task {
    private int TaskId;
    private int TaskCount;
    public Task(int taskId, int taskCount) {
        TaskId = taskId;
        TaskCount = taskCount;
    }

    public int getTaskId() {
        return TaskId;
    }

    public void setTaskId(int taskId) {
        TaskId = taskId;
    }

    public int getTaskCount() {
        return TaskCount;
    }

    public void setTaskCount(int taskCount) {
        TaskCount = taskCount;
    }

    @Override
    public String toString() {
        return "Task{" +
                "TaskId=" + TaskId +
                ", TaskCount=" + TaskCount +
                '}';
    }
}
