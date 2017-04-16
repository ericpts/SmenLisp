import java.util.List;

/**
 * Created by ericpts on 4/16/17.
 */
interface SmenObject {
    boolean atom();
    boolean pair();

    boolean bool();
}

interface AtomObject extends SmenObject {
    @Override
    public default boolean atom() {
        return true;
    }

    @Override
    public default boolean pair() {
        return false;
    }
}

class IntConstant implements AtomObject {
    int value_;

    IntConstant(int value) {
        this.value_ = value;
    }

    static IntConstant fromToken(String s) {
        return new IntConstant(Integer.parseInt(s));
    }

    int value() {
        return this.value_;
    }

    @Override
    public boolean bool() {
        return value_ != 0;
    }
}

class FloatConstant implements AtomObject {
    float value_;

    FloatConstant(float value) {
        this.value_ = value;
    }

    static FloatConstant fromToken(String s) {
        return new FloatConstant(Float.parseFloat(s));
    }

    float value() {
        return  this.value_;
    }

    @Override
    public boolean bool() {
        return value_ != 0.0;
    }
}

class Symbol implements AtomObject {
    String value_;

    Symbol(String value) {
        this.value_ = value;
    }

    String value() {
        return this.value_;
    }

    static Symbol fromToken(String s) {
        return new Symbol(s);
    }

    @Override
    public boolean bool() {
        return !value_.equals("#f");
    }


    static Symbol True() {
        Symbol ret = new Symbol("#t");
        return ret;
    }

    static Symbol False() {
        Symbol ret = new Symbol("f");
        return ret;
    }
}

interface Procedure extends SmenObject {
    @Override
    public default boolean atom() {
        return true;
    }

    @Override
    public default boolean pair() {
        return false;
    }

    @Override
    public default boolean bool() {
        return true;
    }

    public SmenObject call(List<SmenObject> args);
}
