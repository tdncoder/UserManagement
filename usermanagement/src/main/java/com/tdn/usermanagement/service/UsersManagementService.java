package com.tdn.usermanagement.service;

import com.tdn.usermanagement.dto.RequestResponse;
import com.tdn.usermanagement.entity.Users;
import com.tdn.usermanagement.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UsersManagementService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public RequestResponse register(RequestResponse registrationRequest){
        RequestResponse response = new RequestResponse();
        try {
            Users users = new Users();
            users.setEmail(registrationRequest.getEmail());
            users.setCity(registrationRequest.getCity());
            users.setRole(registrationRequest.getRole());
            users.setName(registrationRequest.getName());
            users.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            Users usersResult = usersRepository.save(users);
            if (usersResult.getId() > 0){
                response.setUsers((usersResult));
                response.setMessage("User saved successfully");
                response.setStatusCode(200);
            }

        } catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public RequestResponse login(RequestResponse loginRequest){
        RequestResponse response = new RequestResponse();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword()));
            var user = usersRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(),user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24 Hours");
            response.setMessage("Successfully Logged In");
        } catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public RequestResponse refreshToken(RequestResponse refreshTokenRequest){
        RequestResponse response = new RequestResponse();
        try{
            String userEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            Users users = usersRepository.findByEmail(userEmail).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), users)){
                var jwt = jwtUtils.generateToken(users);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenRequest.getToken());
                response.setExpirationTime("24 Hours");
                response.setMessage("Successfully Refresh Token");
            }
            response.setStatusCode(200);
            return response;
        } catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public RequestResponse getAllUsers(){
        RequestResponse response = new RequestResponse();
        try {
            List<Users> result = usersRepository.findAll();
            if (!result.isEmpty()){
                response.setUsersList(result);
                response.setStatusCode(200);
                response.setMessage("Successful");
            } else {
              response.setStatusCode(404);
              response.setMessage("No Users Found");
            }
            return response;
        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error occurred: " + e.getMessage());
            return response;
        }
    }

    public RequestResponse getUsersById(Integer id){
        RequestResponse response = new RequestResponse();
        try{
            Users usersById = usersRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            response.setUsers(usersById);
            response.setStatusCode(200);
            response.setMessage("User with id: " + id + " - found successfully");
        } catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error occurred: " + e.getMessage());
        }
        return response;
    }

    public RequestResponse deleteUser(Integer id){
        RequestResponse response = new RequestResponse();
        try {
            Optional<Users> usersOptional = usersRepository.findById(id);
            if (usersOptional.isPresent()){
                usersRepository.deleteById(id);
                response.setStatusCode(200);
                response.setMessage("User deleted successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("User not found for deletion");
            }
        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error occurred while deleting user: " + e.getMessage());
        }
        return response;
    }

    public RequestResponse updateUser(Integer id, Users updatedUser){
        RequestResponse response = new RequestResponse();
        try {
            Optional<Users> usersOptional = usersRepository.findById(id);
            if (usersOptional.isPresent()) {
                Users existingUser = usersOptional.get();
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setName(updatedUser.getName());
                existingUser.setCity(updatedUser.getCity());
                existingUser.setRole(updatedUser.getRole());

                // Check if password is present in the request
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()){
                    // Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }

                Users savedUser = usersRepository.save(existingUser);
                response.setUsers(savedUser);
                response.setStatusCode(200);
                response.setMessage("User updated successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("User not found for update");
            }

        } catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error occurred while updating user: " + e.getMessage());
        }
        return response;
    }

    public RequestResponse getMyInfo(String email){
        RequestResponse response = new RequestResponse();
        try {
            Optional<Users> usersOptional = usersRepository.findByEmail(email);
            if (usersOptional.isPresent()) {
                response.setUsers(usersOptional.get());
                response.setStatusCode(200);
                response.setMessage("Successful");
            } else {
                response.setStatusCode(404);
                response.setMessage("User not found for update");
            }
        } catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting user: " + e.getMessage());
        }
        return response;
    }
}
