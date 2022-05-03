package chill.util;

import chill.utils.TheMissingUtils;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.Opcodes.*;


public class ForceThrowTest {

    public static void main(String[] args) {
        ClassWriter cw = new ClassWriter( 0 );
        MethodVisitor mv;

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, "chill/utils/ForceThrowerImpl", null,
                "java/lang/Object",
                new String[]{"chill/utils/ForceThrower"} );

        cw.visitSource( "ForceThrowerImpl.java", null );

        // constructor
        mv = cw.visitMethod( ACC_PUBLIC, "<init>", "()V", null, null );
        mv.visitCode();
        mv.visitVarInsn( ALOAD, 0 );
        mv.visitMethodInsn( INVOKESPECIAL, "java/lang/Object", "<init>", "()V" );
        mv.visitInsn( RETURN );
        mv.visitMaxs( 1, 1 );
        mv.visitEnd();

        // throwException method
        mv = cw.visitMethod( ACC_PUBLIC, "throwException", "(Ljava/lang/Throwable;)V", null, null );
        mv.visitCode();
        mv.visitVarInsn( ALOAD, 1 );
        mv.visitInsn( ATHROW );
        mv.visitMaxs( 1, 2 );
        mv.visitEnd();

        cw.visitEnd();

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = cw.toByteArray();
        String str = encoder.encodeToString(encode);

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decode = decoder.decode(str);
        boolean equals = Arrays.equals(encode, decode);
        System.out.println("Equals : " + equals);

        System.out.println(str);
    }

    @Test
    public void basicThrowTest() {
        try {
            throwAnIOException();
        } catch (Exception ioException) {
            assertTrue(ioException instanceof IOException);
        }
    }

    private void throwAnIOException() {
        // nb: IOException is checked!
        TheMissingUtils.forceThrow(new IOException());
    }

}
