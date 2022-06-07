package chill.m8.scratch;

import chill.m8.M8;

public class Bootstrap {
    public static void main(String[] args) {
        M8.execute(
                """
                        go to https://chill.software
                        expect that "chill.software" is in the body
                        expect that "foo" is in the body
                        """);
    }
}
