package com.pharmacore.pos.service;

import com.pharmacore.pos.entity.Role;
import com.pharmacore.pos.entity.Supplier;
import com.pharmacore.pos.entity.User;
import com.pharmacore.pos.repository.RoleRepository;
import com.pharmacore.pos.repository.SupplierRepository;
import com.pharmacore.pos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            SupplierRepository supplierRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    public void run(String... args) {
        initRolesAndUsers();
        initSuppliers();
    }

    private void initRolesAndUsers() {
        Role adminRole = getOrCreateRole("ROLE_ADMIN", "System Administrator & General Manager");
        Role pharmRole = getOrCreateRole("ROLE_PHARMACIST", "Licensed Dispensing Pharmacist");
        Role cashierRole = getOrCreateRole("ROLE_CASHIER", "Counter POS Cashier");

        // Seed Staff
        createStaffIfAbsent("admin", "System Administrator", adminRole, "admin@pharmacore.com", "0700000000", "1234");
        createStaffIfAbsent("carson", "Carson Sila (Store Manager)", adminRole, "carson@pharmacore.com", "0712345678", "0000");
        createStaffIfAbsent("jane", "Jane Wanjiku (Pharmacist 1)", pharmRole, "jane@pharmacore.com", "0722112233", "1111");
        createStaffIfAbsent("david", "David Kamau (Pharmacist 2)", pharmRole, "david@pharmacore.com", "0733445566", "2222");
        createStaffIfAbsent("faith", "Faith Mutua (Cashier)", cashierRole, "faith@pharmacore.com", "0799887766", "3333");
    }

    private Role getOrCreateRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = Role.builder().name(name).description(description).build();
            return roleRepository.save(r);
        });
    }

    private void createStaffIfAbsent(String username, String fullName, Role role, String email, String phone, String pin) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isEmpty()) {
            User user = User.builder()
                    .username(username)
                    .fullName(fullName)
                    .role(role)
                    .email(email)
                    .phone(phone)
                    .passwordHash("123456")
                    .pinCode(pin)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info("Initialized default staff member: {} ({}) with PIN {}", username, role.getName(), pin);
        }
    }

    private void initSuppliers() {
        createSupplierIfAbsent("Harleys Ltd", "Suresh Patel", "+254 722 000111", "orders@harleys.co.ke", "P051123456Z", "Commercial St, Industrial Area", "Net 30");
        createSupplierIfAbsent("Laborex Kenya", "Grace Njoroge", "+254 733 111222", "sales@laborex-kenya.com", "P051987654A", "Mombasa Road, Nairobi", "Net 15");
        createSupplierIfAbsent("Cosmos Pharmaceuticals", "Dr. Benson Ochieng", "+254 720 333444", "info@cosmos-pharm.com", "P051555666C", "Rangwe Rd, Off Lunga Lunga Rd", "COD");
    }

    private void createSupplierIfAbsent(String name, String contact, String phone, String email, String pin, String address, String terms) {
        if (supplierRepository.findByNameIgnoreCase(name).isEmpty()) {
            Supplier s = Supplier.builder()
                    .name(name)
                    .contactPerson(contact)
                    .phone(phone)
                    .email(email)
                    .kraPin(pin)
                    .address(address)
                    .paymentTerms(terms)
                    .isActive(true)
                    .build();
            supplierRepository.save(s);
            log.info("Initialized default supplier: {}", name);
        }
    }
}

