package ru.skillbox.mc_account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import ru.skillbox.common.events.account.UserEvent;
import ru.skillbox.mc_account.DTO.*;
import ru.skillbox.mc_account.model.Account;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(target = "role", source = "account.role")
    @Mapping(target = "regDate", source = "account.regDate", qualifiedByName = "instantToString")
    @Mapping(target = "birthDate", source = "account.birthDate", qualifiedByName = "instantToString")
    @Mapping(target = "lastOnlineTime", source = "account.lastOnlineTime", qualifiedByName = "instantToString")
    @Mapping(target = "createdOn", source = "account.createdOn", qualifiedByName = "instantToString")
    @Mapping(target = "updatedOn", source = "account.updatedOn", qualifiedByName = "instantToString")
    @Mapping(target = "deletionTimestamp", source = "account.deletionTimestamp", qualifiedByName = "instantToString")
    @Mapping(target = "password", source = "account.password")
    AccountResponseDTO toResponseDTO(Account account);

    @Mapping(target = "regDate", source = "account.regDate", qualifiedByName = "instantToString")
    @Mapping(target = "birthDate", source = "account.birthDate", qualifiedByName = "instantToString")
    @Mapping(target = "lastOnlineTime", source = "account.lastOnlineTime", qualifiedByName = "instantToString")
    @Mapping(target = "createdOn", source = "account.createdOn", qualifiedByName = "instantToString")
    @Mapping(target = "updatedOn", source = "account.updatedOn", qualifiedByName = "instantToString")
    @Mapping(target = "deletionTimestamp", source = "account.deletionTimestamp", qualifiedByName = "instantToString")
    AccountMeDTO toDTO(Account account);

    Account toEntity(AccountMeDTO accountMeDTO);

    default AccountDataDTO toAccountDetailsDTO(Account account) {
        if (account == null) {
            return null;
        }
        AccountDataDTO accountDataDTO = new AccountDataDTO();
        accountDataDTO.setId(account.getId());
        accountDataDTO.setFirstName(account.getFirstName());
        accountDataDTO.setEmail(account.getEmail());
        accountDataDTO.setDeleted(account.isDeleted());
        return accountDataDTO;
    }

    default List<AuthorityDTO> mapAuthorities(Account account) {
        List<AuthorityDTO> authorities = new ArrayList<>();
        AuthorityDTO authorityDTO = new AuthorityDTO();
        authorityDTO.setId(account.getId());
        authorityDTO.setAuthority(account.getRole().name());
        authorities.add(authorityDTO);
        return authorities;
    }

    default AccountDataDTO toAccountDetailsDTOWithAuthorities(Account account) {
        if (account == null) {
            return null;
        }
        AccountDataDTO accountDataDTO = toAccountDetailsDTO(account);
        accountDataDTO.setAuthorities(mapAuthorities(account));
        return accountDataDTO;
    }


    @Mapping(target = "id", source = "account.id")
    @Mapping(target = "firstName", source = "account.firstName")
    @Mapping(target = "lastName", source = "account.lastName")
    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "password", source = "account.password")
    @Mapping(target = "messagePermission", source = "account.messagePermission")
    @Mapping(target = "deleted", source = "account.deleted")
    @Mapping(target = "blocked", source = "account.blocked")
    UserEvent toUserEvent(Account account);


    @Named("instantToString")
    default String instantToString(Instant instant) {
        return instant != null ? DateTimeFormatter.ISO_INSTANT.format(instant) : null;
    }
}
