package test.model;

import chill.db.ChillCodeGenerator;
import chill.db.ChillRecord;
import chill.db.ChillQuery;

public class _generated extends ChillCodeGenerator {

    public static void main(String[] args) throws Exception {
        generateCodeForMyPackage();
    }


//=================== GENERATED CODE ========================


    public static abstract class AbstractUser extends ChillRecord {

        protected final User self = (User) (Object) this;


        public User createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public User saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public User firstOrCreateOrThrow(){
            return (User) firstOrCreateImpl();
        }

        public User fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.Long getId() {
            return self.id.get();
        }

        public java.lang.String getFirstName() {
            return self.firstName.get();
        }

        public void setFirstName(java.lang.String firstName) {
            self.firstName.set(firstName);
        }

        public User withFirstName(java.lang.String firstName) {
            setFirstName(firstName);
            return self;
        }

        public java.lang.String getLastName() {
            return self.lastName.get();
        }

        public void setLastName(java.lang.String lastName) {
            self.lastName.set(lastName);
        }

        public User withLastName(java.lang.String lastName) {
            setLastName(lastName);
            return self;
        }

        public java.lang.String getEmail() {
            return self.email.get();
        }

        public void setEmail(java.lang.String email) {
            self.email.set(email);
        }

        public User withEmail(java.lang.String email) {
            setEmail(email);
            return self;
        }

        public java.lang.String getPassword() {
            return self.password.get();
        }

        public void setPassword(java.lang.String password) {
            self.password.set(password);
        }

        public User withPassword(java.lang.String password) {
            setPassword(password);
            return self;
        }

        @chill.db.ChillRecord.Generated public boolean passwordMatches(String passwd) {
            return self.password.passwordMatches(passwd);
        }

        public java.lang.Integer getAge() {
            return self.age.get();
        }

        public void setAge(java.lang.Integer age) {
            self.age.set(age);
        }

        public User withAge(java.lang.Integer age) {
            setAge(age);
            return self;
        }

        public chill.db.ChillQuery<test.model.Vehicle> getVehicles() {
            return self.vehicles.get();
        }

        public static final chill.db.ChillRecord.Finder<User> find = finder(User.class);

        private static final User instance = new User();

        public static chill.db.ChillQuery<User> where(Object... args) {
            return find.where(args);
        }

        public static chill.db.ChillQuery<User> join(chill.db.ChillField.FK fk) {
            return find.join(fk);
        }

        public static chill.db.ChillQuery<User> select(chill.db.ChillField... fields) {
            return find.select(fields);
        }

        public static class to {
        }

        public static class field {
            public static final chill.db.ChillField<User> ALL = new chill.db.ChillField<>(instance, "*", User.class);
            public static final chill.db.ChillField<java.lang.Long> id = instance.id;
            public static final chill.db.ChillField<java.lang.String> firstName = instance.firstName;
            public static final chill.db.ChillField<java.lang.String> lastName = instance.lastName;
            public static final chill.db.ChillField<java.lang.String> email = instance.email;
            public static final chill.db.ChillField<java.lang.String> password = instance.password;
            public static final chill.db.ChillField<java.lang.Integer> age = instance.age;
        }
        public static chill.db.ChillField<User> allFields() {
            return field.ALL;
        }
        public static chill.db.ChillField<java.lang.Long> id() {
            return instance.id;
        }
        public static chill.db.ChillField<java.lang.String> firstName() {
            return instance.firstName;
        }
        public static chill.db.ChillField<java.lang.String> lastName() {
            return instance.lastName;
        }
        public static chill.db.ChillField<java.lang.String> email() {
            return instance.email;
        }
        public static chill.db.ChillField<java.lang.String> password() {
            return instance.password;
        }
        public static chill.db.ChillField<java.lang.Integer> age() {
            return instance.age;
        }
    }

    public static abstract class AbstractVehicle extends ChillRecord {

