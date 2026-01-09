package com.pbl7.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String otp;
    String email;

    @OneToOne
    User user;

}
