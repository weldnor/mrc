package me.weldnor.mrc.controller.http;

import me.weldnor.mrc.domain.dto.login.LoginRequestDto;
import me.weldnor.mrc.domain.dto.user.NewUserDto;
import me.weldnor.mrc.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.kurento.jsonrpc.JsonUtils.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureDataMongo
class AuthControllerTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;


    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }


    @Test
    void registerAndLogin() throws Exception {
        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setEmail("example@gmail.com");
        newUserDto.setName("user");
        newUserDto.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(toJson(newUserDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("example@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("user"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty());

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("example@gmail.com");
        loginRequestDto.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("example@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("user"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty());
    }
}