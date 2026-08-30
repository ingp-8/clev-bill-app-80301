package com.clevstack.clevbill.support;

import com.clevstack.clevbill.model.Brand;
import com.clevstack.clevbill.model.Category;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Customer;
import com.clevstack.clevbill.model.HsnCode;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.ItemUnit;
import com.clevstack.clevbill.model.PosTerminal;
import com.clevstack.clevbill.model.PriceList;
import com.clevstack.clevbill.model.PriceListItem;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.Supplier;
import com.clevstack.clevbill.model.TaxRate;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.model.UserPropertyAccess;
import com.clevstack.clevbill.repository.BrandRepository;
import com.clevstack.clevbill.repository.CategoryRepository;
import com.clevstack.clevbill.repository.ClientRepository;
import com.clevstack.clevbill.repository.CustomerRepository;
import com.clevstack.clevbill.repository.HsnCodeRepository;
import com.clevstack.clevbill.repository.ItemRepository;
import com.clevstack.clevbill.repository.PosTerminalRepository;
import com.clevstack.clevbill.repository.PriceListItemRepository;
import com.clevstack.clevbill.repository.PriceListRepository;
import com.clevstack.clevbill.repository.PropertyRepository;
import com.clevstack.clevbill.repository.RoleRepository;
import com.clevstack.clevbill.repository.SupplierRepository;
import com.clevstack.clevbill.repository.TaxRateRepository;
import com.clevstack.clevbill.repository.UserPropertyAccessRepository;
import com.clevstack.clevbill.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Builds valid master/reference rows directly through repositories (never
 * through MockMvc) so a controller/service test can set up whatever
 * prerequisite graph it needs in a couple of lines instead of re-deriving
 * valid field values for every entity every time. Names are suffixed with
 * a per-JVM-run unique counter so parallel test methods sharing the one
 * Testcontainers Postgres instance never collide on a unique constraint
 * (e.g. tax_rates.name, clients.client_name) even though nothing here is
 * transactionally isolated from anything else until the owning test's
 * {@code @Transactional} rollback happens.
 */
@Component
public class TestEntityFactory {

    private static final AtomicLong SEQ = new AtomicLong();

    private final ClientRepository clientRepository;
    private final PropertyRepository propertyRepository;
    private final PosTerminalRepository posTerminalRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final TaxRateRepository taxRateRepository;
    private final HsnCodeRepository hsnCodeRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final PriceListRepository priceListRepository;
    private final PriceListItemRepository priceListItemRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserPropertyAccessRepository userPropertyAccessRepository;
    private final PasswordEncoder passwordEncoder;

