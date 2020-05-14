package com.studiojms.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
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

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    public JobExecutionDecider decider() {
        return new DeliveryDecider();
    }

    @Bean
    public Job deliverPackageJob() {
        return jobBuilderFactory.get("deliverPackageJob")
                .start(packageItemStep())
                .next(driveToAddressStep())
                    .on("FAILED").to(storePackageStep())
                .from(driveToAddressStep())
                    .on("*").to(decider())
                        .on("PRESENT").to(givePackageToCustomerStep())
                            .next(customerSatisfactionDecider())
                                .on("CORRECT_ITEM").to(thankCustomerStep())
                            .from(customerSatisfactionDecider())
                                            .on("INCORRECT_ITEM").to(refundCustomerStep())
                        .from(decider())
                            .on("NOT_PRESENT").to(leavePackageAtDoorStep())
                .end()
                .build();
    }

    @Bean
    public Step refundCustomerStep() {
        return stepBuilderFactory.get("refundCustomerStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Customer was refund");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step thankCustomerStep() {
        return stepBuilderFactory.get("thankCustomerStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Thanks for the item");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public JobExecutionDecider customerSatisfactionDecider() {
        return new CustomerSatisfactionDecider();
    }

    @Bean
    public Step packageItemStep() {
        return stepBuilderFactory.get("packageItemStep").tasklet((contribution, chunkContext) -> {
            final String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
            final String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

            System.out.println(String.format("Item %s has been packaged on %s", item, date));
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step driveToAddressStep() {
        boolean GOT_LOST = false;
        return stepBuilderFactory.get("driveToAddressStep").tasklet((contribution, chunkContext) -> {
            if (GOT_LOST) {
                throw new RuntimeException("Got lost driving to the address");
            }

            System.out.println("Successfully arrived to address");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step storePackageStep() {
        return stepBuilderFactory.get("storePackageStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Storing the package while the customer address isn't located");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step leavePackageAtDoorStep() {
        return stepBuilderFactory.get("leavePackageAtDoorStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Leaving package at the door");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step givePackageToCustomerStep() {
        return stepBuilderFactory.get("givePackageToCustomerStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Given the package to the customer");
            return RepeatStatus.FINISHED;
        }).build();
    }

}
