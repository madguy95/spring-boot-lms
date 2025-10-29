package com.springjwt.module.message.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailData {
    private String to;
    private String subject;
    private String body;
}