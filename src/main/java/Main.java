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

    static SmenObject lambda(SExpression argSymbols, SmenObject body, Environment env) {
        return (Procedure) args -> {
            assert(argSymbols.size() == args.size());
            final int n = args.size();

            Environment newEnv = new Environment(env);
            for (int i = 0; i < n; ++i) {
                newEnv.add(((Symbol)argSymbols.get(i)).value(), args.get(i));
            }

            return eval(body, newEnv);
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

        System.out.println("HERE!");

        Environment env = Environment.defaultEnvironment();
        exps.stream().forEach(obj -> {
            System.out.println(eval(obj, env));
        });
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
        final Symbol head = (Symbol) eval(exp.car(), env);
        final SExpression tail = (SExpression)exp.cdr();

        if (head.value() == "if") {
            SmenObject pred = tail.get(0);
            if (eval(pred, env).bool()) {
                return eval(tail.get(1), env);
            } else if (tail.size() == 3) {
                return eval(tail.get(2), env);
            }
        }

        if (head.value() == "define") {
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

                SExpression funArgs = (SExpression)tail.car();
                SmenObject body = tail.cdr();

                Symbol funName = (Symbol)funArgs.car();
                SExpression args = (SExpression)funArgs.cdr();
                env.add(funName.value(), lambda(args, body, env));

                return funName;
            }
        }

        if (head.value() == "lambda") {
            // The first part are the arguments
            final SExpression argSymbols = (SExpression)tail.get(0);
            final SmenObject body = tail.get(1);


            return lambda(argSymbols, body, env);
        }

        return apply(head, tail.objects());
    }

    static SmenObject apply(SmenObject procObject, List<SmenObject> args) {
        Procedure proc = (Procedure)procObject;
        return proc.call(args);
    }
}
