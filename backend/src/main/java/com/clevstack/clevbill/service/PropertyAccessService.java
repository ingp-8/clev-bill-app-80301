package com.clevstack.clevbill.service;

import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.model.UserPropertyAccess;
import com.clevstack.clevbill.repository.PropertyRepository;
import com.clevstack.clevbill.repository.UserPropertyAccessRepository;
import com.clevstack.clevbill.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Answers "which properties can this user see/operate on" — Super Admin
 * bypasses {@code user_property_access} entirely and sees everything (see
 * UserPropertyAccess's javadoc); every other role is limited to whatever
 * rows it has.
 */
@Service
@Transactional(readOnly = true)
public class PropertyAccessService {

    private final UserPropertyAccessRepository userPropertyAccessRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyAccessService(
            UserPropertyAccessRepository userPropertyAccessRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository) {
        this.userPropertyAccessRepository = userPropertyAccessRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Looks the user up and checks access in one transaction — for callers
     * (like PropertyAccessInterceptor) that only have a username and run
     * outside any existing transaction, so a User fetched separately would
     * be detached by the time {@code hasAccess} tried to read its lazy
     * {@code roles} collection.
     */
    public boolean hasAccessByUsername(String username, Long propertyId) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user == null || hasAccess(user, propertyId);
    }

    public boolean isSuperAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName().equals(Role.SUPER_ADMIN));
    }

    public List<Property> getAccessibleProperties(User user) {
        if (isSuperAdmin(user)) {
            return propertyRepository.findAll();
        }
        return userPropertyAccessRepository.findByUserId(user.getId()).stream()
                .map(UserPropertyAccess::getProperty)
                .toList();
    }

    public boolean hasAccess(User user, Long propertyId) {
        if (isSuperAdmin(user)) {
            return true;
        }
        return userPropertyAccessRepository.existsByUserIdAndPropertyId(user.getId(), propertyId);
    }
}
