package chill.rpc;

import java.util.List;
import java.util.stream.Collectors;

public class SampleImpl implements SampleInterface {
    @Override
    public int addOne(int i) {
        return i + 1;
    }

    @Override
    public List<String> filter(List<String> strs, String regex) {
        return strs.stream().filter(s -> s.matches(regex)).collect(Collectors.toList());
    }

    @Override
    public SampleBean updateBeansName(SampleBean bean) {
        bean.setName("bar");
        return bean;
    }

    @Override
    public void throwsAnException() {
        throw new SampleRuntimeException("Foo");
    }
}
