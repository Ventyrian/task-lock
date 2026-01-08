package com.tasklock;

import lombok.Data;
import java.time.Instant;

@Data
public class CompletedTask
{
    private final Instant completedAt;
    private final String task;

    public CompletedTask(String taskName)
    {
        this.task = taskName;
        this.completedAt = Instant.now();
    }

    public CompletedTask( Instant completed, String taskName)
    {
        this.task = taskName;
        this.completedAt = completed;
    }

}
