package test.model;

import chill.db.*;
import chill.db.ChillField.FK;

import java.sql.Timestamp;

public class Vehicle extends ChillRecord {

    public static final String DDL = """
            DROP TABLE IF EXISTS vehicle;
            CREATE TABLE vehicle (
              id INT AUTO_INCREMENT PRIMARY KEY,
              created_at TIMESTAMP,
              updated_at TIMESTAMP,
              user_id INT,
              make VARCHAR(250) NOT NULL,
              model VARCHAR(250) NOT NULL,
              uuid VARCHAR(250),
              year INTEGER
            );
            """;

    ChillField<Long> id = pk("id");
    ChillField<Timestamp> createdAt = createdAt("created_at");
    ChillField<Timestamp> updatedAt = updatedAt("updated_at").optimisticLock();

    ChillField<String> make = field("make", String.class);
    ChillField<String> model = field("model", String.class);
    ChillField<String> uuid = uuid("uuid");

    ChillField<Integer> year = field("year", Integer.class);

    FK<Vehicle, User> user = fk("user_id", User.class);

    //region chill.Record GENERATED CODE

    public Vehicle createOrThrow(){
        if(!create()){
            throw new chill.db.ChillValidation.ValidationException(getErrors());
        }
        return this;
    }

    public Vehicle saveOrThrow(){
        if(!save()){
            throw new chill.db.ChillValidation.ValidationException(getErrors());
        }
        return this;
    }

    public Vehicle firstOrCreateOrThrow(){
        return (Vehicle) firstOrCreateImpl();
    }

    @chill.db.ChillRecord.Generated public java.lang.Long getId() {
        return id.get();
    }

    @chill.db.ChillRecord.Generated public java.sql.Timestamp getCreatedAt() {
        return createdAt.get();
    }

    @chill.db.ChillRecord.Generated public java.sql.Timestamp getUpdatedAt() {
        return updatedAt.get();
    }

    @chill.db.ChillRecord.Generated public java.lang.String getMake() {
        return make.get();
    }

    @chill.db.ChillRecord.Generated public void setMake(String make) {
        this.make.set(make);
    }

    @chill.db.ChillRecord.Generated public Vehicle withMake(String make) {
        setMake(make);
        return this;
    }

    @chill.db.ChillRecord.Generated public java.lang.String getModel() {
        return model.get();
    }

    @chill.db.ChillRecord.Generated public void setModel(String model) {
        this.model.set(model);
    }

    @chill.db.ChillRecord.Generated public Vehicle withModel(String model) {
        setModel(model);
        return this;
    }

    @chill.db.ChillRecord.Generated public java.lang.String getUuid() {
        return uuid.get();
    }

    @chill.db.ChillRecord.Generated public java.lang.Integer getYear() {
        return year.get();
    }

    @chill.db.ChillRecord.Generated public void setYear(Integer year) {
        this.year.set(year);
    }

    @chill.db.ChillRecord.Generated public Vehicle withYear(Integer year) {
        setYear(year);
        return this;
    }

    @chill.db.ChillRecord.Generated public test.model.User getUser() {
        return user.get();
    }

    @chill.db.ChillRecord.Generated public void setUser(User user) {
        this.user.set(user);
    }

    @chill.db.ChillRecord.Generated public Vehicle withUser(User user) {
        setUser(user);
        return this;
    }

    @chill.db.ChillRecord.Generated public static chill.db.ChillQuery<Vehicle> forUser(User user) {
        return new Vehicle().user.reverse(user);
    }

    public static final chill.db.ChillRecord.Finder<Vehicle> find = finder(Vehicle.class);

    //endregion

    public static void main(String[] args) {
        codeGen();
    }

}
