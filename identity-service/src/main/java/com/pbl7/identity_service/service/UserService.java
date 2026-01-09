package com.pbl7.identity_service.service;


import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.pbl7.event.NotificationEvent;
import com.pbl7.identity_service.constant.PredefinedRole;
import com.pbl7.identity_service.dto.AddressDTO;
import com.pbl7.identity_service.dto.ApiResponse;
import com.pbl7.identity_service.dto.UserDto;
import com.pbl7.identity_service.dto.request.CreateCartRequest;
import com.pbl7.identity_service.dto.request.UserCreationRequest;
import com.pbl7.identity_service.dto.request.UserUpdateRequest;
import com.pbl7.identity_service.dto.request.VerifyEmailRequest;
import com.pbl7.identity_service.dto.response.CreateCartResponse;
import com.pbl7.identity_service.dto.response.UserResponse;
import com.pbl7.identity_service.entity.Address;
import com.pbl7.identity_service.entity.Role;
import com.pbl7.identity_service.entity.User;
import com.pbl7.identity_service.exception.AppException;
import com.pbl7.identity_service.exception.ErrorCode;
import com.pbl7.identity_service.mapper.AddressMapper;
import com.pbl7.identity_service.mapper.UserMapper;
import com.pbl7.identity_service.repository.AddressRepository;
import com.pbl7.identity_service.repository.RoleRepository;
import com.pbl7.identity_service.repository.UserRepository;
import com.pbl7.identity_service.repository.httpclient.CartClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    CartClient cartClient;
    KafkaTemplate<String, Object> kafkaTemplate;
    AuthenticationService authenticationService;
    HttpServletRequest httpServletRequest;
    EmailService emailService;
    AddressRepository addressRepository;
    AddressMapper addressMapper;

    public UserResponse createUser(UserCreationRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }


        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();

        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setEmailVerified(false);
        user.setEnabled(true);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        CreateCartRequest req = CreateCartRequest.builder()
                .userId(user.getId())
                .build();
        try {
            log.info("Creating cart with request: {}", req);
            ApiResponse<CreateCartResponse> cartResponse = cartClient.createCart(req);
            log.info("Cart creation response: {}", cartResponse);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.CART_CREATION_FAILED);
        }


        String token = authenticationService.generateToken(user);

        String verificationUrl = applicationUrl(httpServletRequest) + "/users/verify-email?token=" + token;
        String emailBody = emailService.generateEmailContent(request.getUsername(), verificationUrl);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(request.getEmail())
                .subject("Welcome to VGear")
                .body(emailBody)
                .build();

        kafkaTemplate.send("VGearVN", notificationEvent);

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }


    public UserResponse verifyEmail(String userId, VerifyEmailRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.verifyUserEmail(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public void disableUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void enableUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }


    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.toUserResponse(
                userRepository.findById(authentication.getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public AddressDTO getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        return addressMapper.toDto(address);
    }

    public List<Address> getAllAddress() {
        List<Address> address =  addressRepository.findAll();
        return address;
    }

    public User findUserById(String userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    public AddressDTO saveAddress(AddressDTO addressDTO) {
        Address address = addressMapper.toAddress(addressDTO);

        Optional<Address> existingAddress = addressRepository
                .findByNameAndLocalityAndAddressAndCityAndStateAndMobile(
                        address.getName(),
                        address.getLocality(),
                        address.getAddress(),
                        address.getCity(),
                        address.getState(),
                        address.getMobile()
                );

        if (existingAddress.isPresent()) {
            throw new AppException(ErrorCode.ADDRESS_ALREADY_EXISTS);
        }

        Address savedAddress = addressRepository.save(address);

        return addressMapper.toDto(savedAddress);
    }

    public UserDto addAddressToUser(String userId, AddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Address address = addressMapper.toAddress(addressDTO);
        address.setUser(user);
        addressRepository.save(address);
        user.setAddressIds(Collections.singletonList(address.getId()));
        return userMapper.toUserDto(user);
    }

    public String getIdFromJwt(String jwt) throws Exception {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        } else {
            throw new Exception("Invalid JWT token format");
        }

        JWSObject jwsObject = JWSObject.parse(jwt);

        Payload payload = jwsObject.getPayload();
        Map<String, Object> claimsMap = payload.toJSONObject();

        String userId = (String) claimsMap.get("sub");
        return userId;
    }

    public Set<Address> getAllUserAddresses(String jwt) throws Exception {

        String userId = getIdFromJwt(jwt);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return user.getShipAddress() != null ? user.getShipAddress() : new HashSet<>();
    }

    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        addressRepository.deleteById(id);
    }

    public AddressDTO updateAddress(Long id, AddressDTO addressDTO) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        // Cập nhật các trường
        address.setName(addressDTO.getName());
        address.setLocality(addressDTO.getLocality());
        address.setAddress(addressDTO.getAddress());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setMobile(addressDTO.getMobile());
        Address updated = addressRepository.save(address);
        return addressMapper.toDto(updated);
    }

}
