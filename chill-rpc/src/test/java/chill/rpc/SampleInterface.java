package chill.rpc;

import java.util.List;

public interface SampleInterface {

    int addOne(int i);

    List<String> filter(List<String> strs, String regex);

    SampleBean updateBeansName(SampleBean bean);

    void throwsAnException();

}
