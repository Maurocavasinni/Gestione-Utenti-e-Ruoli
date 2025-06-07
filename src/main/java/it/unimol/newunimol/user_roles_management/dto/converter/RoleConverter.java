package it.unimol.newunimol.user_roles_management.dto.converter;

import it.unimol.newunimol.user_roles_management.dto.RoleDto;
import it.unimol.newunimol.user_roles_management.model.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter implements Converter<RoleDto, Role> {

    @Override
    public Role convert(RoleDto source) {
        if (source == null) {
            return null;
        }

        return new Role(source.id(), source.nome(), source.descrizione());
    }

    public RoleDto toDto(Role source) {
        if (source == null) {
            return null;
        }

        return new RoleDto(source.getId(), source.getNome(), source.getDescrizione());
    }
}