    public TestEntityFactory(
            ClientRepository clientRepository,
            PropertyRepository propertyRepository,
            PosTerminalRepository posTerminalRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            TaxRateRepository taxRateRepository,
            HsnCodeRepository hsnCodeRepository,
            ItemRepository itemRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            PriceListRepository priceListRepository,
            PriceListItemRepository priceListItemRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            UserPropertyAccessRepository userPropertyAccessRepository,
            PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.propertyRepository = propertyRepository;
        this.posTerminalRepository = posTerminalRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.taxRateRepository = taxRateRepository;
        this.hsnCodeRepository = hsnCodeRepository;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.priceListRepository = priceListRepository;
        this.priceListItemRepository = priceListItemRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userPropertyAccessRepository = userPropertyAccessRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Monotonically increasing tag, unique for the life of the test JVM. */
    public static String unique(String prefix) {
        return prefix + "-" + SEQ.incrementAndGet();
    }

    public Client createClient() {
        return createClient(unique("Client"));
    }

    public Client createClient(String name) {
        Client client = new Client();
        client.setClientName(name);
        client.setActive(true);
        return clientRepository.save(client);
    }

    public Property createProperty(Client client) {
        return createProperty(client, unique("PROP"), false);
    }

    public Property createProperty(Client client, String seriesPrefix, boolean eInvoiceEnabled) {
        Property property = new Property();
        property.setClient(client);
        property.setPropertyName(unique("Property"));
        property.setAddress("1 Test Street");
        property.setGstin("27AAAAA0000A1Z5");
        property.setInvoiceSeriesPrefix(seriesPrefix);
        property.setDefaultCgstRate(new BigDecimal("9.00"));
        property.setDefaultSgstRate(new BigDecimal("9.00"));
        property.setDefaultIgstRate(new BigDecimal("18.00"));
        property.setEInvoiceEnabled(eInvoiceEnabled);
        property.setActive(true);
        return propertyRepository.save(property);
    }

    public PosTerminal createPosTerminal(Property property) {
        PosTerminal pos = new PosTerminal();
        pos.setProperty(property);
        pos.setClient(property.getClient());
        pos.setPosName(unique("POS"));
        pos.setActive(true);
        return posTerminalRepository.save(pos);
    }

    public Category createCategory(Property property) {
        Category category = new Category();
        category.setProperty(property);
        category.setClient(property.getClient());
        category.setName(unique("Category"));
        category.setActive(true);
        return categoryRepository.save(category);
    }

    public Brand createBrand(Property property) {
        Brand brand = new Brand();
        brand.setProperty(property);
        brand.setClient(property.getClient());
        brand.setName(unique("Brand"));
        brand.setActive(true);
        return brandRepository.save(brand);
    }

    public TaxRate createTaxRate(Property property) {
        return createTaxRate(property, new BigDecimal("9.00"), new BigDecimal("9.00"), new BigDecimal("18.00"));
    }

    public TaxRate createTaxRate(Property property, BigDecimal cgst, BigDecimal sgst, BigDecimal igst) {
        TaxRate taxRate = new TaxRate();
        taxRate.setProperty(property);
        taxRate.setClient(property.getClient());
        taxRate.setName(unique("GST"));
        taxRate.setCgstRate(cgst);
        taxRate.setSgstRate(sgst);
        taxRate.setIgstRate(igst);
        taxRate.setActive(true);
        return taxRateRepository.save(taxRate);
    }

    public HsnCode createHsnCode(Property property) {
        HsnCode hsnCode = new HsnCode();
        hsnCode.setProperty(property);
        hsnCode.setClient(property.getClient());
        hsnCode.setCode(String.valueOf(100000 + SEQ.incrementAndGet() % 900000));
        hsnCode.setDescription("Test HSN");
        hsnCode.setActive(true);
        return hsnCodeRepository.save(hsnCode);
    }

    public Item createItem(Property property, TaxRate taxRate, HsnCode hsnCode) {
        return createItem(property, taxRate, hsnCode, new BigDecimal("100.00"));
    }

    public Item createItem(Property property, TaxRate taxRate, HsnCode hsnCode, BigDecimal sellingPrice) {
        Item item = new Item();
        item.setProperty(property);
        item.setClient(property.getClient());
        item.setSku(unique("SKU"));
        item.setBarcode(unique("BAR"));
        item.setName(unique("Item"));
        item.setTaxRate(taxRate);
        item.setHsnCode(hsnCode);
        item.setUnit(ItemUnit.PCS);
        item.setSellingPrice(sellingPrice);
        item.setCostPrice(sellingPrice.multiply(new BigDecimal("0.6")));
        item.setActive(true);
        return itemRepository.save(item);
    }

    public Customer createCustomer(Client client) {
        return createCustomer(client, null);
    }

    public Customer createCustomer(Client client, String gstin) {
        Customer customer = new Customer();
        customer.setClient(client);
        customer.setName(unique("Customer"));
        customer.setPhone("9000000000");
        customer.setGstin(gstin);
        customer.setActive(true);
        return customerRepository.save(customer);
    }

    public Supplier createSupplier(Client client) {
        Supplier supplier = new Supplier();
        supplier.setClient(client);
        supplier.setName(unique("Supplier"));
        supplier.setPhone("9000000001");
        supplier.setActive(true);
        return supplierRepository.save(supplier);
    }

    public PriceList createPriceList(Property property) {
        PriceList priceList = new PriceList();
        priceList.setProperty(property);
        priceList.setClient(property.getClient());
        priceList.setName(unique("PriceList"));
        priceList.setDefault(false);
        priceList.setActive(true);
        return priceListRepository.save(priceList);
    }

    public PriceListItem createPriceListItem(PriceList priceList, Item item, BigDecimal price) {
        PriceListItem priceListItem = new PriceListItem();
        priceListItem.setPriceList(priceList);
        priceListItem.setItem(item);
        priceListItem.setPrice(price);
        return priceListItemRepository.save(priceListItem);
    }

    public Role role(String roleName) {
        return roleRepository
                .findByRoleName(roleName)
                .orElseThrow(() -> new IllegalStateException("Seeded role missing: " + roleName));
    }

    /** Creates a user with the given seeded role(s) (e.g. Role.SUPER_ADMIN, "Store Manager", "Operator"). */
    public User createUser(String username, String rawPassword, String... roleNames) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(username);
        user.setEnabled(true);
        Set<Role> roles = new java.util.HashSet<>();
        for (String roleName : roleNames) {
            roles.add(role(roleName));
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public UserPropertyAccess grantAccess(User user, Property property) {
        UserPropertyAccess access = new UserPropertyAccess();
        access.setUser(user);
        access.setProperty(property);
        access.setClient(property.getClient());
        return userPropertyAccessRepository.save(access);
    }
}
