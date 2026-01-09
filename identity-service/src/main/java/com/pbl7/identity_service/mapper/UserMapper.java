package com.pbl7.identity_service.mapper;


import com.pbl7.identity_service.dto.UserDto;
import com.pbl7.identity_service.dto.request.UserCreationRequest;
import com.pbl7.identity_service.dto.request.UserUpdateRequest;
import com.pbl7.identity_service.dto.request.VerifyEmailRequest;
import com.pbl7.identity_service.dto.response.UserResponse;
import com.pbl7.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    @Mapping(source = "enabled", target = "isEnabled")
    UserResponse toUserResponse(User user);

    UserDto toUserDto(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    void verifyUserEmail(@MappingTarget User user, VerifyEmailRequest request);
}
//    @Mapping(target = '' , ignore = true) ko mapping field nay
