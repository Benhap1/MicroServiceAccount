package ru.skillbox.mc_account.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class AccountMeDTO {

    private UUID id;

    @NotBlank(message = "First name must not be blank")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;
    private String phone;
    private String photo;
    private String profileCover;
    private String about;
    private String city;
    private String country;
    private String statusCode;
    private Instant regDate;
    private Instant birthDate;
    private String messagePermission;
    private Instant lastOnlineTime;
    private String emojiStatus;
    private Instant createdOn;
    private Instant updatedOn;
    private Instant deletionTimestamp;
    private boolean deleted;
    private boolean online;
    private boolean blocked;

}

