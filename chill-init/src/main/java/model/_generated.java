package model;

import chill.db.ChillCodeGenerator;
import chill.db.ChillRecord;

public class _generated extends ChillCodeGenerator {

    public static void main(String[] args) throws Exception {
        generateCodeForMyPackage();
    }

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

        public java.sql.Timestamp getCreatedAt() {
            return self.createdAt.get();
        }

        public java.sql.Timestamp getUpdatedAt() {
            return self.updatedAt.get();
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

        public java.lang.String getUuid() {
            return self.uuid.get();
        }

        public static final chill.db.ChillRecord.Finder<User> find = finder(User.class);

    }
}
