package chill.script.types.coercions;

import java.math.BigDecimal;
import java.util.LinkedList;

public abstract class Coercion {

    // TODO - priority, thread safe?
    private static final LinkedList<Coercion> COERCIONS = new LinkedList<>();

    static {
        registerDefaultCoercions();
    }

    private static void registerDefaultCoercions() {
        COERCIONS.add( new BoxedToPrimitive());
        COERCIONS.add( new BigDecimalToInt());
    }

    public static Coercion resolve(Class<?> from, Class<?> to) {
        for (Coercion coercion : COERCIONS) {
            if (coercion.canCoerce(from, to)) {
                return coercion;
            }
        }
        return null;
    }

    protected abstract boolean canCoerce(Class<?> from, Class<?> to);

    public abstract Object coerce(Object arg);

    public int getRank() {
        return 3;
    }


    private static class BigDecimalToInt extends Coercion {
        @Override
        protected boolean canCoerce(Class<?> from, Class<?> to) {
            if (from.equals(BigDecimal.class)) {
                if (to.equals(Integer.class) || to.equals(Integer.TYPE)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object coerce(Object arg) {
            return ((BigDecimal) arg).intValue();
        }
    }

    private static class BoxedToPrimitive extends Coercion {
        @Override
        protected boolean canCoerce(Class<?> from, Class<?> to) {
            if (from.equals(Integer.class) && to.equals(Integer.TYPE)) {
                return true;
            }
            if (from.equals(Long.class) && to.equals(Long.TYPE)) {
                return true;
            }
            if (from.equals(Boolean.class) && to.equals(Boolean.TYPE)) {
                return true;
            }
            if (from.equals(Double.class) && to.equals(Double.TYPE)) {
                return true;
            }
            return false;
        }

        @Override
        public Object coerce(Object arg) {
            return arg;
        }
    }
}
