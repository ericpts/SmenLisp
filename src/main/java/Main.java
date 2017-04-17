import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by ericpts on 4/16/17.
 */

class Args {
    @Parameter
    public List<String> files;

    static public Args parse(String[] args) {
        Args ret = new Args();
        JCommander jcom = new JCommander();
        jcom.addObject(ret);
        jcom.parse(args);

        return ret;
    }
}

public class Main {

    /**
     *
     * argSymbols has the form (s1 s2 ... sn).
     * This returns a procedure which accepts n arguments, binds the symbols to the formal arguments and then evaluates body.
     * Body is a list of SExpressions. Each expression inside it is evaluated and the last one is returned
     */
    static SmenObject lambda(SExpression argSymbols, SExpression body, Environment env) {
        return (Procedure) args -> {
            assert(argSymbols.size() == args.size());
            final int n = args.size();

            Environment newEnv = new Environment(env);
            for (int i = 0; i < n; ++i) {
                newEnv.add(((Symbol)argSymbols.get(i)).value(), args.get(i));
            }

            // Evaluate all expressions and return the last one.
            return body.objects().stream().map(obj -> eval(obj, newEnv)).reduce((x, y) -> y).get();
        };
    }

    public static void main(String[] argv) throws IOException {
        Args args = Args.parse(argv);

        if (args.files.size() != 1) {
            System.out.println("Too many files: " + args.files.size());
        }

        final String file = args.files.get(0);

        final String content = new Scanner(new File(file)).useDelimiter("\\Z").next();
        Lexer lexer = new Lexer();
        List<String> tokens = lexer.parse(content);
        List<SmenObject> exps = Parser.parse(tokens);

        Environment env = Environment.defaultEnvironment();
        exps.stream().forEach(obj -> eval(obj, env));
    }

    static SmenObject handleIf(SExpression tail, Environment env) {
        SmenObject pred = tail.get(0);
        if (eval(pred, env).bool()) {
            return eval(tail.get(1), env);
        } else if (tail.size() == 3) {
            return eval(tail.get(2), env);
        }

        return Symbol.False();
    }

    static SmenObject handleDefine(SExpression tail, Environment env) {
        if (tail.get(0).atom()) {
            // It is of the form
            // (define x <val>)
            assert (tail.size() == 2);
            Symbol var = (Symbol) tail.get(0);
            env.add(var.value(), eval(tail.get(1), env));
            return var;
        } else {
            // Otherwise it is a procedure definition
            // (define (fun args...) (body))

            SExpression funArgs = (SExpression) tail.car();
            SExpression body = (SExpression) tail.cdr();

            Symbol funName = (Symbol) funArgs.car();
            SExpression args = (SExpression) funArgs.cdr();
            env.add(funName.value(), lambda(args, body, env));

            return funName;
        }
    }


    static SmenObject eval(SmenObject x, Environment env) {
        if (x.atom()) {
            if (x instanceof  Symbol) {
                return env.lookup(((Symbol)x).value());
            } else {
                return x;
            }
        }
        // Otherwise it is a combination.
        final SExpression exp = (SExpression) x;
        final SmenObject head = exp.car();
        final SExpression tail = (SExpression)exp.cdr();

        if (head instanceof Symbol) {
            final String symbol = ((Symbol) head).value();

            if (symbol.equals("if")) {
                return handleIf(tail, env);
            }

            if (symbol.equals("define")) {
                return handleDefine(tail, env);
            }

            if (symbol.equals("lambda")) {
                // (lambda (args) (body))
                final SExpression argSymbols = (SExpression) tail.get(0);
                final SExpression body = (SExpression)tail.cdr();

                return lambda(argSymbols, body, env);
            }
        }

        return apply(eval(head, env), tail.objects().stream().map(obj -> eval(obj, env)).collect(Collectors.toList()));
    }

    static SmenObject apply(SmenObject procObject, List<SmenObject> args) {
        Procedure proc = (Procedure)procObject;
        return proc.call(args);
    }
}
