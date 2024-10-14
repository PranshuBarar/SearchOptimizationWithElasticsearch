package com.searchoptimizationv2.search_optimization_application_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.searchoptimizationv2.search_optimization_application_v2", "com.optimization_component"})
public class SearchOptimizationApplicationV2Application {

	public static void main(String[] args) {
		SpringApplication.run(SearchOptimizationApplicationV2Application.class, args);
	}

}
