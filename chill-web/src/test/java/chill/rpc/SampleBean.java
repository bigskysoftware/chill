package chill.rpc;

import java.io.Serializable;

public class SampleBean  implements Serializable {

    private String name = new String();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