        protected final Vehicle self = (Vehicle) (Object) this;


        public Vehicle createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public Vehicle saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public Vehicle firstOrCreateOrThrow(){
            return (Vehicle) firstOrCreateImpl();
        }

        public Vehicle fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.Long getId() {
            return self.id.get();
        }

        public java.sql.Timestamp getCreatedAt() {
            return self.createdAt.get();
        }

        public java.sql.Timestamp getUpdatedAt() {
            return self.updatedAt.get();
        }

        public java.lang.String getMake() {
            return self.make.get();
        }

        public void setMake(java.lang.String make) {
            self.make.set(make);
        }

        public Vehicle withMake(java.lang.String make) {
            setMake(make);
            return self;
        }

        public java.lang.String getModel() {
            return self.model.get();
        }

        public void setModel(java.lang.String model) {
            self.model.set(model);
        }

        public Vehicle withModel(java.lang.String model) {
            setModel(model);
            return self;
        }

        public java.lang.String getUuid() {
            return self.uuid.get();
        }

        public java.lang.Integer getYear() {
            return self.year.get();
        }

        public void setYear(java.lang.Integer year) {
            self.year.set(year);
        }

        public Vehicle withYear(java.lang.Integer year) {
            setYear(year);
            return self;
        }

        public test.model.User getUser() {
            return self.user.get();
        }

        public void setUser(test.model.User user) {
            self.user.set(user);
        }

        public Vehicle withUser(test.model.User user) {
            setUser(user);
            return self;
        }

        public static chill.db.ChillQuery<Vehicle> forUser(User user) {
            return new Vehicle().user.reverse(user);
        }

        public static final chill.db.ChillRecord.Finder<Vehicle> find = finder(Vehicle.class);

        private static final Vehicle instance = new Vehicle();

        public static chill.db.ChillQuery<Vehicle> where(Object... args) {
            return find.where(args);
        }

        public static chill.db.ChillQuery<Vehicle> join(chill.db.ChillField.FK fk) {
            return find.join(fk);
        }

        public static chill.db.ChillQuery<Vehicle> select(chill.db.ChillField... fields) {
            return find.select(fields);
        }

        public static class to {
            public static final chill.db.ChillField.FK user = instance.user;
        }

        public static class field {
            public static final chill.db.ChillField<Vehicle> ALL = new chill.db.ChillField<>(instance, "*", Vehicle.class);
            public static final chill.db.ChillField<java.lang.Long> id = instance.id;
            public static final chill.db.ChillField<java.sql.Timestamp> createdAt = instance.createdAt;
            public static final chill.db.ChillField<java.sql.Timestamp> updatedAt = instance.updatedAt;
            public static final chill.db.ChillField<java.lang.String> make = instance.make;
            public static final chill.db.ChillField<java.lang.String> model = instance.model;
            public static final chill.db.ChillField<java.lang.String> uuid = instance.uuid;
            public static final chill.db.ChillField<java.lang.Integer> year = instance.year;
            public static final chill.db.ChillField<test.model.User> user = instance.user;
        }
        public static chill.db.ChillField<Vehicle> allFields() {
            return field.ALL;
        }
        public static chill.db.ChillField<java.lang.Long> id() {
            return instance.id;
        }
        public static chill.db.ChillField<java.sql.Timestamp> createdAt() {
            return instance.createdAt;
        }
        public static chill.db.ChillField<java.sql.Timestamp> updatedAt() {
            return instance.updatedAt;
        }
        public static chill.db.ChillField<java.lang.String> make() {
            return instance.make;
        }
        public static chill.db.ChillField<java.lang.String> model() {
            return instance.model;
        }
        public static chill.db.ChillField<java.lang.String> uuid() {
            return instance.uuid;
        }
        public static chill.db.ChillField<java.lang.Integer> year() {
            return instance.year;
        }
        public static chill.db.ChillField<test.model.User> user() {
            return instance.user;
        }
    }


}