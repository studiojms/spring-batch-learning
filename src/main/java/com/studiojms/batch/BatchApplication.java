package com.studiojms.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class BatchApplication {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job deliverPackageJob() {
		return jobBuilderFactory.get("deliverPackageJob").start(packageItemStep()).build();
	}

	@Bean
	public Step packageItemStep() {
		return stepBuilderFactory.get("packageItem").tasklet((contribution, chunkContext) -> {
			System.out.println("Item has been packaged");
			return RepeatStatus.FINISHED;
		}).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

}
