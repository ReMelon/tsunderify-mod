package remelon.cat;

import remelon.cat.config.TsunderifyConfig;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTransformer {
    private static final Pattern TOKEN_RE = Pattern.compile("[A-Za-z0-9]+(?:'[A-Za-z0-9]+)?|[_]|[^\\w\\s]|[\\s]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Random RNG = new Random();

    private static final Set<String> SWEARS = new HashSet<>(Arrays.asList("fucked", "fuck", "fucks", "fucking", "shit", "shits", "whore", "whores", "cunt", "cunts", "bitch", "bitches", "ass", "asses", "asshole", "assholes", "asshat", "asshats", "dumbass", "dumbasses", "fucker", "fuckers", "idiot", "jackass", "jackasses", "smartass", "smartasses", "pussy", "pussies", "bastard", "bastards", "dick", "dicks", "slut", "sluts", "motherfucker", "motherfuckers", "motherfucking", "omfg", "wtf", "lmfao", "fuckwad", "fuckass", "bitchass"));

    private static final Set<String> ALWAYS_STICKY = new HashSet<>(Arrays.asList("whore", "whores", "cunt", "cunts", "bitch", "bitches", "asses", "asshole", "assholes", "asshat", "asshats", "fucker", "fuckers", "jackass", "jackasses", "smartasses", "pussies", "bastard", "bastards", "dick", "dicks", "slut", "sluts", "motherfucker", "motherfuckers", "fuckface"));

    private static final Set<String> STICKY_HINT = new HashSet<>(Arrays.asList("hi", "hey", "hello", "you", "an", "all", "these", "those", "this", "that", "go", "yall", "y'all", "are", "the", "hate", "be", "like", "at", "silly", "stupid", "say", "be"));

    private static final Set<String> PLURAL_HINT = new HashSet<>(Arrays.asList("these", "those", "all"));

    private static final Map<String, List<String>> REPLACE_TEXT_MAP = new HashMap<>();

    private static final Map<String, List<String>> REPLACE_SWEARS = new HashMap<>();

    static {
        List<String> repl1 = Collections.singletonList("I'm a stupid baka");
        for (String trigger : Arrays.asList("this mod sucks", "stupid mod", "i hate this mod", "fuck you mod", "fuck this mod", "this mod sucks ass", "i hate remelon", "i hate souls", "i hate chensel", "i hate tsunify", "i hate tsunderify", "i hate tsunderifier", "garbage mod", "fuck you tsunderify", "fuck you tsunify", "bad mod", "trash mod", "never using this mod again", "im never using this mod again", "im uninstalling this mod", "im deleting this mod")) {
            REPLACE_TEXT_MAP.put(cleanKey(trigger), new ArrayList<>(repl1));
        }

        List<String> repl2 = Arrays.asList("it's not that I care about you or anything!", "don't think I care or anything!", "It's not like I'm concerned for you or anything!", "but I'm not worried, hmph!", "be safe, you silly baka! It'd be hard to replace you...", "don't think I care or anything!");
        REPLACE_TEXT_MAP.put(cleanKey("be careful"), new ArrayList<>(repl2));

        REPLACE_TEXT_MAP.put(cleanKey("i hate you"), new ArrayList<>(Collections.singletonList("it's not like I like you or anything!")));

        REPLACE_TEXT_MAP.put(cleanKey("i know"), new ArrayList<>(Arrays.asList("don't think I didn't know!", "I know, b- baka!")));
        REPLACE_TEXT_MAP.put(cleanKey("ik"), new ArrayList<>(Arrays.asList("don't think I didn't know!", "I know, b- baka!")));

        REPLACE_TEXT_MAP.put(cleanKey("im not cute"), new ArrayList<>(Collections.singletonList("I'm not c- cute!")));

        REPLACE_TEXT_MAP.put(cleanKey("wtf"), new ArrayList<>(Arrays.asList("what the, baka!", "what the, b- baka!")));

        REPLACE_SWEARS.put("fucked", Arrays.asList("messed", "m- messed"));
        REPLACE_SWEARS.put("omfg", List.of("omg"));
        REPLACE_SWEARS.put("wtf", List.of("what"));
        REPLACE_SWEARS.put("lmfao", List.of("lmao"));
    }

    public static String[] transformText(String text) {
        if (text == null) return null;
        if (!TsunderifyConfig.CONFIG.instance().modEnabled) { return new String[]{text}; }

        String lowerTrim = text.toLowerCase().trim();
        if (lowerTrim.equals("shut the fuck up") || lowerTrim.equals("sybau") && TsunderifyConfig.CONFIG.instance().swearReplacement) {
            String choice = RNG.nextBoolean() ? "shut up, baka!" : "shut up, b- baka!";
            return new String[]{matchCase(text, choice), "1"};
        }

        if (lowerTrim.equals("kys") && TsunderifyConfig.CONFIG.instance().swearReplacement) {
            String choice = RNG.nextBoolean() ? "i- ily, baka!" : "ily, baka!";
            return new String[]{matchCase(text, choice), "1"};
        }


        if (lowerTrim.equals("kill yourself") && TsunderifyConfig.CONFIG.instance().swearReplacement) {
            String choice = RNG.nextBoolean() ? "i- i love you, baka!" : "i love you, baka!";
            return new String[]{matchCase(text, choice), "1"};
        }

        String cleanedLower = cleanKey(text);
        if (REPLACE_TEXT_MAP.containsKey(cleanedLower)) {
            List<String> choices = REPLACE_TEXT_MAP.get(cleanedLower);
            String chosen = choices.get(RNG.nextInt(choices.size()));
            return new String[]{matchCase(text, chosen), "1"};
        }

        Matcher m = TOKEN_RE.matcher(text);
        List<String> tokens = new ArrayList<>();
        boolean anyAlpha = false;
        while (m.find()) {
            String tok = m.group(0);
            tokens.add(tok);
            if (tok.chars().anyMatch(Character::isLetter)) anyAlpha = true;
        }
        if (!anyAlpha) return null;

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).contains("\n")) tokens.set(i, " ");
        }

        boolean profane = false;
        boolean pluralFlag = false;
        boolean bakaMode = false;

        for (int i = 0; i < tokens.size(); i++) {
            String tok = tokens.get(i);
            String lt = tok.toLowerCase();

            if (isWord(tok) && SWEARS.contains(lt) && TsunderifyConfig.CONFIG.instance().swearReplacement) {
                bakaMode = true;

                int prevIdx = prevSigIndex(tokens, i - 1);
                String pword = prevIdx >= 0 ? tokens.get(prevIdx).toLowerCase() : null;
                int nextIdx = nextSigIndex(tokens, i + 1);
                String nword = nextIdx >= 0 ? tokens.get(nextIdx).toLowerCase() : null;

                boolean sticky = (pword != null && STICKY_HINT.contains(pword) && !lt.equals("fucking") && !lt.equals("damn")) || ALWAYS_STICKY.contains(lt);
                boolean pluralish = (lt.endsWith("s") && !lt.equals("ass") && !lt.equals("dumbass")) || (pword != null && PLURAL_HINT.contains(pword));
                boolean an = "an".equals(pword);

                if (lt.equals("fuck") && "you".equals(nword) && nextIdx >= 0) {
                    for (int j = nextIdx; j >= i; j--) {
                        tokens.remove(j);
                    }
                    String rep = RNG.nextBoolean() ? " you baka" : " you b- baka";
                    tokens.add(matchCase(tok, rep));
                    i = Math.max(0, i - 1);
                    continue;
                }

                if (sticky) {
                    String rep = pluralish ? (RNG.nextBoolean() ? "bakas" : "b- bakas") : (RNG.nextBoolean() ? "baka" : "b- baka");
                    tokens.set(i, matchCase(tok, rep));
                    if (an && prevIdx >= 0) {
                        tokens.set(prevIdx, matchCase(tokens.get(prevIdx), "a"));
                    }
                    continue;
                } else {
                    if (pluralish) pluralFlag = true;
                    profane = true;
                    if (REPLACE_SWEARS.containsKey(lt)) {
                        String rep = randChoiceList(REPLACE_SWEARS.get(lt));
                        tokens.set(i, matchCase(tok, rep));
                    } else {
                        tokens.set(i, "");
                        while (i > 0 && tokens.get(i - 1).matches("^[\"'\\(\\[\\{< ]+$")) {
                            tokens.remove(i - 1);
                            i--;
                        }
                        while (i < tokens.size() && tokens.get(i).matches("^[\"'\\)\\]\\}\\]>\\.,]+$")) {
                            tokens.remove(i);
                        }
                    }
                }
            }
        }

        if (profane) {
            boolean anyAlphaLeft = false;
            for (String t : tokens) {
                if (t != null && t.chars().anyMatch(Character::isLetter)) {
                    anyAlphaLeft = true;
                    break;
                }
            }
            if (!anyAlphaLeft) {
                String rep = pluralFlag ? (RNG.nextBoolean() ? "bakas!" : "b- bakas!") : (RNG.nextBoolean() ? "baka!" : "b- baka!");
                String lastTok = tokens.size() > 0 ? tokens.get(tokens.size() - 1) : "";
                return new String[]{matchCase(lastTok, rep), "1"};
            }

            int qm = 0;
            while (!tokens.isEmpty()) {
                String last = tokens.get(tokens.size() - 1);
                if (last.equals("?")) {
                    tokens.remove(tokens.size() - 1);
                    qm++;
                    continue;
                }
                if (last.matches("^[,\\.!? ]+$")) {
                    tokens.remove(tokens.size() - 1);
                } else break;
            }

            String rep = pluralFlag ? (RNG.nextBoolean() ? ", bakas" : ", b- bakas") : (RNG.nextBoolean() ? ", baka" : ", b- baka");
            if (!tokens.isEmpty()) {
                String last = tokens.get(tokens.size() - 1);
                tokens.add(matchCase(last, rep));
            } else {
                tokens.add(matchCase("", pluralFlag ? (RNG.nextBoolean() ? "bakas!" : "b- bakas!") : (RNG.nextBoolean() ? "baka!" : "b- baka!")));
            }

            tokens.add("!");
            for (int q = 0; q < qm; q++) tokens.add("?");
        }

        if (bakaMode) {
            if (!tokens.isEmpty()) {
                String last = tokens.get(tokens.size() - 1);
                if (!(last.endsWith("!") || last.endsWith("?"))) {
                    tokens.add("!");
                }
            } else {
                tokens.add("!");
            }
        }

        List<String> suffixes = getSuffixes();
        double configuredChance = (TsunderifyConfig.CONFIG.instance().suffixChance/100);
        boolean applySuffix = RNG.nextDouble() < configuredChance;

        if (applySuffix) {
            while (!tokens.isEmpty()) {
                String last = tokens.get(tokens.size() - 1);
                if (last.matches("^[,\\.!? ]+$")) tokens.remove(tokens.size() - 1);
                else break;
            }
            String suf = suffixes.get(RNG.nextInt(suffixes.size()));
            String last = tokens.isEmpty() ? "" : tokens.get(tokens.size() - 1);
            tokens.add(matchCase(last, suf));
        } else if (!bakaMode) {
            return null;
        }

        StringBuilder out = new StringBuilder();
        for (String t : tokens) out.append(t);
        String result = out.toString();

        if (RNG.nextDouble() < (TsunderifyConfig.CONFIG.instance().stutterChance/100)) {
            result = stutter(result);
        }

        return new String[]{result, "1"};
    }

    private static String cleanKey(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z\\s]", "").trim();
    }

    private static boolean isWord(String tok) {
        if (tok == null) return false;
        for (char c : tok.toCharArray()) if (Character.isLetter(c)) return true;
        return false;
    }

    private static int prevSigIndex(List<String> tokens, int idx) {
        for (int i = idx; i >= 0; i--) {
            String t = tokens.get(i);
            if (t == null) continue;
            if (t.trim().length() == 0) continue;
            if (t.equals("\"") || t.equals("'")) continue;
            return i;
        }
        return -1;
    }

    private static int nextSigIndex(List<String> tokens, int idx) {
        for (int i = idx; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (t == null) continue;
            if (t.trim().length() == 0) continue;
            if (t.equals("\"") || t.equals("'")) continue;
            return i;
        }
        return -1;
    }

    private static String matchCase(String word, String replacement) {
        if (word == null) return replacement;
        if (word.equals(word.toUpperCase())) return replacement.toUpperCase();
        if (word.length() > 0 && Character.isUpperCase(word.charAt(0))) {
            if (replacement.length() == 0) return replacement;
            return replacement.substring(0, 1).toUpperCase() + replacement.substring(1);
        }
        return replacement;
    }

    private static String stutter(String text) {
        int i = 0;
        while (i < text.length() && !Character.isLetter(text.charAt(i))) i++;
        if (i >= text.length()) return text;
        char first = text.charAt(i);
        return text.substring(0, i) + first + "- " + text.substring(i);
    }

    private static String randChoiceList(List<String> list) {
        return list.get(RNG.nextInt(list.size()));
    }

    private static List<String> getSuffixes() {
        return Arrays.asList(", but it's not that I like you or anything!", ", hmph!", ". hmph!", ", but don't think I'm doing this for you!", ", tch...", ". tch...", ", I guess...", ", but don't think this makes us friends!");
    }
}
