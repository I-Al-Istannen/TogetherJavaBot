package org.togetherjava.permission;

import com.moandjiezana.toml.Toml;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

/**
 * Manages user permissions.
 */
public class PermissionManager {

  private Toml config;

  /**
   * Creates a new permission manager.
   *
   * @param config the config
   */
  public PermissionManager(Toml config) {
    this.config = config;
  }

  /**
   * Checks whether a user has the given permission.
   *
   * @param user the user
   * @param permission the permission. Blank permissions are always granted.
   * @return true if the user has the needed permission
   */
  public boolean hasPermission(Member user, String permission) {
    if (permission.isBlank()) {
      return true;
    }
    Toml permissions = config.getTable("permissions");
    for (Role role : user.getRoles()) {

      String key = "id_" + role.getId();

      if (permissions.contains(key)) {
        if (permissions.getList(key).contains(permission)) {
          return true;
        }
      }
    }
    return false;
  }
}
