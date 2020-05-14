package com.studiojms.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

public class CustomerSatisfactionDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        final int val = new Random().nextInt(10);
        String itemStatus;
        if (val <= 6) {
            itemStatus = "CORRECT_ITEM";
        } else {
            itemStatus = "INCORRECT_ITEM";
        }
        return new FlowExecutionStatus(itemStatus);
    }
}
