package chill.db;

import chill.utils.TheMissingUtils;
import test.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.model.Vehicle;

import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChillRecordTest {

    public static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Class.forName("org.h2.Driver");
        ChillRecord.connectionSource = () -> DriverManager.getConnection(TEST_DB_URL);
    }

    @BeforeEach
    public void beforeEach() {
        ChillRecord.executeUpdate(User.DDL);
        ChillRecord.executeUpdate(Vehicle.DDL);
    }

    @Test
    public void boostrapTest() {

        assertEquals(0, User.find.where("first_name=$first AND last_name=$last").count());

        User sample = new User()
                .withFirstName("Carson")
                .withLastName("Gross")
                .withEmail("example@example.com")
                .createOrThrow();

        System.out.println("ID: " + sample.getId());

        //============================================
        // Raw Queries
        //============================================
        assertEquals(1, User.find.all().count());
        assertEquals(1, User.find.where("first_name IS NOT NULL").count());

        //============================================
        // Named Value Queries
        //============================================
        assertEquals(1, User.find.where("first_name=$first",
                "first", "Carson").count());
        assertEquals(1, User.find.where("first_name=$first AND last_name=$last",
                "first", "Carson",
                "last", "Gross").count());

        assertEquals(0, User.find.where("first_name=$first AND last_name=$last",
                "first", "Carson",
                "last", "Taft").count());

        //============================================
        // Positional Queries
        //============================================
        assertEquals(1, User.find.where("first_name=?", "Carson").count());
        assertEquals(1, User.find.where("first_name=? AND last_name=?", "Carson", "Gross").count());
        assertEquals(0, User.find.where("first_name=? AND last_name=?", "Carson", "Taft").count());

        //============================================
        // Short Queries
        //============================================
        assertEquals(1, User.find.where("first_name", "Carson").count());
        assertEquals(1, User.find.where("last_name", "Gross").count());
        assertEquals(1, User.find.where("first_name", "Carson", "last_name", "Gross").count());
        assertEquals(0, User.find.where("last_name", "Taft").count());
        assertEquals(0, User.find.where("first_name", "Carson", "last_name", "Taft").count());
    }

    @Test
    public void testFirstMethod() {
        Assertions.assertNull(User.find.byPrimaryKey(1));

        User sample = new User()
                .withFirstName("Carson")
                .withLastName("Gross")
                .withEmail("example@example.com")
                .createOrThrow();

        Assertions.assertNotNull(User.find.byPrimaryKey(sample.getId()));
    }

    @Test
    public void testUpdateMethod() {
        User sample = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com").createOrThrow();

        assertEquals("Gross", User.find.byPrimaryKey(sample.getId()).getLastName());

        sample.setLastName("Taft");

        int update = sample.update();

        Assertions.assertEquals(1, update);

        assertEquals("Taft", User.find.byPrimaryKey(sample.getId()).getLastName());
    }

    @Test
    public void testUpdateToNull() {
        User sample = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com").createOrThrow();

        assertEquals("Gross", User.find.byPrimaryKey(sample.getId()).getLastName());

        sample.setLastName(null);

        int update = sample.update();

        Assertions.assertEquals(1, update);

        assertNull(User.find.byPrimaryKey(sample.getId()).getLastName());
    }

    @Test
    public void testDeleteMethod() {
        var sample = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com").createOrThrow();

        assertEquals("Gross", User.find.byPrimaryKey(sample.getId()).getLastName());

        sample.setLastName("Taft");

        int deleted = sample.delete();

        Assertions.assertEquals(1, deleted);

        Assertions.assertNull(User.find.byPrimaryKey(sample.getId()));
    }


