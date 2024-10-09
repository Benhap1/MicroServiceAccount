package ru.skillbox.mc_account.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AccountDataDTO {
    private UUID id;
    private String firstName;
    private String email;
    private String password;
    private String roles;
    private List<AuthorityDTO> authorities;
    private boolean deleted;
}
