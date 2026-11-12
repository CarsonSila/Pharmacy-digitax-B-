package com.pharmacore.pos.service;

import com.pharmacore.pos.entity.*;
import com.pharmacore.pos.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final TaxClassificationRepository taxClassificationRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductUnitConversionRepository productUnitConversionRepository;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            TaxClassificationRepository taxClassificationRepository,
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository,
            ProductUnitConversionRepository productUnitConversionRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.taxClassificationRepository = taxClassificationRepository;
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.productUnitConversionRepository = productUnitConversionRepository;
    }

    @Override
    public void run(String... args) {
        initReferenceData();
        initMedicinesCatalog();
        initProductUnitConversions();
        initRolesAndUsers();
        initSuppliers();
    }

    // -------------------------------------------------------------------------
    // Reference Data: Categories, UOMs, Tax Classifications
    // -------------------------------------------------------------------------

    private void initReferenceData() {
        // Categories
        getOrCreateCategory("ANTIBIOTICS",   "Antibiotics & Antimicrobials");
        getOrCreateCategory("ANALGESICS",    "Pain Relief & Anti-inflammatory");
        getOrCreateCategory("CARDIO",        "Cardiovascular Medicines");
        getOrCreateCategory("DIABETES",      "Diabetes & Endocrine");
        getOrCreateCategory("ANTIMALARIALS", "Antimalarial Medicines");
        getOrCreateCategory("RESPIRATORY",   "Respiratory & Allergy");
        getOrCreateCategory("GASTRO",        "Gastrointestinal Medicines");
        getOrCreateCategory("VITAMINS",      "Vitamins, Supplements & OTC");
        getOrCreateCategory("GYNAECOLOGY",   "Women's Health & Gynaecology");
        getOrCreateCategory("GENERAL",       "General & Miscellaneous");

        // Units of Measure
        getOrCreateUom("Tablet",                "tab");
        getOrCreateUom("Capsule",               "cap");
        getOrCreateUom("Strip (10 Tabs)",       "strip");
        getOrCreateUom("Strip (6 Tabs)",        "strip");
        getOrCreateUom("Pack (100 Tabs)",       "pack");
        getOrCreateUom("Pack (14 Tabs)",        "pack");
        getOrCreateUom("Pack (24 Tabs)",        "pack");
        getOrCreateUom("Pack (30 Tabs)",        "pack");
        getOrCreateUom("Bottle",                "btl");
        getOrCreateUom("Sachet",                "sachet");
        getOrCreateUom("Vial",                  "vial");
        getOrCreateUom("Inhaler",               "inhaler");
        getOrCreateUom("Cream/Ointment (Tube)", "tube");
        getOrCreateUom("Suspension (100ml)",    "susp");

        // Tax Classifications
        getOrCreateTaxClass("EXEMPT",     "VAT Exempt Medicines",         new BigDecimal("0.00"),  "E", "Prescription & essential medicines exempt per KRA eTIMS");
        getOrCreateTaxClass("ZERO_RATED", "Zero-Rated Medical Supplies",  new BigDecimal("0.00"),  "B", "Zero-rated medical and surgical supplies");
        getOrCreateTaxClass("VAT_16",     "Standard VAT 16%",             new BigDecimal("16.00"), "A", "Standard-rated OTC supplements and cosmetics");
    }

    private Category getOrCreateCategory(String code, String name) {
        return categoryRepository.findByCode(code).orElseGet(() -> {
            Category c = Category.builder().code(code).name(name).build();
            Category saved = categoryRepository.save(c);
            log.info("Initialized category: {} - {}", code, name);
            return saved;
        });
    }

    private UnitOfMeasure getOrCreateUom(String name, String symbol) {
        return unitOfMeasureRepository.findByName(name).orElseGet(() -> {
            UnitOfMeasure u = UnitOfMeasure.builder().name(name).symbol(symbol).build();
            UnitOfMeasure saved = unitOfMeasureRepository.save(u);
            log.info("Initialized unit of measure: {} ({})", name, symbol);
            return saved;
        });
    }

    private TaxClassification getOrCreateTaxClass(String classCode, String taxName, BigDecimal taxRate,
                                                   String kraTaxCode, String legalDescription) {
        return taxClassificationRepository.findByClassCode(classCode).orElseGet(() -> {
            TaxClassification t = TaxClassification.builder()
                    .classCode(classCode)
                    .taxName(taxName)
                    .taxRate(taxRate)
                    .kraTaxCode(kraTaxCode)
                    .legalDescription(legalDescription)
                    .build();
            TaxClassification saved = taxClassificationRepository.save(t);
            log.info("Initialized tax classification: {} - {}", classCode, taxName);
            return saved;
        });
    }

    // -------------------------------------------------------------------------
    // Medicines Catalog: ~55 products + initial batches
    // -------------------------------------------------------------------------

    private void initMedicinesCatalog() {
        LocalDate expiry = LocalDate.now().plusYears(2);
        int[] batchCounter = {1};

        // Helper lambdas for lookup
        java.util.function.Function<String, Category> cat =
                code -> categoryRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalStateException("Category not found: " + code));
        java.util.function.Function<String, UnitOfMeasure> uom =
                name -> unitOfMeasureRepository.findByName(name)
                        .orElseThrow(() -> new IllegalStateException("UOM not found: " + name));
        java.util.function.Function<String, TaxClassification> tax =
                code -> taxClassificationRepository.findByClassCode(code)
                        .orElseThrow(() -> new IllegalStateException("Tax class not found: " + code));

        // ---- ANALGESICS ----
        seedProduct(batchCounter, expiry, "Panadol 500mg",           "Paracetamol 500mg",              "ANALGESICS",  "PCM-500",   "3004.90.00", "EXEMPT",  "2.00",   "5.00",   "Tablet",           500, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Panadol Extra",           "Paracetamol+Caffeine",           "ANALGESICS",  "PAN-EXT",   "3004.90.00", "EXEMPT",  "18.00",  "35.00",  "Strip (10 Tabs)",  100, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Brufen 400mg",            "Ibuprofen 400mg",                "ANALGESICS",  "IBU-400",   "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           300, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Voltaren 50mg",           "Diclofenac Sodium 50mg",         "ANALGESICS",  "DCF-50",    "3004.90.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Aspirin 300mg",           "Acetylsalicylic Acid 300mg",     "ANALGESICS",  "ASP-300",   "3004.90.00", "EXEMPT",  "2.00",   "5.00",   "Tablet",           400, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Tramadol 50mg",           "Tramadol HCl 50mg",              "ANALGESICS",  "TRM-50",    "3004.90.00", "EXEMPT",  "15.00",  "35.00",  "Capsule",          100, "Cosmos Pharmaceuticals", cat, uom, tax);

        // ---- ANTIBIOTICS ----
        seedProduct(batchCounter, expiry, "Amoxil 500mg",            "Amoxicillin 500mg",              "ANTIBIOTICS", "AMX-500",   "3004.10.00", "EXEMPT",  "8.00",   "18.00",  "Capsule",          300, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Augmentin 625mg",         "Amoxicillin+Clavulanate 625mg",  "ANTIBIOTICS", "AUG-625",   "3004.10.00", "EXEMPT",  "55.00",  "110.00", "Tablet",           150, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Zithromax 500mg",         "Azithromycin 500mg",             "ANTIBIOTICS", "AZT-500",   "3004.10.00", "EXEMPT",  "45.00",  "95.00",  "Tablet",           100, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Ciproxin 500mg",          "Ciprofloxacin 500mg",            "ANTIBIOTICS", "CPX-500",   "3004.10.00", "EXEMPT",  "18.00",  "38.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Flagyl 400mg",            "Metronidazole 400mg",            "ANTIBIOTICS", "MTZ-400",   "3004.10.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           300, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Doxycycline 100mg",       "Doxycycline HCl 100mg",          "ANTIBIOTICS", "DOX-100",   "3004.10.00", "EXEMPT",  "12.00",  "28.00",  "Capsule",          150, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Septrin 480mg",           "Cotrimoxazole 480mg",            "ANTIBIOTICS", "CTX-480",   "3004.10.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           250, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Erythromycin 250mg",      "Erythromycin Stearate 250mg",    "ANTIBIOTICS", "ERY-250",   "3004.10.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Cloxacillin 500mg",       "Cloxacillin Sodium 500mg",       "ANTIBIOTICS", "CLX-500",   "3004.10.00", "EXEMPT",  "12.00",  "28.00",  "Capsule",          100, "Cosmos Pharmaceuticals", cat, uom, tax);

        // ---- ANTIMALARIALS ----
        seedProduct(batchCounter, expiry, "Coartem 20/120mg",        "Artemether/Lumefantrine",        "ANTIMALARIALS","COAR-6",   "3004.60.00", "EXEMPT",  "320.00", "550.00", "Strip (10 Tabs)",   50, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Chloroquine 250mg",       "Chloroquine Phosphate 250mg",    "ANTIMALARIALS","CHL-250",  "3004.60.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           150, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Quinine 300mg",           "Quinine Sulphate 300mg",         "ANTIMALARIALS","QUI-300",  "3004.60.00", "EXEMPT",  "12.00",  "28.00",  "Tablet",           100, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Fansidar",                "Sulfadoxine+Pyrimethamine",      "ANTIMALARIALS","FAN-SP",   "3004.60.00", "EXEMPT",  "25.00",  "55.00",  "Tablet",            80, "Cosmos Pharmaceuticals", cat, uom, tax);

        // ---- CARDIO ----
        seedProduct(batchCounter, expiry, "Amlodipine 5mg",          "Amlodipine Besylate 5mg",        "CARDIO",      "AML-5",     "3004.90.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Lisinopril 10mg",         "Lisinopril 10mg",                "CARDIO",      "LIS-10",    "3004.90.00", "EXEMPT",  "10.00",  "22.00",  "Tablet",           180, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Atenolol 50mg",           "Atenolol 50mg",                  "CARDIO",      "ATN-50",    "3004.90.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           180, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Losartan 50mg",           "Losartan Potassium 50mg",        "CARDIO",      "LOS-50",    "3004.90.00", "EXEMPT",  "18.00",  "38.00",  "Tablet",           150, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Simvastatin 20mg",        "Simvastatin 20mg",               "CARDIO",      "SMV-20",    "3004.90.00", "EXEMPT",  "12.00",  "28.00",  "Tablet",           150, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Atorvastatin 20mg",       "Atorvastatin Calcium 20mg",      "CARDIO",      "ATV-20",    "3004.90.00", "EXEMPT",  "18.00",  "40.00",  "Tablet",           120, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Hydrochlorothiazide 25mg","Hydrochlorothiazide 25mg",       "CARDIO",      "HCT-25",    "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           180, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Furosemide 40mg",         "Furosemide 40mg",                "CARDIO",      "FUR-40",    "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           180, "Harleys Ltd",           cat, uom, tax);

        // ---- DIABETES ----
        seedProduct(batchCounter, expiry, "Metformin 500mg",         "Metformin HCl 500mg",            "DIABETES",    "MET-500",   "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           300, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Metformin 850mg",         "Metformin HCl 850mg",            "DIABETES",    "MET-850",   "3004.90.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Glibenclamide 5mg",       "Glibenclamide 5mg",              "DIABETES",    "GLB-5",     "3004.90.00", "EXEMPT",  "3.00",   "8.00",   "Tablet",           250, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Glimepiride 2mg",         "Glimepiride 2mg",                "DIABETES",    "GLM-2",     "3004.90.00", "EXEMPT",  "12.00",  "28.00",  "Tablet",           150, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Insulin Mixtard 30/70",   "Biphasic Insulin 30IU/70IU",     "DIABETES",    "INS-MIX",   "3004.31.00", "EXEMPT",  "450.00", "850.00", "Vial",              20, "Laborex Kenya",         cat, uom, tax);

        // ---- RESPIRATORY ----
        seedProduct(batchCounter, expiry, "Salbutamol Inhaler",      "Salbutamol Sulphate 100mcg",     "RESPIRATORY", "SAL-INH",   "3004.90.00", "EXEMPT",  "180.00", "380.00", "Inhaler",           30, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Prednisolone 5mg",        "Prednisolone 5mg",               "RESPIRATORY", "PRD-5",     "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Cetirizine 10mg",         "Cetirizine HCl 10mg",            "RESPIRATORY", "CTZ-10",    "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           250, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Loratadine 10mg",         "Loratadine 10mg",                "RESPIRATORY", "LOR-10",    "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           250, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Beclomethasone Inhaler",  "Beclomethasone 100mcg",          "RESPIRATORY", "BECL-INH",  "3004.90.00", "EXEMPT",  "450.00", "850.00", "Inhaler",           15, "Laborex Kenya",         cat, uom, tax);

        // ---- GASTRO ----
        seedProduct(batchCounter, expiry, "Omeprazole 20mg",         "Omeprazole 20mg",                "GASTRO",      "OMP-20",    "3004.90.00", "EXEMPT",  "12.00",  "28.00",  "Capsule",          250, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Ranitidine 150mg",        "Ranitidine HCl 150mg",           "GASTRO",      "RAN-150",   "3004.90.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Domperidone 10mg",        "Domperidone 10mg",               "GASTRO",      "DOM-10",    "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           200, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "ORS Sachet",              "Oral Rehydration Salts",         "GASTRO",      "ORS-SAC",   "3004.90.00", "EXEMPT",  "8.00",   "20.00",  "Sachet",           150, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Buscopan 10mg",           "Hyoscine Butylbromide 10mg",     "GASTRO",      "BSC-10",    "3004.90.00", "EXEMPT",  "8.00",   "18.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Loperamide 2mg",          "Loperamide HCl 2mg",             "GASTRO",      "LPR-2",     "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Capsule",          180, "Harleys Ltd",           cat, uom, tax);

        // ---- GYNAECOLOGY ----
        seedProduct(batchCounter, expiry, "Folic Acid 5mg",          "Folic Acid 5mg",                 "GYNAECOLOGY", "FOL-5",     "3004.90.00", "EXEMPT",  "3.00",   "8.00",   "Tablet",           300, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Ferrous Sulphate+Folic Acid","Iron+Folic Acid",             "GYNAECOLOGY", "FER-FOL",   "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           250, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Fluconazole 150mg",       "Fluconazole 150mg",              "GYNAECOLOGY", "FLC-150",   "3004.90.00", "EXEMPT",  "55.00",  "120.00", "Capsule",           80, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Clotrimazole Cream",      "Clotrimazole 1% Cream",          "GYNAECOLOGY", "CLT-CRM",   "3004.90.00", "EXEMPT",  "55.00",  "120.00", "Cream/Ointment (Tube)", 40, "Harleys Ltd",      cat, uom, tax);

        // ---- VITAMINS ----
        seedProduct(batchCounter, expiry, "Vitamin C 500mg",         "Ascorbic Acid 500mg",            "VITAMINS",    "VITC-500",  "2106.90.00", "VAT_16",  "8.00",   "20.00",  "Tablet",           200, "Laborex Kenya",         cat, uom, tax);
        seedProduct(batchCounter, expiry, "Zinc Sulphate 20mg",      "Zinc Sulphate 20mg",             "VITAMINS",    "ZNC-20",    "3004.90.00", "EXEMPT",  "5.00",   "12.00",  "Tablet",           200, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Neurobion",               "Vitamin B Complex",              "VITAMINS",    "NEUR-B",    "2106.90.00", "VAT_16",  "15.00",  "35.00",  "Tablet",           150, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Calcium+Vitamin D",       "Calcium Carbonate+Vit D3",       "VITAMINS",    "CALC-D",    "2106.90.00", "VAT_16",  "18.00",  "40.00",  "Tablet",           120, "Laborex Kenya",         cat, uom, tax);

        // ---- GENERAL ----
        seedProduct(batchCounter, expiry, "Albendazole 400mg",       "Albendazole 400mg",              "GENERAL",     "ALB-400",   "3004.90.00", "EXEMPT",  "25.00",  "60.00",  "Tablet",           150, "Harleys Ltd",           cat, uom, tax);
        seedProduct(batchCounter, expiry, "Betamethasone Cream",     "Betamethasone 0.1% Cream",       "GENERAL",     "BET-CRM",   "3004.90.00", "EXEMPT",  "55.00",  "120.00", "Cream/Ointment (Tube)", 50, "Laborex Kenya",     cat, uom, tax);
        seedProduct(batchCounter, expiry, "Gentamicin Eye Drops",    "Gentamicin 0.3% Eye Drops",      "GENERAL",     "GEN-EYE",   "3004.90.00", "EXEMPT",  "55.00",  "120.00", "Bottle",            40, "Cosmos Pharmaceuticals", cat, uom, tax);
        seedProduct(batchCounter, expiry, "Surgical Spirit 100ml",   "Isopropyl Alcohol 70%",          "GENERAL",     "SURG-SP",   "3808.94.00", "VAT_16",  "25.00",  "60.00",  "Bottle",            50, "Harleys Ltd",           cat, uom, tax);

        log.info("Medicine catalogue seeding complete. {} products processed.", batchCounter[0] - 1);
    }

    @SuppressWarnings("java:S107") // many params intentional for concise seeding table
    private void seedProduct(
            int[] batchCounter,
            LocalDate expiry,
            String brandName,
            String genericName,
            String categoryCode,
            String sku,
            String hsCode,
            String taxCode,
            String costPriceStr,
            String sellingPriceStr,
            String uomName,
            int initialQty,
            String supplierName,
            java.util.function.Function<String, Category> catLookup,
            java.util.function.Function<String, UnitOfMeasure> uomLookup,
            java.util.function.Function<String, TaxClassification> taxLookup) {

        Optional<Product> existing = productRepository.findBySku(sku);
        if (existing.isPresent()) {
            return; // idempotent
        }

        Category category           = catLookup.apply(categoryCode);
        UnitOfMeasure unit          = uomLookup.apply(uomName);
        TaxClassification taxClass  = taxLookup.apply(taxCode);

        Product product = Product.builder()
                .sku(sku)
                .brandName(brandName)
                .genericName(genericName)
                .category(category)
                .baseUnit(unit)
                .taxClassification(taxClass)
                .hsCode(hsCode)
                .costPrice(new BigDecimal(costPriceStr))
                .sellingPrice(new BigDecimal(sellingPriceStr))
                .build();
        productRepository.save(product);

        String batchNumber = String.format("BT-2024-%03d", batchCounter[0]++);
        ProductBatch batch = ProductBatch.builder()
                .product(product)
                .batchNumber(batchNumber)
                .expiryDate(expiry)
                .costPrice(new BigDecimal(costPriceStr))
                .quantityAvailable(initialQty)
                .supplierName(supplierName)
                .build();
        productBatchRepository.save(batch);

        log.info("Initialized product: {} [{}] – batch {} qty={}", brandName, sku, batchNumber, initialQty);
    }

    // -------------------------------------------------------------------------
    // Fractional & Bulk Unit Conversions (Pieces, Strips, Whole Packs)
    // -------------------------------------------------------------------------

    private void initProductUnitConversions() {
        // Panadol: Base = Tablet (1 piece). Strips (10) and Whole Pack (100)
        seedConversion("PCM-500", "Strip (10 Tabs)", 10, new BigDecimal("45.00"));
        seedConversion("PCM-500", "Pack (100 Tabs)", 100, new BigDecimal("400.00"));

        // Brufen 400mg: Base = Tablet. Strips (10) and Whole Pack (100)
        seedConversion("IBU-400", "Strip (10 Tabs)", 10, new BigDecimal("100.00"));
        seedConversion("IBU-400", "Pack (100 Tabs)", 100, new BigDecimal("900.00"));

        // Augmentin 625mg: Base = Tablet. Whole Pack (14 tabs)
        seedConversion("AUG-625", "Pack (14 Tabs)", 14, new BigDecimal("1400.00"));

        // Amoxil 500mg: Base = Capsule. Strips (10) and Whole Pack (100)
        seedConversion("AMX-500", "Strip (10 Tabs)", 10, new BigDecimal("160.00"));
        seedConversion("AMX-500", "Pack (100 Tabs)", 100, new BigDecimal("1500.00"));

        // Coartem: Base = Tablet. Strip (6 tabs) and Full Course (24 tabs)
        seedConversion("COAR-6",  "Strip (6 Tabs)", 6, new BigDecimal("350.00"));
        seedConversion("COAR-6",  "Pack (24 Tabs)", 24, new BigDecimal("1200.00"));

        // Amlodipine 5mg: Base = Tablet. Strip (10) and Monthly Pack (30)
        seedConversion("AML-5",   "Strip (10 Tabs)", 10, new BigDecimal("150.00"));
        seedConversion("AML-5",   "Pack (30 Tabs)", 30, new BigDecimal("400.00"));

        // Metformin 500mg: Base = Tablet. Strip (10) and Monthly Pack (100)
        seedConversion("MET-500", "Strip (10 Tabs)", 10, new BigDecimal("100.00"));
        seedConversion("MET-500", "Pack (100 Tabs)", 100, new BigDecimal("850.00"));

        // Omeprazole 20mg: Base = Capsule. Strip (10) and Monthly Pack (30)
        seedConversion("OMP-20",  "Strip (10 Tabs)", 10, new BigDecimal("250.00"));
        seedConversion("OMP-20",  "Pack (30 Tabs)", 30, new BigDecimal("700.00"));
    }

    private void seedConversion(String sku, String uomName, int factor, BigDecimal price) {
        productRepository.findBySku(sku).ifPresent(prod -> {
            unitOfMeasureRepository.findByName(uomName).ifPresent(uom -> {
                Optional<ProductUnitConversion> existing = productUnitConversionRepository.findByProductAndUnit(prod, uom);
                if (existing.isEmpty()) {
                    ProductUnitConversion conv = ProductUnitConversion.builder()
                            .product(prod)
                            .unit(uom)
                            .conversionFactor(factor)
                            .sellingPrice(price)
                            .build();
                    productUnitConversionRepository.save(conv);
                    log.info("Initialized unit conversion: {} -> {} (x{}, KES {})", prod.getBrandName(), uomName, factor, price);
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // Staff & Suppliers (existing, unchanged)
    // -------------------------------------------------------------------------

    private void initRolesAndUsers() {
        Role adminRole = getOrCreateRole("ROLE_ADMIN", "System Administrator & General Manager");
        Role pharmRole = getOrCreateRole("ROLE_PHARMACIST", "Licensed Dispensing Pharmacist");
        Role cashierRole = getOrCreateRole("ROLE_CASHIER", "Counter POS Cashier");

        // Seed Staff
        createStaffIfAbsent("admin",  "System Administrator",          adminRole,    "admin@pharmacore.com",  "0700000000", "1234");
        createStaffIfAbsent("carson", "Carson Sila (Store Manager)",   adminRole,    "carson@pharmacore.com", "0712345678", "0000");
        createStaffIfAbsent("jane",   "Jane Wanjiku (Pharmacist 1)",   pharmRole,    "jane@pharmacore.com",   "0722112233", "1111");
        createStaffIfAbsent("david",  "David Kamau (Pharmacist 2)",    pharmRole,    "david@pharmacore.com",  "0733445566", "2222");
        createStaffIfAbsent("faith",  "Faith Mutua (Cashier)",         cashierRole,  "faith@pharmacore.com",  "0799887766", "3333");
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
        createSupplierIfAbsent("Harleys Ltd",           "Suresh Patel",        "+254 722 000111", "orders@harleys.co.ke",    "P051123456Z", "Commercial St, Industrial Area",  "Net 30");
        createSupplierIfAbsent("Laborex Kenya",         "Grace Njoroge",       "+254 733 111222", "sales@laborex-kenya.com", "P051987654A", "Mombasa Road, Nairobi",           "Net 15");
        createSupplierIfAbsent("Cosmos Pharmaceuticals","Dr. Benson Ochieng",  "+254 720 333444", "info@cosmos-pharm.com",   "P051555666C", "Rangwe Rd, Off Lunga Lunga Rd",   "COD");
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
