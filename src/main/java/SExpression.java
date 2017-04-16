import java.util.ArrayList;
import java.util.List;

/**
 * Created by ericpts on 4/16/17.
 */

class SExpression implements SmenObject {
    List<SmenObject> objects_;

    SExpression() {
        this.objects_ = new ArrayList<>();
    }

    SExpression(List<SmenObject> objects) {
        this.objects_ = objects;
    }

    public void add(SmenObject obj) {
        this.objects_.add(obj);
    }

    @Override
    public boolean atom() {
        return false;
    }

    @Override
    public boolean pair() {
        return true;
    }

    SmenObject car() {
        return objects_.get(0);
    }

    SmenObject cdr() {
        return new SExpression(objects_.subList(1, objects_.size()));
    }

    int size() {
        return objects_.size();
    }

    @Override
    public boolean bool() {
        return objects_.size() > 0;
    }

    List<SmenObject> objects() {
        return this.objects_;
    }

    SmenObject get(int i) {
        return  objects_.get(i);
    }
}
