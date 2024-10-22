package com.searchoptimizationv2.search_optimization_application_v2.Service;

import com.optimization_component.payload.Filter;
import com.optimization_component.service.ElasticSearchServiceImpl;
import com.searchoptimizationv2.search_optimization_application_v2.DTOs.UserDTO;
import com.searchoptimizationv2.search_optimization_application_v2.DTOs.UserPasswordUpdateRequest;
import com.searchoptimizationv2.search_optimization_application_v2.entities.User;
import com.searchoptimizationv2.search_optimization_application_v2.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private ElasticSearchServiceImpl<User> elasticSearchService;

    public String createUser(UserDTO userDTO) throws IOException {

        User user = userRepository.findByUsername(userDTO.getUsername());
        if(user != null) {
            return "User already exists";
        }

        user = User.builder()
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .username(userDTO.getUsername())
                .age(userDTO.getAge())
                .status(userDTO.getStatus())
                .groupId(userDTO.getGroupId())
                .build();


        User userFromDB = userRepository.save(user);

        //==========================================================================
        elasticSearchService.save(User.class.getName().toLowerCase(), userFromDB.getUserId().toString(), userFromDB);
        //==========================================================================
        return "User created successfully";
    }

    public String updateUserPassword(UserPasswordUpdateRequest userPasswordUpdateRequest) throws IOException {

        String userIdString = userPasswordUpdateRequest.getUserIdString();
        String newPassword = userPasswordUpdateRequest.getNewPassword();

        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId).orElseThrow();
        user.setPassword(newPassword);
        userRepository.save(user);

        //==========================================================================
        elasticSearchService.save(User.class.getName().toLowerCase(), user.getUserId().toString(), user);
        //==========================================================================

        return "Password updated successfully";
    }

    public String deleteUser(String userIdString) throws IOException {
        UUID userId = UUID.fromString(userIdString);
        userRepository.deleteById(userId);

        //==========================================================================
        elasticSearchService.delete(User.class.getName().toLowerCase(), userIdString);
        //==========================================================================

        return "User deleted successfully";
    }

    public User getUser(String userIdString){
        UUID userId = UUID.fromString(userIdString);
        return userRepository.findById(userId).orElseThrow();
    }

    public List<User> searchUser(Filter filter) throws Exception {
        return elasticSearchService.search(User.class.getName().toLowerCase(), filter);
    }
}
