package com.studiojms.batch;

import com.studiojms.batch.model.User;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.io.File;

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
    public Job job() {
        return jobBuilderFactory.get("job").start(chunkBasedStep()).build();
    }

    private Step chunkBasedStep() {
        return stepBuilderFactory.get("chunkBasedStep").<User, User>chunk(3)
                .reader(itemReader())
                .writer(items -> {
                    System.out.println(String.format("Received a list of size %s", items.size()));
                    items.forEach(System.out::println);
                }).build();
    }

    @Bean
    public ItemReader<User> itemReader() {
        final String[] tokens = {"Username", "Login email", "Identifier", "First name", "Last name"};


        final FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new ClassPathResource("username-or-email.csv"));

        final DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();
        final DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(";");
        tokenizer.setNames(tokens);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new UserFieldSetMapper());

        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    public JobExecutionDecider decider() {
        return new DeliveryDecider();
    }

    @Bean
    public Job deliverPackageJob() {
        return jobBuilderFactory.get("deliverPackageJob")
                .start(packageItemStep())
//                .on("*").to(deliveryFlow())
//                .next(nestedBillingJobStep())
                .split(new SimpleAsyncTaskExecutor())
                .add(deliveryFlow(), billingFlow())
                .end()
                .build();
    }

    @Bean
    public Flow deliveryFlow() {
        return new FlowBuilder<SimpleFlow>("deliveryFlow")
                .start(driveToAddressStep())
                    .on("FAILED").fail()
                .from(driveToAddressStep())
                    .on("*").to(decider())
                        .on("PRESENT").to(givePackageToCustomerStep())
                            .next(customerSatisfactionDecider())
                                .on("CORRECT_ITEM").to(thankCustomerStep())
                            .from(customerSatisfactionDecider())
                                .on("INCORRECT_ITEM").to(refundCustomerStep())
                    .from(decider())
                        .on("NOT_PRESENT").to(leavePackageAtDoorStep())
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

    @Bean
    public Job prepareFlowersJob() {
        return jobBuilderFactory.get("prepareFlowersJob")
                .start(selectFlowersStep())
                    .on("TRIM_REQUIRED").to(removeThornsStep()).next(arrangeFlowersStep())
                .from(selectFlowersStep())
                    .on("NO_TRIM_REQUIRED").to(arrangeFlowersStep())
                .from(arrangeFlowersStep()).on("*").to(deliveryFlow())
                .end()
                .build();
    }

    @Bean
    public StepExecutionListener selectFlowerListener() {
        return new FlowersSelectionStepExecutionListener();
    }

    @Bean
    public Step selectFlowersStep() {
        return stepBuilderFactory.get("selectFlowersStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Selecting flowers");
            return RepeatStatus.FINISHED;
        })
        .listener(selectFlowerListener())
        .build();
    }

    @Bean
    public Step removeThornsStep() {
        return stepBuilderFactory.get("removeThornsStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Removing thorns from flowers");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step arrangeFlowersStep() {
        return stepBuilderFactory.get("arrangeFlowersStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Arranging flowers for order");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step nestedBillingJobStep() {
        return stepBuilderFactory.get("nestedBillingJobStep").job(billingJob()).build();
    }

    @Bean
    public Step sendInvoiceStep() {
        return stepBuilderFactory.get("sendInvoiceStep").tasklet((contribution, chunkContext) -> {
            System.out.println("Invoice is sent to the customer");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Job billingJob() {
        return jobBuilderFactory.get("billingJob")
                .start(sendInvoiceStep())
                .build();
    }

    @Bean
    public Flow billingFlow() {
        return new FlowBuilder<SimpleFlow>("billingFlow")
                .start(sendInvoiceStep())
                .build();
    }

}