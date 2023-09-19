package chill.script.commands;


import chill.script.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionCommandTest {
    @Test
    public void testParse() {
        var output = TestUtils.programOutput("""
                let x be "hello there"
                                
                fun foo()
                    print x
                end
                                
                set x to "hello, world"
                                
                foo()""");
        assertEquals("hello, world", output);
    }

    @Test
    public void testLocalClosures() {
        var output = TestUtils.programOutput("""
                fun hook(value)
                    
                    fun getHook()
                        return value
                    end
                    
                    fun setHook(newValue)
                        set value to newValue
                    end
                    
                    return [getHook, setHook]
                end
                                
                let [getA, setA] be hook("a")
                let [getB, setB] be hook("b")
                                
                print getA()
                print getB()
                setA("c")
                print getA()
                print getB()""");
        assertEquals("abcb", output);
    }

    @Test
    public void destructuring() {
        var output = TestUtils.programOutput("""
                                
                let value be [1, [3, [5, [7]]]]
                let [a, [b, [c, [d]]]] be value
                                
                print a
                print b
                print c
                print d""");
        assertEquals("1357", output);
    }
}
