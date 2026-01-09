package com.pbl7.identity_service.controller;


import com.pbl7.identity_service.dto.AddressDTO;
import com.pbl7.identity_service.dto.ApiResponse;
import com.pbl7.identity_service.dto.UserDto;
import com.pbl7.identity_service.dto.request.UserCreationRequest;
import com.pbl7.identity_service.dto.request.UserUpdateRequest;
import com.pbl7.identity_service.dto.request.VerifyEmailRequest;
import com.pbl7.identity_service.dto.response.UserResponse;
import com.pbl7.identity_service.entity.Address;
import com.pbl7.identity_service.entity.User;
import com.pbl7.identity_service.mapper.UserMapper;
import com.pbl7.identity_service.service.AuthenticationService;
import com.pbl7.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;
    AuthenticationService authenticationService;
    UserMapper userMapper;

    @PostMapping("/registration")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }


    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        String userId = authenticationService.getUserIdFromToken(token);

        VerifyEmailRequest verifyEmailRequest = new VerifyEmailRequest();
        verifyEmailRequest.setEmailVerified(true);
        userService.verifyEmail(userId, verifyEmailRequest);

        URI redirectUri = URI.create("http://localhost:3000/email-verified");
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }


    @GetMapping
    ApiResponse<List<UserResponse>> getUsers(@RequestHeader("Authorization") String jwt) {
        log.info("jwt: {}", jwt);
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/get-user")
    ApiResponse<UserResponse> getUser() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser())
                .build();
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        User user = userService.findUserById(userId);
        UserDto userDto = userMapper.toUserDto(user);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/{userId}/disable")
    ApiResponse<String> disableUser(@PathVariable String userId) {
        userService.disableUser(userId);
        return ApiResponse.<String>builder().result("User account has been disabled").build();
    }

    @PutMapping("/{userId}/enable")
    ApiResponse<String> enableUser(@PathVariable String userId) {
        userService.enableUser(userId);
        return ApiResponse.<String>builder().result("User account has been enabled").build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @PostMapping("/addresses")
    public AddressDTO saveAddress(@RequestBody AddressDTO addressDTO) {
//        log.info("Saving address: {}", addressDTO);
        return userService.saveAddress(addressDTO);
    }

    @PostMapping("/add-address")
    public AddressDTO addAddress(@RequestBody AddressDTO addressDTO, @RequestHeader("Authorization") String jwt) {
        log.info("Saving address: {}", addressDTO);
        return userService.saveAddress(addressDTO);
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        AddressDTO address = userService.getAddressById(id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/address/{id}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable Long id, @RequestHeader("Authorization") String jwt) {
        AddressDTO address = userService.getAddressById(id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/all-address")
    public ResponseEntity<List<Address>> getAllAddress(@RequestHeader("Authorization") String jwt) {
        List<Address> address = userService.getAllAddress();
        return ResponseEntity.ok(address);
    }

    @PostMapping("/{userId}/addresses")
    public UserDto addAddressToUser(@PathVariable String userId, @RequestBody AddressDTO addressDTO) {
        return userService.addAddressToUser(userId, addressDTO);
    }


    @GetMapping("/addresses")
    ApiResponse<Set<Address>> getAllUserAddress(@RequestHeader("Authorization") String jwt) throws Exception {
        return ApiResponse.<Set<Address>>builder()
                .result(userService.getAllUserAddresses(jwt))
                .build();
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @RequestBody AddressDTO addressDTO) {
        AddressDTO updated = userService.updateAddress(id, addressDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/update-address/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable Long addressId,
            @RequestBody AddressDTO addressDTO,
            @RequestHeader("Authorization") String jwt) {
        AddressDTO updated = userService.updateAddress(addressId, addressDTO);
        return ResponseEntity.ok(ApiResponse.<AddressDTO>builder()
                .code(1000)
                .result(updated)
                .build());
    }

}
