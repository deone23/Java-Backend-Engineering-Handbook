package com.javahandbook.hibernate_identity_vs_sequence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.javahandbook.hibernate_identity_vs_sequence.service.ClearExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.DetachExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.DirtyCheckingExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.EmployeeIdentityService;
import com.javahandbook.hibernate_identity_vs_sequence.service.FlushBeforeQueryExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.FlushCommitModeExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.FlushRollbackExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.MergeExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.RefreshExperiment;
import com.javahandbook.hibernate_identity_vs_sequence.service.SequenceAllocationExperiment;

@SpringBootApplication
public class HibernateIdentityVsSequenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HibernateIdentityVsSequenceApplication.class, args);
	}

	@Bean
    CommandLineRunner run(MergeExperiment experiment) {
    return args -> experiment.run();
       }

}
