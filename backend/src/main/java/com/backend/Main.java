package com.backend;

import com.backend.member.Gender;
import com.backend.member.Member;
import com.backend.member.MemberRepository;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(MemberRepository memberRepository, PasswordEncoder passwordEncoder){
        return args -> {
            Faker faker = new Faker();
            Random random = new Random();
            String name = faker.name().fullName().toLowerCase();
            String email = faker.internet().safeEmailAddress();
            int age = random.nextInt(18,76);
            Gender gender = Gender.MALE;

            Member john = new Member(
                    name,
                    email,
                    passwordEncoder.encode("password"),
                    age,
                    gender);

            List<Member> members = List.of(john);
            memberRepository.saveAll(members);
            System.out.println(email);
        };
    }
}
