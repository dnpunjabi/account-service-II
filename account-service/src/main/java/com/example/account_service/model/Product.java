package com.example.account_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Data
@Setter
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String name;
    private String productCode;
    private boolean onboarding;
    private boolean pinActivation;
    private boolean onlineBankingActivation;

}
