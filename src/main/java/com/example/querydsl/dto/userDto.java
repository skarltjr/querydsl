package com.example.querydsl.dto;

import lombok.Data;

@Data
public class userDto {
    private String username;
    private int age;

    public userDto() { }

    public userDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