//    @Test void beforeAndAftersWork(){
//        var vehicle = new Vehicle().withYear(1978);
//        assertEquals(78, vehicle.year.rawValue());
//        assertEquals(1978, vehicle.getYear());
//    }

    @Test
    void foreignKeys(){
        var user = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com").createOrThrow();

        var vehicle = new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983).withUser(user).createOrThrow();

        Vehicle foundVehicle = Vehicle.find.byPrimaryKey(vehicle.getId());
        User user2 = User.find.byPrimaryKey(user.getId());
        assertNotNull(user2);

        User user3 = foundVehicle.getUser();
        assertNotNull(user3);

        ChillQuery<Vehicle> vehicles = Vehicle.forUser(user3);
        assertEquals(1, vehicles.count());
        assertEquals(vehicle.getId(), vehicles.first().getId());
    }

    @Test
    void validators(){
        var user = new User()
                .withFirstName("Carson")
                .withLastName("Gross")
                .withEmail("example.com");

        assertEquals(false, user.create());
        ChillValidation.Errors errors = user.getErrors();

        List<String> errorsForEmail = errors.getErrorsFor("email");
        assertEquals(1, errorsForEmail.size());
        assertEquals("Invalid Email: example.com", errorsForEmail.get(0));

        user.setEmail("carson@example.com");

        assertEquals(true, user.create());
    }

    @Test
    void passwordTest() {
        var user = new User()
                .withPassword("chill pill")
                .withEmail("example.com");


        assertEquals(true, user.passwordMatches("chill pill"));

        assertEquals(false, user.passwordMatches("unchill pill"));

    }

    @Test
    void requiredTest() {
        var user = new User();

        assertEquals(false, user.isRecordValid());

        List<String> emailErrors = user.getErrors().getErrorsFor("email");

        assertEquals(2, emailErrors.size());

        assertTrue(emailErrors.contains("Field email cannot be null"));
        assertTrue(emailErrors.contains("Invalid Email: null"));
    }

    @Test
    void uuidTest() {
        var vehicle = new Vehicle();
        System.out.println(vehicle.getUuid());
        assertNotNull(vehicle.getUuid());
    }

    @Test
    void manyTest() {
        var user = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com").createOrThrow();

        // 5 in users collection
        TheMissingUtils.n(5).times(() -> {
            var vehicle = new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983).withUser(user);
            vehicle.createOrThrow();
        });

        // not in users collection
        new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983).createOrThrow();

        var vehicles = user.getVehicles().toList();

        assertEquals(5, vehicles.size());

        for (Vehicle vehicle : vehicles) {
            assertEquals("Toyota", vehicle.getMake());
            assertEquals("LandCruiser", vehicle.getModel());
            assertEquals(1983, vehicle.getYear());
        }
    }

    @Test
    void manyTestThroughQuery() {
        var user = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com").createOrThrow();

        // 5 vehicles in users collection
        TheMissingUtils.n(5).times(() -> {
            user.getVehicles().newRecord()
                    .withMake("Toyota").withModel("LandCruiser").withYear(1983)
                    .createOrThrow();
        });

        // not in users collection
        new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983).createOrThrow();

        var vehicles = user.getVehicles().toList();

        assertEquals(5, vehicles.size());

        for (Vehicle vehicle : vehicles) {
            assertEquals("Toyota", vehicle.getMake());
            assertEquals("LandCruiser", vehicle.getModel());
            assertEquals(1983, vehicle.getYear());
        }
    }

    @Test
    void firstOrCreateOrThrow() {
        var user = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com").createOrThrow();

        // 5 vehicles in users collection
        TheMissingUtils.n(5).times(() -> {
            Vehicle vehicle = user.getVehicles()
                    .newRecord()
                    .withMake("Toyota")
                    .withModel("LandCruiser")
                    .withYear(1983)
                    .firstOrCreateOrThrow();
        });

        // not in users collection
        new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983).createOrThrow();

        var vehicles = user.getVehicles().toList();

        assertEquals(1, vehicles.size());

        for (Vehicle vehicle : vehicles) {
            assertEquals("Toyota", vehicle.getMake());
            assertEquals("LandCruiser", vehicle.getModel());
            assertEquals(1983, vehicle.getYear());
        }
    }

    @Test
    public void testDateFunctionality(){
        Vehicle vehicle = new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983);

        assertNull(vehicle.getCreatedAt());
        assertNull(vehicle.getUpdatedAt());

        vehicle.saveOrThrow();

        var initialUpdatedAt = vehicle.getUpdatedAt();
        assertNotNull(vehicle.getCreatedAt());
        assertNotNull(initialUpdatedAt);
        assertEquals(vehicle.getCreatedAt(), initialUpdatedAt);

        vehicle.setMake("Jeep");
        vehicle.setModel("Wrangler");
        vehicle.saveOrThrow();

        assertTrue(vehicle.getUpdatedAt().after(initialUpdatedAt));
        assertTrue(vehicle.getUpdatedAt().after(vehicle.getCreatedAt()));
    }

    @Test
    public void testOptimistConcurrency(){
        Vehicle vehicle = new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983);
        vehicle.saveOrThrow();

        Vehicle copyOfVehicle = Vehicle.find.byPrimaryKey(vehicle.getId());

        vehicle.setMake("Jeep");
        vehicle.setModel("Wrangler");
        vehicle.saveOrThrow();

        // should fail
        copyOfVehicle.setMake("Ford");
        copyOfVehicle.setModel("F150");
        try {
            copyOfVehicle.saveOrThrow();
            fail("Should have failed w/ an optimistic concurrency failure");
        } catch (ChillRecord.OptimisticConcurrencyFailure e) {
            e.printStackTrace();
            // pass
        }
    }

    @Test
    public void testOptimistConcurrencyWorksWRepeatedSaves(){
        Vehicle vehicle = new Vehicle().withMake("Toyota").withModel("LandCruiser").withYear(1983);
        vehicle.saveOrThrow();

        vehicle.setMake("Jeep");
        vehicle.setModel("Wrangler");
        vehicle.saveOrThrow();

        vehicle.setMake("Toyota");
        vehicle.setModel("LandCruiser");
        vehicle.saveOrThrow();
    }

    @Test
    void perfTest(){
        int iterations = 10000;
        long time = TheMissingUtils.time(() -> {
            TheMissingUtils.n(iterations).times(
                    () -> {
                        User user = new User().withFirstName("Carson").withLastName("Gross").withEmail("example@example.com");
                        user.saveOrThrow();
                        user.getVehicles().newRecord().withMake("Toyota").withModel("LandCruiser").withYear(1983).saveOrThrow();
                    }
            );
        });
        System.out.println("Iterations: " + iterations + " took " + time + "ms");
    }

}
