package it.unimol.newunimol.user_roles_management.dto.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.util.Random;

import it.unimol.newunimol.user_roles_management.dto.UserCreationDto;
import it.unimol.newunimol.user_roles_management.dto.RoleDto;
import it.unimol.newunimol.user_roles_management.model.User;
import it.unimol.newunimol.user_roles_management.repository.UserRepository;
import it.unimol.newunimol.user_roles_management.service.RoleService;

@Component
public class UserCreationConverter implements Converter<UserCreationDto, User> {
    @Autowired
    private RoleConverter roleConverter;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRepository userRepository;

    private static final Random random = new Random();

    @Override
    public User convert(UserCreationDto source) {
        if (source == null) {
            return null;
        }

        RoleDto ruolo = roleService.findById(source.role());
        String randomId;
        do {
            randomId = String.valueOf(100000 + random.nextInt(900000));
        } while (userRepository.findById(randomId).isPresent());

        return new User(randomId, source.username(), source.email(), source.name(), source.surname(),
                source.password(), roleConverter.convert(ruolo));
    }
}
