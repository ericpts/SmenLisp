import java.util.ArrayList;
import java.util.List;

/**
 * Created by ericpts on 4/16/17.
 */
public class Lexer {
    private String parseToken(String text) {
        assert(!text.isEmpty());

        final char first = text.charAt(0);

        if (first == '(' || first == ')') {
            return String.valueOf(first);
        }

        int at = 0;
        while (at < text.length() && !Character.isWhitespace(text.charAt(at))
                && text.charAt(at) != '(' && text.charAt(at) != ')') {
            at += 1;
        }

        return text.substring(0, at);
    }

    public List<String> parseImpl(String text, List<String> acum) {

        if (text.isEmpty()) {
            return acum;
        }

        if (Character.isWhitespace(text.charAt(0))) {
            return parseImpl(text.substring(1), acum);
        }

        final String currentToken = parseToken(text);
        acum.add(currentToken);

        return parseImpl(text.substring(currentToken.length()), acum);
    }

    public List<String> parse(String text) {
        return parseImpl(text, new ArrayList<>());
    }
}

