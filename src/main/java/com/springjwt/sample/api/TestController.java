package com.springjwt.sample.api;

import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.module.user.domain.entity.User;
import com.springjwt.sample.model.request.RequestDTO;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/getList")
    public ResponseEntity<PagedResult<User>> getListData() {
        Faker faker = new Faker(Locale.of("en-US"));
        // List to store user data
        List<User> userList = new ArrayList<>();

        // Create 10 fake User objects
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(faker.number().randomNumber());
            user.setUsername(faker.name().name());
            user.setPassword(faker.internet().password());
            user.setEmail(faker.internet().emailAddress());
            userList.add(user);
        }
        return ResponseFactory.pagedResponse(userList, 1, 10, userList.size());
    }

    /**
     * Java 15+: Text Blocks for better JSON documentation
     * Example request body:
     * <pre>{@code
     * {
     *   "customLocalDate": "2023/10/10",
     *   "localDate": "2023-10-10",
     *   "localDateTime": "2023-10-10T13:10:00",
     *   "zonedDateTime": "2023-10-10T13:10:00Z"
     * }
     * }</pre>
     *
     * @param requestDTO Request body containing date and time fields
     * @return ResponseEntity with the same RequestDTO
     */
    @PostMapping("/sendDate")
    public ResponseEntity<?> checkDateTime(@RequestBody RequestDTO requestDTO) {
        log.info("LocalDate: {}", requestDTO.getLocalDate());
        log.info("LocalDateTime: {}", requestDTO.getLocalDateTime());
        log.info("ZoneDateTime: {}", requestDTO.getZonedDateTime().toString());
        return ResponseFactory.success(requestDTO);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public String moderatorAccess() {
        return "Moderator Board.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
}
