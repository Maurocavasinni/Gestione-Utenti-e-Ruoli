package it.unimol.newunimol.user_roles_management.dto.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import it.unimol.newunimol.user_roles_management.dto.UserDto;
import it.unimol.newunimol.user_roles_management.model.User;

@Component
public class UserConverter implements Converter<UserDto, User> {
    @Autowired
    RoleConverter roleConverter;

    @Override
    public User convert(@NonNull UserDto source) {

        return new User(source.id(), source.username(), source.email(), source.name(), source.surname(),
                source.password(), source.creationDate(), source.lastLogin(), roleConverter.convert(source.ruolo()));
    }

    public UserDto toDto(User source) {
        if (source == null) {
            return null;
        }

        return new UserDto(source.getId(), source.getUsername(), source.getEmail(), source.getName(), source.getSurname(),
                source.getPassword(), source.getCreationDate(), source.getLastLogin(), roleConverter.toDto(source.getRole()));
    }
}
