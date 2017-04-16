import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Created by ericpts on 4/16/17.
 */
class Parser {

    static class ParseToken {
        enum Type {
            PAREN,
            SMEN_OBJECT,
        }

        public ParseToken(Type type) {
            this.type_ = type;
            this.obj_ = null;
        }

        public ParseToken(SmenObject obj) {
            this.type_ = Type.SMEN_OBJECT;
            this.obj_ = obj;
        }

        Type type_;
        Object obj_;


        SmenObject object() {
            assert(this.type_ == Type.SMEN_OBJECT);
            return (SmenObject)(this.obj_);
        }
    }

    static public List<SmenObject> parse(List<String> tokens) {

        Stack<ParseToken> stack = new Stack<>();

        for (String token: tokens) {
            if (token.equals("(")) {
                stack.push(new ParseToken(ParseToken.Type.PAREN));
            }
            else if (token.equals(")")) {
                SExpression exp = new SExpression();
                ParseToken t;
                while ((t = stack.pop()).type_ != ParseToken.Type.PAREN) {
                    exp.add(t.object());
                }
                stack.push(new ParseToken(exp));
            } else {
                SmenObject obj;
                try {
                    obj = IntConstant.fromToken(token);
                } catch (NumberFormatException e) {
                    try {
                        obj = FloatConstant.fromToken(token);
                    } catch (NumberFormatException ee) {
                        obj = Symbol.fromToken(token);
                    }
                }

                stack.push(new ParseToken(obj));
            }
        }
        return stack.stream().map(parseToken -> parseToken.object()).collect(Collectors.toList());
    }
}
