package com.example.demo.entity;

public class Task {
    private int TaskId;
    private int TaskCount;

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
