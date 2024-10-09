package ru.skillbox.mc_account.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class AuthorityDTO {
    private UUID id;
    private String authority;
}
