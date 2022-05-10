package model;

import chill.db.ChillField;
import chill.db.ChillRecord;

import java.sql.Timestamp;

public class User extends ChillRecord {

    ChillField<Long> id = pk("id");
    ChillField<Timestamp> createdAt = createdAt("created_at");
    ChillField<Timestamp> updatedAt = updatedAt("updated_at").optimisticLock();
    ChillField<String> email = email("email" );
    ChillField<String> password = password("password");
    ChillField<String> uuid = uuid("uuid");

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

    @chill.db.ChillRecord.Generated public User fromWebParams(java.util.Map<String, String> values, String... params) {
        ChillRecord.populateFromWebParams(this, values, params);
        return this;
    }

    @chill.db.ChillRecord.Generated public Long getId() {
        return id.get();
    }

    @chill.db.ChillRecord.Generated public Timestamp getCreatedAt() {
        return createdAt.get();
    }

    @chill.db.ChillRecord.Generated public Timestamp getUpdatedAt() {
        return updatedAt.get();
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

    @chill.db.ChillRecord.Generated public String getUuid() {
        return uuid.get();
    }

    public static final chill.db.ChillRecord.Finder<User> find = finder(User.class);

    //endregion

    public static void main(String[] args) {
        codeGen();
    }
}
