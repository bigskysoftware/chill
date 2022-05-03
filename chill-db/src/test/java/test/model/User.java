package test.model;

import chill.db.ChillField;
import chill.db.ChillQuery;
import chill.db.ChillRecord;

public class User extends ChillRecord {

    public static final String DDL = """
            DROP TABLE IF EXISTS user;
            CREATE TABLE user (
              id INT AUTO_INCREMENT PRIMARY KEY,
              first_name VARCHAR(250),
              last_name VARCHAR(250),
              email VARCHAR(250) DEFAULT NULL,
              password VARCHAR(250) DEFAULT NULL,
              age INTEGER
            );
            """;

    ChillField<Long> id = pk("id");

    ChillField<String> firstName = field("first_name", String.class);

    ChillField<String> lastName = field("last_name", String.class);

    ChillField<String> email = email("email").required();

    ChillField<String> password = password("password");

    ChillField<Integer> age = field("age", Integer.class);

    ChillField<ChillQuery<Vehicle>> vehicles = many(Vehicle.class, "user_id");

    //region chill.Record GENERATED CODE

    public User createOrThrow(){
        if(!create()){
            throw new chill.db.ChillValidation.ValidationException(getErrors());
        }
        return this;
    }

    public User saveOrThrow(){
        if(!save()){
            throw new chill.db.ChillValidation.ValidationException(getErrors());
        }
        return this;
    }

    public User firstOrCreateOrThrow(){
        return (User) firstOrCreateImpl();
    }

    @chill.db.ChillRecord.Generated public Long getId() {
        return id.get();
    }

    @chill.db.ChillRecord.Generated public String getFirstName() {
        return firstName.get();
    }

    @chill.db.ChillRecord.Generated public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    @chill.db.ChillRecord.Generated public User withFirstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    @chill.db.ChillRecord.Generated public String getLastName() {
        return lastName.get();
    }

    @chill.db.ChillRecord.Generated public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    @chill.db.ChillRecord.Generated public User withLastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    @chill.db.ChillRecord.Generated public String getEmail() {
        return email.get();
    }

    @chill.db.ChillRecord.Generated public void setEmail(String email) {
        this.email.set(email);
    }

    @chill.db.ChillRecord.Generated public User withEmail(String email) {
        setEmail(email);
        return this;
    }

    @chill.db.ChillRecord.Generated public String getPassword() {
        return password.get();
    }

    @chill.db.ChillRecord.Generated public void setPassword(String password) {
        this.password.set(password);
    }

    @chill.db.ChillRecord.Generated public User withPassword(String password) {
        setPassword(password);
        return this;
    }

    @chill.db.ChillRecord.Generated public boolean passwordMatches(String passwd) {
        return password.passwordMatches(passwd);
    }

    @chill.db.ChillRecord.Generated public Integer getAge() {
        return age.get();
    }

    @chill.db.ChillRecord.Generated public void setAge(Integer age) {
        this.age.set(age);
    }

    @chill.db.ChillRecord.Generated public User withAge(Integer age) {
        setAge(age);
        return this;
    }

    @chill.db.ChillRecord.Generated public ChillQuery<test.model.Vehicle> getVehicles() {
        return vehicles.get();
    }

    public static final chill.db.ChillRecord.Finder<User> find = finder(User.class);

    //endregion

    public static void main(String[] args) {
        codeGen();
    }

}
