package com.pbl7.identity_service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // nhung chuoi duoc random ngau nhien
    private String id;

    private String username;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "is_enabled", nullable = false, columnDefinition = "boolean default false")
    private boolean isEnabled = false;

    @ManyToMany
    Set<Role> roles;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    Set<Address> shipAddress = new HashSet<>();

    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean default false")
    boolean emailVerified;

    @ElementCollection
    private List<Long> favoriteRestaurantIds = new ArrayList<>();

    @ElementCollection
    private List<Long> orderIds = new ArrayList<>();

    @ElementCollection
    private List<Long> addressIds = new ArrayList<>();


}
