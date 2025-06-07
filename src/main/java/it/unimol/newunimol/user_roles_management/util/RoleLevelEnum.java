package it.unimol.newunimol.user_roles_management.util;

public enum RoleLevelEnum {
    STUDENT("student", 0),
    TEACHER("teach", 1),
    ADMIN("admin", 2),
    SUPER_ADMIN("sadmin", 3);

    private final String roleId;
    private final int level;

    RoleLevelEnum(String roleId, int level) {
        this.roleId = roleId;
        this.level = level;
    }

    public static RoleLevelEnum fromRoleName(String roleId) {
        for (RoleLevelEnum role : RoleLevelEnum.values()) {
            if (role.roleId.equalsIgnoreCase(roleId)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Ruolo non trovato.");
    }

    public int getLevel() {
        return this.level;
    }
}
