import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by ericpts on 4/16/17.
 */
public class Environment {
    Map<String, SmenObject> map_;
    Environment parent_;

    Environment() {
        this.map_ = new HashMap<>();
        this.parent_ = null;
    }

    Environment(Environment parent) {
        this.map_ = new HashMap<>();
        this.parent_ = parent;
    }

    void add(String t, SmenObject obj) {
        map_.put(t, obj);
    }

    SmenObject lookup(String t) {
        if (map_.containsKey(t)) {
            return map_.get(t);
        }

        if (this.parent_ != null) {
            return this.parent_.lookup(t);
        }

        return null;
    }


    static Environment defaultEnvironment() {
        Environment ret = new Environment();

        Function<SmenObject, Number> toNumber = (SmenObject smenObject) -> {
            assert (smenObject instanceof FloatConstant ||
                    smenObject instanceof IntConstant);
            if (smenObject instanceof FloatConstant) {
                FloatConstant flt = (FloatConstant) smenObject;
                return flt.value();
            } else {
                IntConstant ict = (IntConstant) smenObject;
                return ict.value();
            }
        };

        ret.add("+", new Procedure() {
            @Override
            public SmenObject call(List<SmenObject> args) {
                Stream<Number> mapStream = args.stream().map(toNumber);
                if (args.stream().anyMatch(obj -> obj instanceof FloatConstant)) {
                    return new FloatConstant(mapStream.reduce((r, r2) -> r.floatValue() + r2.floatValue()).get().floatValue());
                } else {
                    return new IntConstant(mapStream.reduce((r, r2) -> r.intValue() + r2.intValue()).get().intValue());
                }
            }
        });

        ret.add("*", new Procedure() {
            @Override
            public SmenObject call(List<SmenObject> args) {
                Stream<Number> mapStream = args.stream().map(toNumber);
                if (args.stream().anyMatch(obj -> obj instanceof FloatConstant)) {
                    return new FloatConstant(mapStream.reduce((r, r2) -> r.floatValue() * r2.floatValue()).get().floatValue());
                } else {
                    return new IntConstant(mapStream.reduce((r, r2) -> r.intValue() * r2.intValue()).get().intValue());
                }
            }
        });

        ret.add("=", new Procedure() {
            @Override
            public SmenObject call(List<SmenObject> args) {
                SmenObject sample = args.get(0);
                Class c = sample.getClass();
                if (!args.stream().allMatch(obj -> c.equals(obj.getClass()))) {
                    return Symbol.False();
                }

                if (sample instanceof Symbol) {
                    return (args.stream().map(obj -> (Symbol)obj).allMatch(sbol -> sbol.value().equals(((Symbol) sample).value()))) ?
                            Symbol.True() : Symbol.False();
                }

                if (sample instanceof IntConstant) {
                    return (args.stream().map(obj -> (IntConstant)obj).allMatch(sbol -> sbol.value() == ((IntConstant)sample).value())) ?
                            Symbol.True() : Symbol.False();
                }

                if (sample instanceof FloatConstant) {
                    return (args.stream().map(obj -> (FloatConstant)obj).allMatch(sbol -> sbol.value() == ((FloatConstant)sample).value())) ?
                            Symbol.True() : Symbol.False();
                }

                return args.stream().allMatch(obj -> obj.equals(sample)) ? Symbol.True() : Symbol.False();
            }
        });

        return ret;
    }
}
