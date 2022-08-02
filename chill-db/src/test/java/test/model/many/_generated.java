package test.model.many;

import chill.db.ChillCodeGenerator;
import chill.db.ChillRecord;
import test.model.User;
import test.model.Vehicle;

public class _generated extends ChillCodeGenerator {

    public static void main(String[] args) throws Exception {
        generateCodeForMyPackage();
    }
    public static abstract class AbstractManyA extends ChillRecord {

        protected final ManyA self = (ManyA) (Object) this;


        public ManyA createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ManyA saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ManyA firstOrCreateOrThrow(){
            return (ManyA) firstOrCreateImpl();
        }

        public ManyA fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.Long getId() {
            return self.id.get();
        }

        public chill.db.ChillQuery<test.model.many.ManyB> getManyBs() {
            return self.manyBs.get();
        }

        public chill.db.ChillQuery<test.model.many.ManyC> getManyCs() {
            return self.manyCs.get();
        }

        public static final chill.db.ChillRecord.Finder<ManyA> find = finder(ManyA.class);

        public static chill.db.ChillQuery<ManyA> where(Object... args) {
            return find.where(args);}

        public static chill.db.ChillQuery<ManyA> join(chill.db.ChillField.FK fk) {
            return find.join(fk);}

        public static class to {
            private static final ManyA instance = new ManyA();
        }

    }


    public static abstract class AbstractManyB extends ChillRecord {

        protected final ManyB self = (ManyB) (Object) this;


        public ManyB createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ManyB saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ManyB firstOrCreateOrThrow(){
            return (ManyB) firstOrCreateImpl();
        }

        public ManyB fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.Long getId() {
            return self.id.get();
        }

        public test.model.many.ManyA getParentA() {
            return self.parentA.get();
        }

        public void setParentA(test.model.many.ManyA parentA) {
            self.parentA.set(parentA);
        }

        public ManyB withParentA(test.model.many.ManyA parentA) {
            setParentA(parentA);
            return self;
        }

        public static chill.db.ChillQuery<ManyB> forManyA(ManyA parentA) {
            return new ManyB().parentA.reverse(parentA);
        }

        public chill.db.ChillQuery<test.model.many.ManyC> getManyCs() {
            return self.manyCs.get();
        }

        public static final chill.db.ChillRecord.Finder<ManyB> find = finder(ManyB.class);

        public static chill.db.ChillQuery<ManyB> where(Object... args) {
            return find.where(args);}

        public static chill.db.ChillQuery<ManyB> join(chill.db.ChillField.FK fk) {
            return find.join(fk);}

        public static class to {
            private static final ManyB instance = new ManyB();
            public static final chill.db.ChillField.FK parentA = instance.parentA;
        }

    }


    public static abstract class AbstractManyC extends ChillRecord {

        protected final ManyC self = (ManyC) (Object) this;


        public ManyC createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ManyC saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return self;
        }

        public ManyC firstOrCreateOrThrow(){
            return (ManyC) firstOrCreateImpl();
        }

        public ManyC fromWebParams(java.util.Map<String, String> values, String... params) {
            ChillRecord.populateFromWebParams(self, values, params);
            return self;
        }

        public java.lang.Long getId() {
            return self.id.get();
        }

        public test.model.many.ManyB getParentB() {
            return self.parentB.get();
        }

        public void setParentB(test.model.many.ManyB parentB) {
            self.parentB.set(parentB);
        }

        public ManyC withParentB(test.model.many.ManyB parentB) {
            setParentB(parentB);
            return self;
        }

        public static chill.db.ChillQuery<ManyC> forManyB(ManyB parentB) {
            return new ManyC().parentB.reverse(parentB);
        }

        public static final chill.db.ChillRecord.Finder<ManyC> find = finder(ManyC.class);

        public static chill.db.ChillQuery<ManyC> where(Object... args) {
            return find.where(args);}

        public static chill.db.ChillQuery<ManyC> join(chill.db.ChillField.FK fk) {
            return find.join(fk);}

        public static class to {
            private static final ManyC instance = new ManyC();
            public static final chill.db.ChillField.FK parentB = instance.parentB;
        }

    }
}