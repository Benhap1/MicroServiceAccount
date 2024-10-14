package ru.skillbox.mc_account.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String messagePermission;
    private boolean deleted;
    private boolean blocked;
}
