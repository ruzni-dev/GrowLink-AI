package com.growlink.backend.service;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.growlink.backend.entity.Interest;
import com.growlink.backend.repository.InterestRepository;

@Configuration
public class InterestInitializer {
    @Bean
    CommandLineRunner seedInterests(InterestRepository repository) {
        return args -> {
            List<String> names = List.of("Technology", "Education", "Sports", "Entrepreneurship", "Volunteering", "Community Development", "Career Development", "Arts and Culture");
            names.forEach(name -> repository.findByNameIgnoreCase(name).orElseGet(() -> repository.save(new Interest(name))));
        };
    }
}
